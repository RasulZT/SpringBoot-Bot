package com.parqour.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parqour.bot.Enums.*;
import com.parqour.bot.Enums.interfaces.Localizable;
import com.parqour.bot.Service.*;
import com.parqour.bot.Singleton.CurrentUser;
import com.parqour.bot.Singleton.Ticket;
import com.parqour.bot.entity.*;
import com.parqour.bot.repository.DutyRepository;
import com.parqour.bot.repository.ParkingRepository;
import com.parqour.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingBot extends TelegramLongPollingBot {

    @Value("${telegram.username}")
    private String username;

    @Value("${telegram.token}")
    private String botToken;


    @Value("${asana.project-id}")
    private String projectId;

    private final Map<String, CurrentUser> users = new HashMap<>();
    private final UserRepository userRepository;
    private static final String CREATE_TICKET_BUTTON = "Создать тикет";
    private static final String ON_DUTY_BUTTON = "Дежурные";
    private static final String MY_OPERATORS_BUTTON = "Мои операторы";
    private static final String MY_PARKINGS_BUTTON = "Мои парковки";
    private static final String ADD_USER_BUTTON = "Добавить нового пользователя";
    private static final String SET_USER_BUTTON = "Назначить пользователя";
    private static final String SET_OPERATORS_PARKING_BUTTON = "Назначить парковку";
    private static final String SET_DUTY_BUTTON = "Назначить дежурного";
    private static final String GET_USERS_BUTTON = "Список пользователей";
    private static final String MANAGE_OPERATORS_BUTTON = "Управление операторами";
    private static final String MANAGE_PARKINGS_BUTTON = "Управление парковками";
    private static final String SET_GROUP_BUTTON = "Назначить чат-группу";
    private static final String BACK_BUTTON = "Назад";
    private static final String CANCEL_BUTTON = "Отменить";
    private static final String CONTINUE_BUTTON = "Пропустить";
    private static final String CANCEL_TICKET_TEXT = "Если вы совершили ошибку или хотите отменить создание тикета - нажмите на кнопку отменить";
    private static final String WORK_SCHEDULE_TEXT = "График дежурств в нерабочее время";
    private static final String CHOOSE_CORRECT_OPTION_TEXT = "Выберите один из возможных вариантов пожалуйста!";
    private static final String TYPE_PHONE_NUMBER_TEXT = "Нажмите на кнопку и начинайте вводить номер телефона пользователя";
    private static final String TYPE_PARKING_NAME_TEXT = "Нажмите на кнопку и начинайте вводить название объекта в поле ввода сообщения";
    private static final String TAP_TO_SEARCH_TEXT = "Нажмите, чтобы начать поиск";
    private static final String UNAUTHORIZED_TEXT = """
            Уважаемый пользователь,
            Сообщаем Вам, что у Вас отсутствует соответствующий доступ к данному боту. Извините за неудобства. Если у Вас есть вопросы или потребности, пожалуйста, обратитесь к администратору или ответственному лицу, чтобы получить дополнительную информацию или разрешение на использование данного бота.
            С благодарностью,
            Команда поддержки.""";
    private static final String CONTACT_SAVED_TEXT = "Ваши контактные данные сохранены!";
    private static final String TYPE_NEW_USER_PHONE_TEXT = "Напишите номер телефона нового пользователя:";
    private static final String TYPE_GROUP_CHAT_TEXT = "Напишите название группового чата:";
    private static final String ON_DUTY_TEXT = "Дежурные: ";
    private static final String NOT_IDENTIFIED_TEXT = "Не указано";
    private static final String CHAT_NOT_ACTIVATED_TEXT = "Ваш чат не активирован. Пожалуйста, отправьте свои контактные данные, нажав на кнопку меню ниже." +
            "\n" + "Если вы были добавлены в базу ранее, вы сможете продолжить использование бота.";

    private static final String NO_PARKING_ASSIGNED_TEXT = "Вы не прикреплены к каким-либо парковкам, свяжитесь с админом!" + "\n" +
            "Пожалуйста, очистите поле ввода, нажав на кнопку 'X' или удалив текст вручную.";
    private static final String TICKET_CANCELED_TEXT = "Создание тикета отменено!";
    private static final String ACCESS_REQUIRED_TEXT = "У вас нет доступа к такому функционалу";
    private static final String LEAVE_CHAT_TEXT = "Группа не прикреплена к определенному объекту, обратитесь к Администратору бота";

    private static final String USER_ADDED = "Пользователь добавлен!";
    private static final String INVALID_DATE_MESSAGE = "Нельзя указать прошедшее время!";
    private static final String SUCCESSFUL_DUTY_ASSIGNMENT = "Дежурство успешно назначено!";
    private static final String DATE_FORMAT_HINT = "Укажите дату в корректном формате, пример: 2023-01-01";
    private static final String SELECT_ADMINISTRATOR_MESSAGE = "Выберите администратора:";
    private static final String GROUP_NAME_SET_MESSAGE = "Название группового чата указано, теперь выберите прикрепляемый объект";
    private static final String OPERATOR_SELECTED_MESSAGE = "Оператор выбран, теперь выберите прикрепляемый объект";
    private static final String ENTER_DUTY_DATE_MESSAGE = "Оператор выбран, теперь укажите дату дежурства в формате 2023-01-01";
    private static final String TICKET_APPROVED_MESSAGE = "Тикет принят пользователем - @";
    private static final String TICKET_DENIED_MESSAGE = "Тикет отклонён пользователем - @";
    private static final String TICKET_NOT_CREATED_ERROR = "Не удалось создать тикет, ошибка сервера YouTrack!";
    private static final String TICKET_ACCEPTED_YOUTRACK_MESSAGE = "и отправлен в YouTrack!!";
    private static final String NUMBER_SAVED_SELECT_ROLE_TEXT = "Номер телефона сохранен, выберите роль пользователя:";
    private static final String CONFIRM_EDITING_TEXT = "Вы собираетесь изменить данные существующего пользователя, продолжайте если подтверждаете изменения, выберите роль пользователя:";
    private static final String TYPE_VALID_NUMBER_TEXT = "Напишите валидный номер телефона пожалуйста:";
    private static final String ERROR_SEND_TEXT = "Ошибка! Пожалуйста введите текстовое сообщение";
    private static final String SEND_CONTACT = "Поделиться своими контактами";
    private static final String ADMIN_MODE_CHOOSE_COMMAND_TEXT = "Вы в режиме админа, выберите команду из меню";
    private static final String ADMIN_MODE_SUCCESS_TEXT = "Переключение на режим администратора выполнено успешно. Для возврата в обычный режим, пожалуйста, отправьте команду /start.";
    private static final String SELECTED_PROBLEM_AREA_TEXT = "Вы выбрали участок проблемы";
    private static final String TYPE_TICKET_COMMENT_TEXT = "Введите комментарий к задаче:";
    private static final String SELECTED_PARKING_TEXT = "Вы выбрали объект";
    private static final String SELECT_CRITICALITY_LEVEL_TEXT = "Укажите уровень критичности:";
    private static final String ADMIN_GROUP_ASSIGNED_TEXT = "Группа успешно установлена на объект";
    private static final String SELECT_REMOVING_USER_TEXT = "Выберите пользователя, которого хотите открепить:";
    private static final String NO_USER_ASSIGNED_TEXT = "В выбранном объекте нету прикрепленных пользователей";
    private static final String OPERATOR_REMOVED_TEXT = "Оператор успешно откреплен от объекта";
    private static final String PARKING_WAS_REMOVED_TEXT = "Объект был удален или более недоступен";
    private static final String SELECTED_UNKNOWN_USER = "Выбрали не существующего пользователя или пользователь был удален.";
    private static final String SELECTED_REMOVING_PARKING_TEXT = "Выберите объект, который хотите открепить:";
    private static final String SELECTED_USER_TEXT = "Выбрали пользователя";
    private static final String OPERATOR_ASSIGNED_TEXT = "Оператор успешно назначен админу!";
    private static final String BOT_WAS_RELOADED_TEXT = "Бот был перезагружен, выберите пользователей заново!";
    private static final String USER_NOT_ASSIGNED_PARKING_TEXT = "Пользователь не прикреплен к какой-либо парковке";
    private static final String OPERATOR_ASSIGNED_PARKING_TEXT = "Оператор успешно прикреплен к объекту";
    private static final String SELECTED_CRITICALITY_LEVEL_TEXT = "Вы выбрали уровень критичности";
    private static final String TYPE_PROBLEM_AREA = "Укажите тип проблемы:";
    private static final String TICKET_SEND_ERROR_TEXT = "Не удалось отправить тикет в чат-группу поддержки";
    private static final String TICKET_SEND_GROUP_TEXT = "Тикет отправлен в чат-группы, ссылка на тикет:";
    private static final String TICKET_TEXT = "Тикет";
    private static final String TICKET_BY_WHO_TEXT = "был принят пользователем @";
    private static final String TICKET_LINK_TEXT = "Ссылка на тикет:";

    private final List<ProblemArea> problemAreas = List.of(ProblemArea.values());
    private final List<Role> userRoles = List.of(Role.values());
    private final List<CriticalityLevel> criticalityLevels = List.of(CriticalityLevel.values());
    private final List<IncidentProblems> incidents = List.of(IncidentProblems.values());
    private final List<EventProblems> events = List.of(EventProblems.values());
    private SendMessage message = new SendMessage();
    private final UserService userService;
    private final ParkingService parkingService;
    private final ParkingRepository parkingRepository;
    private final YouTrackService youTrackService;
    private final AsanaService asanaService;
    private static final String START_MESSAGE = "Добро пожаловать в Бот для создания задач! Напишите /start чтобы начать";
    private static final String MAIN_MESSAGE = "Добро пожаловать в Бот для создания задач!";
    private Map<Long, ParkingEntity> parkings;
    private final GroupMessagingService groupMessagingService;
    private final Random random = new Random();
    private final TicketService ticketService;
    private final DutyRepository dutyRepository;
    private ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru"));
    private ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en"));

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMyChatMember()) {
            log.info("update.hasMyChatMember() - " + update.getMyChatMember());
            if (update.getMyChatMember().getChat() != null) {
                Long groupChatId = update.getMyChatMember().getChat().getId();
                String groupName = update.getMyChatMember().getChat().getTitle();
                log.info("groupName - " + groupName);
                log.info("groupChatId - " + groupChatId);
                ParkingEntity parking = parkingService.getParkingByGroupChatId(groupChatId).orElse(parkingService.getParkingByGroupName(groupName).orElse(null));
                ServiceGroupEntity serviceGroupEntity = parkingService.getGroupByGroupChatId(groupChatId).orElse(parkingService.getGroupByGroupName(groupName).orElse(null));

                if (parking == null && serviceGroupEntity == null) {
                    leaveChat(groupChatId.toString());
                }
                if (serviceGroupEntity != null) {
                    serviceGroupEntity.setGroupChatId(groupChatId);
                    parkingService.saveGroup(serviceGroupEntity);
                }
                if (parking != null) {
                    parking.setGroupChatId(groupChatId);
                    parkingService.save(parking);
//                    sendLinkToBotButton(groupChatId.toString());
                }
            }

        }


        if (update.hasInlineQuery()) {
            parkings = parkingService.getAllParkingsAsMap();
            List<UserEntity> operators = userService.getAlLUsers();

            String chatId = update.getInlineQuery().getFrom().getId().toString();
            CurrentUser currentUser = identifyCurrentUser(chatId);
            State state = currentUser.getState();
            log.info("state: ", state);
            if (state == null) {
                state = State.START;
            }
            if (state.equals(State.WAITING_PARKING)) {
                Set<ParkingEntity> userParkings = currentUser.getUser().getParkings();
                if (userParkings.isEmpty()) {
                    sendTextMessage(NO_PARKING_ASSIGNED_TEXT, chatId);
                    sendMessage(identifiedUser(currentUser), chatId);
                } else {
                    sendParkings(parkingService.getAllParkingsAsMapFromList(userParkings), update);
                }
            } else if (state.equals(State.ADMIN_WAITING_PARKING)) {
                sendParkings(parkings, update);
            } else if (state.equals(State.ADMIN_OPERATORS_PARKING)) {
                sendParkings(parkings, update);
            } else if (state.equals(State.ADMIN_WAITING_PARKINGS_MANAGING)) {
                sendParkings(parkings, update);
            } else if (state.equals(State.ADMIN_WAITING_OPERATORS_MANAGING)) {
                sendOperators(operators, update);
            } else if (state.equals(State.ADMIN_WAITING_OPERATOR)) {
                sendOperators(operators, update);
            } else if (state.equals(State.ADMIN_WAITING_DUTY_OPERATOR)) {
                sendOperators(operators, update);
            } else if (state.equals(State.ADMIN_WAITING_SET_USER)) {
                sendOperators(operators, update);
            } else {
                return;
            }
        }


        if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            CurrentUser currentUser = identifyCurrentUser(chatId);
            State state = currentUser.getState();

            ResourceBundle bundle = getBundleFromUser(currentUser.getUser());

            log.info("User callback chat id - " + chatId + " State - " + state);

            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals(CONTINUE_BUTTON)) {
                users.put(chatId, currentUser);
                endOfQuestionnaire(chatId);
            } else if (state.equals(State.WAITING_PROBLEM_AREA)) {
                obtainProblemArea(callbackData, chatId);
            } else if (state.equals(State.WAITING_CRITICALITY)) {
                obtainCriticality(callbackData, chatId);
            } else if (state.equals(State.WAITING_DESCRIPTION)) {
                obtainDescriptionResult(callbackData, chatId, bundle);
            } else if (state.equals(State.WAITING_ADD_USER_ROLE)) {
                obtainAddUserRole(callbackData, chatId);
            } else if (state.equals(State.ADMIN_WAITING_MANAGE_PARKING)) {
                obtainAdminOperatorsManageParkingResult(callbackData, chatId);
            } else if (state.equals(State.ADMIN_WAITING_MANAGE_PARKING_OPERATOR)) {
                obtainAdminParkingManagingOperatorResult(callbackData, chatId);
            } else if (state.equals(State.ADMIN_WAITING_SET_ADMIN)) {
                obtainAdminSetAdminResult(callbackData, chatId, bundle);
            } else if (parseAsJson(callbackData) != null) {
                obtainTicketCallback(update, callbackData);
            }

            users.put(chatId, currentUser);
        }
        if (update.hasMessage()) {
            if (update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat()) {
                return; // Do nothing for group chats
            }
            String chatId = update.getMessage().getChatId().toString();
            CurrentUser currentUser = identifyCurrentUser(chatId);
            State state = currentUser.getState();
            UserEntity user;
            ResourceBundle bundle = getBundleFromUser(currentUser.getUser());

            if (!userService.isUserExistByChatId(chatId)) {
                currentUser.setState(State.CONTACT);
                message.setText(CHAT_NOT_ACTIVATED_TEXT);
//                sendMessage(sendContact(message), chatId);
            }

            if (state == null) {
                state = State.START;
            }

            log.info("User message chat id - " + chatId + " State - " + state);

            //Send Contact
            message.setChatId(chatId);

            if (update.getMessage().getContact() != null) {
                String phoneNumber = update.getMessage().getContact().getPhoneNumber();
                phoneNumber = editPhoneNumber(phoneNumber);
                String language = update.getMessage().getFrom().getLanguageCode();
                if (userService.isUserExistByPhoneNumber(phoneNumber)) {

                    log.info("New user - " + phoneNumber);

                    if (update.getMessage().getChat() != null) {
                        log.info("Username - " + update.getMessage().getChat().getUserName());
                        String userName = update.getMessage().getChat().getUserName();
                        user = userRepository.findByPhoneNumber(phoneNumber).orElse(new UserEntity());
                        user.setUsername(userName);
                        user.setChatId(chatId);
                        user.setPhoneNumber(phoneNumber);
                        user.setAdmin(false);
                        user.setLanguageCode(UserEntity.LanguageCode.valueOf(language));

                        if (user.getRole() == null) {
                            user.setRole(Role.OPERATOR);
                        }

                        currentUser.setUser(user);
                        userRepository.save(user);
                        currentUser.setState(State.START);
                        sendTextMessage(CONTACT_SAVED_TEXT, chatId);

                        users.put(chatId, currentUser);
                        sendMessage(identifiedUser(currentUser), chatId);
                    }
                } else {
                    if ((update.getMyChatMember() == null) || (update.getMyChatMember().getChat() == null)) {
                        sendTextMessage(UNAUTHORIZED_TEXT, chatId);
                    }
                }
            }


            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                String bundleKey = getKeyFromValue(messageText);
                if (bundleKey != null) {
                    messageText = bundleRu.getString(bundleKey);
                }

                switch (messageText) {
                    case "/start":
                        log.info("START - " + chatId);
                        currentUser.setState(State.START);
                        UserEntity userEntity = userService.getUserByChatId(chatId);
                        String language = update.getMessage().getFrom().getLanguageCode();
                        if (userEntity == null || userEntity.getUsername() == null || userEntity.getUsername().isBlank()) {
                            currentUser.setState(State.CONTACT);
                            bundle = getBundleFromLanguageCode(language);
                            message.setText(CHAT_NOT_ACTIVATED_TEXT);
                            sendMessage(sendContact(message, bundle), chatId, bundle);
                        } else {
                            if (language != null && !language.isBlank()) {
                                userEntity.setLanguageCode(UserEntity.LanguageCode.valueOf(update.getMessage().getFrom().getLanguageCode()));
                                userRepository.save(userEntity);
                            }
                            currentUser.setUser(userEntity);
                            sendMessage(identifiedUser(currentUser), chatId);
                        }
                        break;
                    case "/admin":
                        if (userService.isUserAdminByChatId(chatId)) { //userRepository.findByUsername(username).isPresent()
                            currentUser.setState(State.ADMIN);
                            identifiedAdmin(chatId);
                        } else {
                            sendTextMessage(ACCESS_REQUIRED_TEXT, chatId);
                            currentUser.setState(State.START);
                            sendMessage(identifiedUser(currentUser), chatId);
                        }
                        break;
                    case CANCEL_BUTTON:
                        currentUser.setState(State.START);
                        users.put(chatId, currentUser);
                        sendTextMessage(TICKET_CANCELED_TEXT, chatId);
                        sendMessage(identifiedUser(currentUser), chatId);
                        break;
                    case BACK_BUTTON:
                        if (currentUser.getUser().isAdmin()) {
                            currentUser.setState(State.ADMIN);
                            identifiedAdmin(chatId);
                        } else {
                            currentUser.setState(State.START);
                            sendMessage(identifiedUser(currentUser), chatId);
                        }
                        break;
                    case ADD_USER_BUTTON:
                        sendTextMessage(TYPE_NEW_USER_PHONE_TEXT, chatId);
                        currentUser.setState(State.WAITING_ADD_USER);
                        break;
                    case GET_USERS_BUTTON:
                        message.setText(userService.getUsersInfoByUsers().toString());
                        message.setReplyMarkup(backButton(bundle));
                        sendMessage(message, chatId);
                        break;
                    case MANAGE_OPERATORS_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_OPERATORS_MANAGING);
                        message.setReplyMarkup(backButton(bundle));
                        sendMessage(operatorsButtonRedirectorLogic(bundle), chatId);
                        break;
                    case SET_USER_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_SET_ADMIN);
                        message.setReplyMarkup(backButton(bundle));
                        sendMessage(sendAdminsToManageUsers(message), chatId);
                        break;
                    case MANAGE_PARKINGS_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_PARKINGS_MANAGING);
                        message.setReplyMarkup(backButton(bundle));
                        buttonRedirectorLogic(message, chatId, bundle);
                        break;
                    case SET_GROUP_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_GROUP_NAME);
                        message.setText(TYPE_GROUP_CHAT_TEXT);
                        message.setReplyMarkup(backButton(bundle));
                        sendMessage(message, chatId);
                        break;
                    case SET_OPERATORS_PARKING_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_OPERATOR);
                        sendMessage(operatorsButtonRedirectorLogic(bundle), chatId);
                        break;
                    case SET_DUTY_BUTTON:
                        currentUser.setState(State.ADMIN_WAITING_DUTY_OPERATOR);
                        sendMessage(operatorsButtonRedirectorLogic(bundle), chatId);
                        break;
                    case CREATE_TICKET_BUTTON:
                        currentUser.setState(State.PARKING);
                        message.setReplyMarkup(cancelButton(bundle));
                        message.setText(CANCEL_TICKET_TEXT);
                        sendMessage(message, chatId);
                        buttonRedirectorLogic(message, chatId, bundle);
                        currentUser.setState(State.WAITING_PARKING);
                        break;
                    case ON_DUTY_BUTTON:
                        SendMessage newMessage = new SendMessage();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(bundle.getString("ON_DUTY_TEXT")).append("\n");

                        for (String duty : groupMessagingService.getDutyOperators()) {
                            stringBuilder.append(duty).append(" ");
                        }

                        stringBuilder.append("\n").append("<a href=\"").append("https://docs.google.com/spreadsheets/d/13aF5938Jb6-9twaUa64qTmQiBjWa-NklajjA_ADlZ2Y/edit#gid=1746907808").append("\">")
                                .append(bundle.getString("WORK_SCHEDULE_TEXT")).append("</a>").append("\n");

                        newMessage.enableHtml(true);
                        newMessage.setParseMode("HTML");
                        newMessage.setText(stringBuilder.toString());
                        newMessage.setDisableWebPagePreview(true);
                        sendMessage(newMessage, chatId);
                        currentUser.setState(State.START);
                        break;
                    case MY_OPERATORS_BUTTON:
                        UserEntity userOperators = userService.getUserByChatId(chatId);
                        message.setText(userService.getUsersInfoByUsers(userOperators.getOperators()).toString());
                        currentUser.setState(State.START);
                        sendMessage(message, chatId);
                        break;
                    case MY_PARKINGS_BUTTON:
                        UserEntity userParkings = userService.getUserByChatId(chatId);
                        message.setText(parkingService.getParkingsInfoByUsers(userParkings.getParkings()).toString());
                        currentUser.setState(State.START);
                        sendMessage(message, chatId);
                        break;
                    default:
                        if (state.equals(State.ADMIN_WAITING_PARKING)) {
                            obtainAdminParkingResult(messageText, chatId, parkings, bundle);
                        } else if (state.equals(State.ADMIN_OPERATORS_PARKING)) {
                            obtainAdminOperatorsParkingResult(messageText, chatId, parkings, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_GROUP_NAME)) {
                            obtainGroupNameResult(messageText, chatId, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_PARKINGS_MANAGING)) {
                            obtainAdminParkingManagingResult(messageText, chatId, parkings, bundle);
                        } else if (state.equals(State.WAITING_PARKING)) {
                            obtainParkingResult(messageText, chatId, parkings, bundle);
                        } else if (state.equals(State.WAITING_DESCRIPTION)) {
                            obtainDescriptionResult(messageText, chatId, bundle);
                        } else if (state.equals(State.WAITING_ADD_USER)) {
                            obtainAddUser(messageText, currentUser, chatId, message, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_DUTY_DATE)) {
                            obtainDutyDate(messageText, currentUser, chatId, message, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_OPERATORS_MANAGING)) {
                            obtainAdminOperatorsManageResult(messageText, chatId, message, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_OPERATOR)) {
                            obtainOperatorNameResult(messageText, chatId, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_DUTY_OPERATOR)) {
                            obtainDutyOperatorNameResult(messageText, chatId, message, bundle);
                        } else if (state.equals(State.ADMIN_WAITING_SET_USER)) {
                            obtainAdminSetUserResult(messageText, chatId);
                        } else {
                            identifiedUser(currentUser);
                        }
                }
                users.put(chatId, currentUser);
            }

        }

    }

    private void obtainDutyDate(String messageText, CurrentUser currentUser, String chatId, SendMessage message, ResourceBundle bundle) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        message.setReplyMarkup(backButton(bundle));
        message.setChatId(chatId);
        try {
            LocalDate parsedDate = LocalDate.parse(messageText, dateFormatter);
            Long operatorId = currentUser.getAddingDutyOperatorId();
            UserEntity adminUser = userService.getUserById(operatorId);
            if (parsedDate.isBefore(LocalDate.now().atStartOfDay().toLocalDate())) {
                message.setText(INVALID_DATE_MESSAGE);
                sendMessage(message, chatId);
            } else {
                DutiesEntity duty = new DutiesEntity();
                duty.setUser(currentUser.getUser());
                duty.setAssignedUser(adminUser);
                duty.setDutyDate(parsedDate);
                dutyRepository.save(duty);
                message.setText(SUCCESSFUL_DUTY_ASSIGNMENT);
                sendMessage(message, chatId);
            }
        } catch (DateTimeParseException e) {
            message.setText(DATE_FORMAT_HINT);
            sendMessage(message, chatId);
        }
    }

    private SendMessage sendAdminsToManageUsers(SendMessage message) {
        List<UserEntity> adminUsers = userService.getAlLUsers();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();


        for (UserEntity user : adminUsers) {
            if (user.getUsername() != null && user.getRole() != null && user.getRole().equals(Role.PARKING_ADMIN)) {
                InlineKeyboardButton button = new InlineKeyboardButton(user.getPhoneNumber() + " - " + user.getUsername() + " - " + user.getRole().getRussianValue());
                button.setCallbackData(user.getId().toString());
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);
            }
        }

        // Add rows to the keyboard
        markup.setKeyboard(keyboard);
        message.setText(SELECT_ADMINISTRATOR_MESSAGE);
        message.setReplyMarkup(markup);
        return message;
    }

    private void obtainGroupNameResult(String messageText, String chatId, ResourceBundle bundle) {
        CurrentUser currentUser = users.get(chatId);
        currentUser.setAddingGroupName(messageText);
        sendTextMessage(GROUP_NAME_SET_MESSAGE, chatId);
        buttonRedirectorLogic(message, chatId, bundle);
        currentUser.setState(State.ADMIN_WAITING_PARKING);
    }

    private void obtainOperatorNameResult(String callbackData, String chatId, ResourceBundle bundle) {
        CurrentUser currentUser = users.get(chatId);
        UserEntity operator = userService.getUserByPhoneNumber(callbackData);
        currentUser.setAddingOperatorId(operator.getId());
        sendTextMessage(OPERATOR_SELECTED_MESSAGE, chatId);
        buttonRedirectorLogic(message, chatId, bundle);
        currentUser.setState(State.ADMIN_OPERATORS_PARKING);
    }

    private void obtainDutyOperatorNameResult(String callbackData, String chatId, SendMessage message, ResourceBundle bundle) {
        CurrentUser currentUser = users.get(chatId);
        UserEntity operator = userService.getUserByPhoneNumber(callbackData);
        currentUser.setAddingDutyOperatorId(operator.getId());
        currentUser.setState(State.ADMIN_WAITING_DUTY_DATE);
        message.setText(ENTER_DUTY_DATE_MESSAGE);
        message.setReplyMarkup(backButton(bundle));
        message.setChatId(chatId);
        sendMessage(message, chatId);
    }

    private void obtainAddUserRole(String callbackData, String chatId) {
        try {
            CurrentUser currentUser = users.get(chatId);
            Role role = Role.valueOf(callbackData);
            String phoneNumber = currentUser.getAddingUserPhoneNumber();
            UserEntity user = userRepository.findByPhoneNumber(phoneNumber).orElse(new UserEntity());
            user.setPhoneNumber(phoneNumber);
            user.setRole(role);

            userService.save(user);

            currentUser.setState(State.ADMIN);
            currentUser.setAddingUserPhoneNumber(null);
            users.put(chatId, currentUser);
            sendTextMessage(USER_ADDED, chatId);
            identifiedAdmin(chatId);
        } catch (Exception e) {
            log.error("Error WAITING_ADD_USER_ROLE - " + e.getMessage());
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainTicketCallback(Update update, String callbackData) {
        try {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

            String callbackUsername = update.getCallbackQuery().getFrom().getUserName();
            String groupChatId = update.getCallbackQuery().getMessage().getChat().getId().toString();
            JsonNode jsonNode = parseAsJson(callbackData);
            String result = jsonNode.get("result").textValue();
            String orderId = jsonNode.get("orderId").textValue();

            EditMessageText editMessageText = new EditMessageText();
            TicketEntity ticket = ticketService.getTicketByOrderId(orderId).orElse(null);
            String ticketText = update.getCallbackQuery().getMessage().getText();

            if (ticket != null) {
                SendMessage messageToTicketCreator = new SendMessage();
                messageToTicketCreator.setChatId(ticket.getUser().getChatId());
                if (ticket.getMessageId() != null) {
                    messageToTicketCreator.setReplyToMessageId(ticket.getMessageId());
                }

                ParkingEntity parking = ticket.getParking();
                if (result.equals("APPROVE")) {
                    ticketText = ticketText + "\n" + TICKET_APPROVED_MESSAGE + callbackUsername;
                    String issueId = youTrackService.createIssue(ticket);
                    String asanaIssueId = asanaService.createIssue(ticket);
                    if (issueId != null) {
                        ticket.setYouTrackIssueId(issueId);
                        //ticketService.save(ticket);
                        String ticketUrl = "https://youtrack.parqour.com/issue/" + issueId;
                        ticketText = ticketText + "\n" + TICKET_LINK_TEXT + " " + ticketUrl;
                        messageToTicketCreator.setText(TICKET_TEXT + " " + ticketUrl + " " + TICKET_BY_WHO_TEXT + callbackUsername + TICKET_ACCEPTED_YOUTRACK_MESSAGE);
                        if (parking != null && parking.getGroupChatId() != null) {
                            String parkingGroupChatId = parking.getGroupChatId().toString();
                            sendTextMessage(ticketText, parkingGroupChatId);
                        }
                    } else {
                        messageToTicketCreator.setText(TICKET_NOT_CREATED_ERROR);
                    }

                    if (asanaIssueId!= null) {
                        ticket.setAsanaIssueId(asanaIssueId);
                        String asanaTicketUrl = "https://app.asana.com/0/" + projectId + "/" + asanaIssueId;
                        ticketText = ticketText + "\n" + TICKET_LINK_TEXT + " " + asanaTicketUrl;
                        messageToTicketCreator.setText(TICKET_TEXT + " " + asanaTicketUrl + " " + TICKET_BY_WHO_TEXT + callbackUsername + TICKET_ACCEPTED_YOUTRACK_MESSAGE);
                        if (parking != null && parking.getGroupChatId() != null) {
                            String parkingGroupChatId = parking.getGroupChatId().toString();
                            sendTextMessage(ticketText, parkingGroupChatId);
                        }
                    }
                    ticket.setCommentsUpdatedTime(null);
                    ticketService.save(ticket);
                } else if (result.equals("DENY")) {
                    ticketText = ticketText + "\n" + TICKET_DENIED_MESSAGE + callbackUsername;
                    messageToTicketCreator.setText(TICKET_DENIED_MESSAGE + callbackUsername);
                }
                sendMessage(messageToTicketCreator, ticket.getUser().getChatId());
            }

            editMessageText.setText(ticketText);
            editMessageText.setChatId(groupChatId);
            editMessageText.setMessageId(messageId);


            editMessage(editMessageText);

        } catch (Exception e) {
            log.error("Error obtainTicketCallback - " + e.getMessage());
        }
    }

    private void obtainAddUser(String messageText, CurrentUser currentUser, String chatId, SendMessage message, ResourceBundle bundle) {
        messageText = messageText.trim().replaceAll("\\s", "");
        if (isPhoneNumber(messageText)) {
            String phoneNumber = editPhoneNumber(messageText);
            String resultMessage = NUMBER_SAVED_SELECT_ROLE_TEXT;
            if (userService.isUserExistByPhoneNumber(phoneNumber)) {
                resultMessage = CONFIRM_EDITING_TEXT;
            }

            currentUser.setAddingUserPhoneNumber(phoneNumber);

            currentUser.setState(State.WAITING_ADD_USER_ROLE);
            users.put(chatId, currentUser);
            createKeyboardMarkup(message, userRoles, currentUser.getUser().getLanguageCode().toString());
            message.setText(resultMessage);
            sendMessage(message, chatId);
        } else {
            sendTextMessage(TYPE_VALID_NUMBER_TEXT, chatId);
        }
    }

    private boolean isPhoneNumber(String text) {
        // Remove any non-digit characters from the text
        String digitsOnly = text.replaceAll("\\D", "");

        // Define the regex pattern for phone numbers
        String phonePattern = "^(\\+\\d{1,3})?\\d{10,14}$";

        // Check if the digitsOnly string matches the phonePattern regex
        return digitsOnly.matches(phonePattern);
    }

    private void sendParkings(Map<Long, ParkingEntity> parkings, Update update) {
        try {
            if (update.hasInlineQuery()) {
                try {

                    InlineQuery inlineQuery = update.getInlineQuery();
                    InlineQueryResult queryResult;
                    InputTextMessageContent textMessageContent;

                    String parkingName = inlineQuery.getQuery();

                    List<InlineQueryResult> results = new ArrayList<>();

                    List<ParkingEntity> similarParkings = new ArrayList<>();

                    for (ParkingEntity parking : parkings.values()) {
                        if (parking.getName().toLowerCase().contains(parkingName.toLowerCase())) {
                            similarParkings.add(parking);
                        }
                    }

                    if (similarParkings.size() > 10) {
                        similarParkings = similarParkings.subList(0, 10);
                    }
                    for (ParkingEntity parking : similarParkings) {
                        InlineQueryResultArticle article = new InlineQueryResultArticle();
                        article.setId(parking.getId().toString());
                        article.setInputMessageContent(new InputTextMessageContent(parking.getName()));
                        article.setTitle(parking.getName());
                        results.add(article);
                    }

                    try {
                        this.execute(new AnswerInlineQuery(update.getInlineQuery().getId(), results));
                    } catch (TelegramApiException e) {
                        //
                    }
                } catch (NullPointerException ne) {
                    //
                }
            }
        } catch (Exception e) {
            //
        }
    }

    private void sendOperators(List<UserEntity> users, Update update) {
        try {
            if (update.hasInlineQuery()) {
                try {
                    InlineQuery inlineQuery = update.getInlineQuery();
                    String phoneNumber = inlineQuery.getQuery();
                    List<InlineQueryResult> results = new ArrayList<>();
                    List<UserEntity> similarUsers = new ArrayList<>();
                    for (UserEntity user : users) {
                        if (user.getPhoneNumber().toLowerCase().contains(phoneNumber.toLowerCase())) {
                            similarUsers.add(user);
                        }
                    }

                    if (similarUsers.size() > 5) {
                        similarUsers = similarUsers.subList(0, 5);
                    }
                    for (UserEntity user : similarUsers) {
                        InlineQueryResultArticle article = new InlineQueryResultArticle();
                        article.setId(user.getId().toString());
                        article.setInputMessageContent(new InputTextMessageContent(user.getPhoneNumber()));
                        article.setTitle(user.getPhoneNumber());
                        article.setDescription(user.getRole() + " - " + getValueOrDefault(user.getUsername()) + " " + user.getParkings().size() + " parkings");
                        results.add(article);
                    }
                    executeInlineQueries(update.getInlineQuery().getId(), results);

                } catch (NullPointerException ne) {
                    log.error("sendOperators NullPointerException {}", ne.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("sendOperators exception {}", e.getMessage());
        }
    }

    private void executeInlineQueries(String inlineQueryId, List<InlineQueryResult> results) {
        try {
            this.execute(new AnswerInlineQuery(inlineQueryId, results));
        } catch (TelegramApiException e) {
//            log.error("executeInlineQueries exception {}", e.getMessage());
        }
    }

    public void sendTextMessage(String text, String chatId) {
        sendTextMessage(text, null, chatId);
    }

    public void sendTextMessage(String text, String additionalText, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        CurrentUser currentUser = users.get(chatId);
        ResourceBundle bundle = getBundleFromUser(currentUser.getUser());

        String key = getKeyFromValue(text);
        if (key != null) {
            sendMessage.setText(bundle.getString(key));
        }

        if (additionalText != null) {
            sendMessage.setText(sendMessage.getText() + additionalText);
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("sendTextMessage exception {}", e.getMessage());
        }
    }

    public void sendMessage(SendMessage message, String chatId, ResourceBundle bundle) {
        try {
            String text = message.getText();
            String key = getKeyFromValue(text);
            if (key != null) {
                message.setText(bundle.getString(key));
            }
            message.setChatId(chatId);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage exception {}", e.getMessage());
        }
    }

    public void sendMessage(SendMessage message, String chatId) {
        try {
            CurrentUser user = users.get(chatId);
            String languageCode = "en";
            ResourceBundle bundle = bundleEn;
            if (user != null &&
                    user.getUser() != null &&
                    user.getUser().getChatId() != null &&
                    user.getUser().getLanguageCode() != null) {
                languageCode = user.getUser().getLanguageCode().toString();
            }

            if (languageCode.equals("ru")) {
                bundle = bundleRu;
            }
            String text = message.getText();
            String key = getKeyFromValue(text);
            if (key != null) {
                message.setText(bundle.getString(key));
            }
            message.setChatId(chatId);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage exception {}", e.getMessage());
        }
    }

    public void editMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("editMessage exception {}", e.getMessage());
        }
    }

    public void leaveChat(String groupId) {
        try {
            sendTextMessage(LEAVE_CHAT_TEXT, groupId);
            LeaveChat leaveChat = new LeaveChat();
            leaveChat.setChatId(groupId);
            execute(leaveChat);
        } catch (TelegramApiException e) {
            log.error("leaveChat exception {}", e.getMessage());
        }
    }

    private String editPhoneNumber(String phoneNumber) {
        if (phoneNumber.charAt(0) == '+') {
            phoneNumber = phoneNumber.substring(1);
        }
        if (phoneNumber.charAt(0) == '8') {
            phoneNumber = '7' + phoneNumber.substring(1);
        }
        return phoneNumber;
    }

    private void obtainDescriptionResult(String messageText, String chatId, ResourceBundle bundle) {
        try {
            CurrentUser currentUser = users.get(chatId);
            UserEntity user = currentUser.getUser();


            currentUser.setState(State.START);
            currentUser.setUser(user);
            Ticket ticket = currentUser.getTicket();
            ticket.setDescription(messageText);
            currentUser.setTicket(ticket);

            users.put(chatId, currentUser);

            endOfQuestionnaire(chatId);
        } catch (DateTimeParseException e) {
            sendTextMessage(ERROR_SEND_TEXT, chatId);
        }
    }

    private SendMessage sendContact(SendMessage message, ResourceBundle bundle) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();


        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(bundle.getString("SEND_CONTACT"));
        keyboardButton.setRequestContact(true);
        row.add(keyboardButton);

        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    private <E extends Enum<E> & Localizable> void createKeyboardMarkup(SendMessage message, List<E> items, String languageCode) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (E item : items) {
            InlineKeyboardButton button = new InlineKeyboardButton(item.getLocalizedValue(languageCode));
            button.setCallbackData(item.name());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
    }

    public SendMessage identifiedUser(CurrentUser currentUser) {
        ResourceBundle bundle = getBundleFromUser(currentUser.getUser());
        message.setText(bundle.getString("MAIN_MESSAGE"));

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();


        row1.add(bundle.getString("CREATE_TICKET_BUTTON"));
        row2.add(bundle.getString("ON_DUTY_BUTTON"));
        row2.add(bundle.getString("MY_PARKINGS_BUTTON"));

        if (currentUser != null && currentUser.getUser() != null && currentUser.getUser().getRole() != null) {
            if (currentUser.getUser().getRole().equals(Role.PARKING_ADMIN)) {
                row2.add(bundle.getString("MY_OPERATORS_BUTTON"));
            }
        }

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);

        return message;
    }

    public void identifiedAdmin(String chatId) {
        message.setText(ADMIN_MODE_CHOOSE_COMMAND_TEXT);
        ResourceBundle bundle = getBundleFromUser(users.get(chatId).getUser());
        message.setReplyMarkup(backButton(bundle));

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();


        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add(bundle.getString("ADD_USER_BUTTON"));
        row1.add(bundle.getString("SET_USER_BUTTON"));

        row2.add(bundle.getString("GET_USERS_BUTTON"));
        row2.add(bundle.getString("MANAGE_OPERATORS_BUTTON"));
        row2.add(bundle.getString("MANAGE_PARKINGS_BUTTON"));

        row3.add(bundle.getString("SET_GROUP_BUTTON"));
        row3.add(bundle.getString("SET_DUTY_BUTTON"));
        row3.add(bundle.getString("SET_OPERATORS_PARKING_BUTTON"));

        row4.add(bundle.getString("BACK_BUTTON"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);


        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        sendMessage(message, chatId);
    }

    private void obtainProblemArea(String callbackData, String chatId) {
        CurrentUser currentUser = users.get(chatId);
        Ticket ticket = currentUser.getTicket();
        try {
            setProblemAreaAndSendMessages(currentUser, ticket, IncidentProblems.valueOf(callbackData).name(), chatId);
        } catch (IllegalArgumentException incidentException) {
            try {
                setProblemAreaAndSendMessages(currentUser, ticket, EventProblems.valueOf(callbackData).name(), chatId);
            } catch (IllegalArgumentException eventException) {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        }
    }

    private void setProblemAreaAndSendMessages(CurrentUser currentUser, Ticket ticket, String problemArea, String chatId) {
        ticket.setProblemArea(problemArea);
        currentUser.setTicket(ticket);
        sendTextMessage(SELECTED_PROBLEM_AREA_TEXT, " - " + problemArea, chatId);
        sendTextMessage(TYPE_TICKET_COMMENT_TEXT, chatId);
        currentUser.setState(State.WAITING_DESCRIPTION);
        users.put(chatId, currentUser);
    }


    private void obtainParkingResult(String callbackData, String chatId, Map<Long, ParkingEntity> parkings, ResourceBundle bundle) {
        try {
            if (parkings == null || parkings.isEmpty()) {
                parkings = parkingService.getAllParkingsAsMap();
            }
            ParkingEntity parking = getParkingByName(parkings, callbackData);

            if (parking != null) {
                CurrentUser currentUser = users.get(chatId);
                UserEntity writingUser = userService.getUserById(currentUser.getUser().getId());
                if (writingUser.getParkings() != null && writingUser.getParkings().contains(parking)) {
                    Ticket ticket = new Ticket();
                    ticket.setParkingEntity(parking);
                    ticket.setParking(parking.getName());
                    currentUser.setTicket(ticket);
                    currentUser.setUser(writingUser);

                    sendTextMessage(SELECTED_PARKING_TEXT, " - " + parking.getName(), chatId);

                    sendTextMessage(TYPE_TICKET_COMMENT_TEXT, chatId);

//                    message.setText(SELECT_CRITICALITY_LEVEL_TEXT);
//                    createKeyboardMarkup(message, criticalityLevels, currentUser.getUser().getLanguageCode().toString());
//                    sendMessage(message, chatId);
                    currentUser.setState(State.WAITING_DESCRIPTION);
                    users.put(chatId, currentUser);
                } else {
                    sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
                }
            } else {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminParkingResult(String callbackData, String chatId, Map<Long, ParkingEntity> parkings, ResourceBundle bundle) {
        try {
            if (parkings == null || parkings.isEmpty()) {
                parkings = parkingService.getAllParkingsAsMap();
            }

            callbackData = callbackData.trim();
            ParkingEntity parking = getParkingByName(parkings, callbackData);

            if (parking != null) {
                CurrentUser currentUser = users.get(chatId);
                currentUser.setState(State.ADMIN);
                parking.setGroupName(currentUser.getAddingGroupName());
                parkingService.save(parking);
                sendTextMessage(ADMIN_GROUP_ASSIGNED_TEXT, " - " + parking.getName(), chatId);
                identifiedAdmin(chatId);
            } else {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminParkingManagingResult(String callbackData, String chatId, Map<Long, ParkingEntity> parkings, ResourceBundle bundle) {
        try {
            if (parkings == null || parkings.isEmpty()) {
                parkings = parkingService.getAllParkingsAsMap();
            }

            callbackData = callbackData.trim();
            ParkingEntity parking = getParkingByName(parkings, callbackData);

            if (parking != null) {
                CurrentUser currentUser = users.get(chatId);

                InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
                List<UserEntity> parkingUsers = getUsersForParking(userService.getAlLUsers(), parking);
                if (!parkingUsers.isEmpty()) {

                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                    for (UserEntity user : parkingUsers) {
                        if (user.getUsername() != null) {
                            InlineKeyboardButton button = new InlineKeyboardButton(user.getPhoneNumber() + " - " + user.getUsername());
                            button.setCallbackData(user.getId().toString());
                            List<InlineKeyboardButton> row = new ArrayList<>();
                            row.add(button);
                            keyboard.add(row);
                        }
                    }
                    replyKeyboardMarkup.setKeyboard(keyboard);

                    currentUser.setState(State.ADMIN_WAITING_MANAGE_PARKING_OPERATOR);
                    currentUser.setManagingParkingId(parking.getId());
                    users.put(chatId, currentUser);
                    message.setReplyMarkup(replyKeyboardMarkup);
                    message.setText(SELECT_REMOVING_USER_TEXT);
                    sendMessage(message, chatId);
                } else {
                    sendTextMessage(NO_USER_ASSIGNED_TEXT, chatId);
                    message.setReplyMarkup(backButton(bundle));
                    buttonRedirectorLogic(message, chatId, bundle);
                }
            } else {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminParkingManagingOperatorResult(String callbackData, String chatId) {
        try {
            UserEntity managingUser = userService.getUserById(Long.valueOf(callbackData));
            CurrentUser currentUser = users.get(chatId);
            if (managingUser != null) {
                ParkingEntity managingParking = parkingService.getParkingById(currentUser.getManagingParkingId());
                if (managingParking != null) {
                    Set<ParkingEntity> parkingEntities = managingUser.getParkings();
                    parkingEntities.remove(managingParking);
                    managingUser.setParkings(parkingEntities);
                    userService.save(managingUser);
                    sendTextMessage(OPERATOR_REMOVED_TEXT, " - " + managingParking.getName(), chatId);
                    currentUser.setState(State.ADMIN);
                    users.put(chatId, currentUser);
                    identifiedAdmin(chatId);
                } else {
                    sendTextMessage(PARKING_WAS_REMOVED_TEXT, chatId);
                    currentUser.setState(State.ADMIN);
                    identifiedAdmin(chatId);
                }
            } else {
                sendTextMessage(SELECTED_UNKNOWN_USER, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminSetAdminResult(String callbackData, String chatId, ResourceBundle bundle) {
        try {
            UserEntity adminUser = userService.getUserById(Long.valueOf(callbackData));
            CurrentUser currentUser = users.get(chatId);
            if (adminUser != null) {
                currentUser.setManagingUserId(adminUser.getId());
                currentUser.setState(State.ADMIN_WAITING_SET_USER);
                users.put(chatId, currentUser);
                sendTextMessage(SELECTED_USER_TEXT, " - " + adminUser.getPhoneNumber(), chatId);

                sendMessage(operatorsButtonRedirectorLogic(bundle), chatId);
            } else {
                sendTextMessage(SELECTED_UNKNOWN_USER, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный ответ");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminSetUserResult(String callbackData, String chatId) {
        try {
            UserEntity addingUser = userService.getUserByPhoneNumber(callbackData);
            CurrentUser currentUser = users.get(chatId);
            if (addingUser != null) {
                UserEntity adminUser = userService.getUserById(currentUser.getManagingUserId());
                if (adminUser != null) {
                    Set<UserEntity> operators = adminUser.getOperators();
                    operators.add(addingUser);
                    adminUser.setOperators(operators);
                    userService.save(adminUser);

                    sendTextMessage(OPERATOR_ASSIGNED_TEXT, chatId);
                    currentUser.setState(State.ADMIN);
                    users.put(chatId, currentUser);
                    identifiedAdmin(chatId);
                } else {
                    sendTextMessage(BOT_WAS_RELOADED_TEXT, chatId);
                    currentUser.setState(State.ADMIN);
                    users.put(chatId, currentUser);
                    identifiedAdmin(chatId);
                }
            } else {
                sendTextMessage(SELECTED_UNKNOWN_USER, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный ответ");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    public List<UserEntity> getUsersForParking(List<UserEntity> allUsers, ParkingEntity parking) {
        List<UserEntity> usersForParking = new ArrayList<>();

        for (UserEntity user : allUsers) {
            Set<ParkingEntity> userParkings = user.getParkings();
            if (userParkings != null && userParkings.contains(parking)) {
                usersForParking.add(user);
            }
        }

        return usersForParking;
    }

    private void obtainAdminOperatorsManageResult(String callbackData, String chatId, SendMessage message, ResourceBundle bundle) {
        UserEntity user = userService.getUserByPhoneNumber(callbackData);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        CurrentUser currentUser = users.get(chatId);
        currentUser.setManagingUserId(user.getId());

        if (user.getParkings().isEmpty()) {
            message.setText(USER_NOT_ASSIGNED_PARKING_TEXT);
            currentUser.setState(State.ADMIN_WAITING_OPERATORS_MANAGING);
            users.put(chatId, currentUser);
            sendMessage(message, chatId);
        } else {
            for (ParkingEntity parking : user.getParkings()) {
                InlineKeyboardButton button = new InlineKeyboardButton(parking.getName());
                button.setCallbackData(parking.getId().toString());
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);
            }
            currentUser.setState(State.ADMIN_WAITING_MANAGE_PARKING);
            users.put(chatId, currentUser);
            // Add rows to the keyboard
            markup.setKeyboard(keyboard);
            message.setText(SELECTED_REMOVING_PARKING_TEXT);
            message.setReplyMarkup(markup);
            sendMessage(message, chatId);
        }
    }

    private void obtainAdminOperatorsManageParkingResult(String callbackData, String chatId) {
        try {
            ParkingEntity parking = parkingService.getParkingById(Long.valueOf(callbackData));
            if (parking != null) {
                CurrentUser currentUser = users.get(chatId);
                UserEntity managingOperator = userService.getUserById(currentUser.getManagingUserId());
                if (managingOperator != null) {
                    Set<ParkingEntity> parkingEntities = managingOperator.getParkings();
                    parkingEntities.remove(parking);
                    managingOperator.setParkings(parkingEntities);
                    userService.save(managingOperator);
                    sendTextMessage(OPERATOR_REMOVED_TEXT, " - " + parking.getName(), chatId);
                    currentUser.setState(State.ADMIN);
                    users.put(chatId, currentUser);
                    identifiedAdmin(chatId);
                } else {
                    sendTextMessage(SELECTED_UNKNOWN_USER, chatId);
                }
            } else {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainAdminOperatorsParkingResult(String callbackData, String chatId, Map<Long, ParkingEntity> parkings, ResourceBundle bundle) {
        try {
            if (parkings == null || parkings.isEmpty()) {
                parkings = parkingService.getAllParkingsAsMap();
            }

            callbackData = callbackData.trim();
            ParkingEntity parking = getParkingByName(parkings, callbackData);

            if (parking != null) {
                CurrentUser currentUser = users.get(chatId);
                currentUser.setState(State.ADMIN);

                UserEntity operator = userService.getUserById(currentUser.getAddingOperatorId());
                if (operator != null) {
                    Set<ParkingEntity> parkingEntities = operator.getParkings();
                    parkingEntities.add(parking);
                    operator.setParkings(parkingEntities);
                    userService.save(operator);
                    sendTextMessage(OPERATOR_ASSIGNED_PARKING_TEXT, " - " + parking.getName(), chatId);
                } else {
                    sendTextMessage(SELECTED_UNKNOWN_USER, chatId);
                }
                identifiedAdmin(chatId);
            } else {
                sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать выбранный объект");
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }
    }

    private void obtainCriticality(String callbackData, String chatId) {
        try {
            CurrentUser currentUser = users.get(chatId);
            CriticalityLevel criticalityLevel = CriticalityLevel.valueOf(callbackData);
            Ticket ticket = currentUser.getTicket();
            ticket.setCriticalityLevel(criticalityLevel);
            currentUser.setTicket(ticket);

            sendTextMessage(SELECTED_CRITICALITY_LEVEL_TEXT, " - " + criticalityLevel.getRussianValue(), chatId);

            sendTextMessage(TYPE_TICKET_COMMENT_TEXT, chatId);
//            message.setText(TYPE_PROBLEM_AREA);
//            if (currentUser.getTicket() != null && currentUser.getTicket().getCriticalityLevel() != null) {
//                if (currentUser.getTicket().getCriticalityLevel().equals(CriticalityLevel.EVENT)) {
//                    createKeyboardMarkup(message, events, currentUser.getUser().getLanguageCode().toString());
//                } else if (currentUser.getTicket().getCriticalityLevel().equals(CriticalityLevel.INCIDENT)) {
//                    createKeyboardMarkup(message, incidents, currentUser.getUser().getLanguageCode().toString());
//                } else {
//                    createKeyboardMarkup(message, problemAreas, currentUser.getUser().getLanguageCode().toString());
//                }
//            }
//            sendMessage(message, chatId);
            currentUser.setState(State.WAITING_DESCRIPTION);
            users.put(chatId, currentUser);
        } catch (Exception e) {
            log.error("obtainCriticality - " + e.getMessage());
            sendTextMessage(CHOOSE_CORRECT_OPTION_TEXT, chatId);
        }

    }

    public ReplyKeyboardMarkup backButton(ResourceBundle bundle) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(bundle.getString("BACK_BUTTON"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup cancelButton(ResourceBundle bundle) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(bundle.getString("CANCEL_BUTTON"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void endOfQuestionnaire(String chatId) {
        String ticketUrl = "";
        String asanaTicketUrl ="";

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("HTML");

        CurrentUser currentUser = users.get(chatId);
        UserEntity user = currentUser.getUser();

        currentUser.setUser(user);
        currentUser.setState(State.START);
        userRepository.save(user);
        Ticket ticket = currentUser.getTicket();
        ticket.setUser(user);
        ticket.setOrderId(createOrderId());

        List<ServiceGroupEntity> serviceGroups = parkingService.getAllGroups();

        TicketEntity ticketEntity = ticketService.saveTicket(ticket);
        String issueId = youTrackService.createIssue(ticketEntity);
        String asanaIssueId = asanaService.createIssue(ticketEntity);

        StringBuilder msgBuilder = new StringBuilder();

        if (issueId != null) {
            ticketEntity.setYouTrackIssueId(issueId);
            ticketUrl = "https://youtrack.parqour.com/issue/" + issueId;
            ticket.setTicketUrl(ticketUrl);

            msgBuilder.append("YouTrack: ").append(ticketUrl).append("\n");

        } else {
            msgBuilder.append(TICKET_NOT_CREATED_ERROR).append("\n");
        }

        if (asanaIssueId != null) {
            ticketEntity.setAsanaIssueId(asanaIssueId);
            asanaTicketUrl = "https://app.asana.com/0/" + projectId + "/" + asanaIssueId;
            ticket.setAsanaTicketUrl(asanaTicketUrl);

            msgBuilder.append("Asana: ").append(asanaTicketUrl).append("\n");
        }

        ticketEntity.setCommentsUpdatedTime(null);
        ticketService.save(ticketEntity);

        if (serviceGroups.isEmpty()) {
            sendTextMessage(TICKET_SEND_ERROR_TEXT, chatId);
        } else {
            for (ServiceGroupEntity serviceGroup : serviceGroups) {
                if (serviceGroup.getGroupChatId() != null) {
                    String groupChatId = serviceGroup.getGroupChatId().toString();


                    String ticketTextToServiceGroup = groupMessagingService.createTicketInfoToServiceGroup(ticket).toString();
                    sendMessage.setText(ticketTextToServiceGroup);
                    sendMessage(sendMessage, groupChatId); //Send to Support group chat
                    sendTextMessage(TICKET_SEND_GROUP_TEXT, " " + ticketUrl, chatId);
                    sendTextMessage(TICKET_SEND_GROUP_TEXT, " " + asanaTicketUrl, chatId);

                } else {
                    sendTextMessage(TICKET_SEND_ERROR_TEXT, chatId);
                }
            }
        }

        ParkingEntity parking = ticketEntity.getParking();

        if (parking != null && parking.getGroupChatId() != null) {
            String parkingGroupChatId = parking.getGroupChatId().toString();
            ResourceBundle bundle = getBundleFromParking(parking);

            String ticketTextToParkingGroup = groupMessagingService.createTicketInfoToParkingGroup(ticket, bundle, parking.getLanguageCode().toString()).toString();
            sendMessage.setText(ticketTextToParkingGroup);
            sendMessage(sendMessage, parkingGroupChatId); //Send to Parking group chat
        }

        if (ticket.getCriticalityLevel().equals(CriticalityLevel.INCIDENT)) {
            sendTicketToParkingAdmins(parking, sendMessage);
        }

        if (msgBuilder.isEmpty()) {
            msgBuilder.append("Не удалось создать тикеты.").append("\n");
        }
        sendTextMessage(msgBuilder.toString(), chatId);

        sendMessage(identifiedUser(currentUser), chatId);
        users.put(chatId, currentUser);
    }

    private void sendTicketToParkingAdmins(ParkingEntity parking, SendMessage ticket) {
        try {
            List<UserEntity> parkingUsers = getUsersForParking(userService.getAlLUsers(), parking);
            for (UserEntity parkingUser : parkingUsers) {
                if (parkingUser.getRole() != null && parkingUser.getRole().equals(Role.PARKING_ADMIN)) {
                    sendMessage(ticket, parkingUser.getChatId());
                }
            }
        } catch (Exception e) {
            log.error("Не удалось отправить тикет админам парковок");
        }
    }

    private CurrentUser identifyCurrentUser(String chatId) {
        UserEntity user = userRepository.findByChatId(chatId).orElse(new UserEntity());
        CurrentUser currentUser = users.get(chatId);

        if (currentUser == null) {
            currentUser = new CurrentUser();
            currentUser.setChatId(chatId);
            currentUser.setState(State.START);
        }
        if (currentUser.getState() == null) {
            currentUser.setState(State.START);
            sendTextMessage(MAIN_MESSAGE, chatId);
        }

        currentUser.setUser(user);

        users.put(chatId, currentUser);
        return currentUser;
    }

    public ParkingEntity getParkingByName(Map<Long, ParkingEntity> parkings, String parkingName) {
        for (ParkingEntity parking : parkings.values()) {
            if (parking.getName().equalsIgnoreCase(parkingName)) {
                return parking; // Found the matching ParkingEntity
            }
        }
        return null; // No matching ParkingEntity found
    }

    public SendMessage operatorsButtonRedirectorLogic(ResourceBundle bundle) {
        message.setText(TYPE_PHONE_NUMBER_TEXT);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setSwitchInlineQueryCurrentChat("");
        button.setText(bundle.getString("TAP_TO_SEARCH_TEXT"));
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        keyboard.add(row);

        // Add rows to the keyboard
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        return message;
    }

    public void buttonRedirectorLogic(SendMessage message, String chatId, ResourceBundle bundle) {
        message.setText(TYPE_PARKING_NAME_TEXT);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setSwitchInlineQueryCurrentChat("");
        button.setText(bundle.getString("TAP_TO_SEARCH_TEXT"));
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        keyboard.add(row);

        // Add rows to the keyboard
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        sendMessage(message, chatId);
    }

    public String createOrderId() {
        String ts = String.valueOf(System.currentTimeMillis());
        int randomNumber = random.nextInt(10);
        return ts + randomNumber;
    }

    private static JsonNode parseAsJson(String callback) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(callback);
        } catch (Exception e) {
            log.error("Can not parse callback - " + e.getMessage());
            return null;
        }
    }

    private String getKeyFromValue(String text) {

        for (String key : bundleRu.keySet()) {
            if (bundleRu.getString(key).equals(text)) {
                return key;
            }
        }

        for (String key : bundleEn.keySet()) {
            if (bundleEn.getString(key).equals(text)) {
                return key;
            }
        }
        return null; // Return null if no matching key is found
    }

    private ResourceBundle getBundleFromUser(UserEntity user) {
        if (user != null && user.getLanguageCode() != null) {
            String languageCode = user.getLanguageCode().toString(); // Convert to lower case for case-insensitive comparison
            if (languageCode.equals("ru")) {
                return bundleRu;
            } else {
                return bundleEn;
            }
        } else {
            // Handle the case where user or user's language code is null
            return bundleEn; // or any default bundle you prefer
        }
    }

    private ResourceBundle getBundleFromLanguageCode(String languageCode) {
        if (languageCode.equals("ru")) {
            return bundleRu;
        } else {
            return bundleEn;
        }
    }

    private ResourceBundle getBundleFromParking(ParkingEntity parking) {
        if (parking != null && parking.getLanguageCode() != null) {
            String languageCode = parking.getLanguageCode().toString(); // Convert to lower case for case-insensitive comparison
            if (languageCode.equals("ru")) {
                return bundleRu;
            } else {
                return bundleEn;
            }
        } else {
            // Handle the case where user or user's language code is null
            return bundleRu; // or any default bundle you prefer
        }
    }

    private String getValueOrDefault(String value) {
        return value != null ? value : NOT_IDENTIFIED_TEXT;
    }
}
