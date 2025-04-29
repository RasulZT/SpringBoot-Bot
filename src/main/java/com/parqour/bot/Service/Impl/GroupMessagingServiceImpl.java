package com.parqour.bot.Service.Impl;

import com.nimbusds.jose.shaded.json.JSONObject;
import com.parqour.bot.Service.GroupMessagingService;
import com.parqour.bot.Singleton.Ticket;
import com.parqour.bot.entity.DutiesEntity;
import com.parqour.bot.repository.DutyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessagingServiceImpl implements GroupMessagingService {
    private final DutyRepository dutyRepository;

    private final String ACCEPT_TICKET = "Принять";
    private final String DENY_TICKET = "Некорректный тикет";

    @Override
    public SendMessage sendMessageToGroup(Ticket ticket, String groupChatId) {

        SendMessage message = new SendMessage();
        if (ticket != null) {
            log.info("Sending ticket to group - " + ticket.toString());
            // Create the inline keyboard with buttons
            InlineKeyboardButton approveButton = new InlineKeyboardButton();
            JSONObject approveCallback = new JSONObject();
            approveCallback.put("orderId", ticket.getOrderId());
            approveCallback.put("result", "APPROVE");
//            approveCallback.put("groupChatId", groupChatId);
            approveButton.setText(ACCEPT_TICKET);
            approveButton.setCallbackData(approveCallback.toString());

            InlineKeyboardButton denyButton = new InlineKeyboardButton();
            JSONObject denyCallback = new JSONObject();
            denyCallback.put("orderId", ticket.getOrderId());
            denyCallback.put("result", "DENY");
//            denyCallback.put("groupChatId", groupChatId);
            denyButton.setText(DENY_TICKET);
            denyButton.setCallbackData(denyCallback.toString());
            return message;
        } else {
            return null;
        }
    }

    @Override
    public StringBuilder createTicketInfoToParkingGroup(Ticket ticket, ResourceBundle bundle, String languageCode) {
        try {
            List<String> duties = getDutyOperators();
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("🆕❇️🅿️ | ")
                    .append(ticket.getParking())
                    .append(" | ID:")
                    .append(ticket.getOrderId())
                    .append("\n");

            stringBuilder.append("IP: ")
                    .append("<a href=\"")
                    .append(ticket.getParkingEntity().getGoogleTableLink())
                    .append("\">")
                    .append(ticket.getParkingEntity().getIp())
                    .append("</a>")
                    .append("\n");

            stringBuilder.append("Создатель: @")
                    .append(getValueOrDefault(ticket.getUser().getUsername()))
                    .append("\n");

            stringBuilder.append("Дежурные: ");

            for (String duty : duties) {
                stringBuilder.append(duty).append(" ");
            }

            stringBuilder.append("\n");

            //stringBuilder.append(bundle.getString("creator")).append(": @").append(getValueOrDefault(ticket.getUser().getUsername())).append("\n");
            //stringBuilder.append(bundle.getString("criticality_level")).append(": ").append(getValueOrDefault(ticket.getCriticalityLevel().getLocalizedValue(languageCode))).append("\n");
            //stringBuilder.append(bundle.getString("problem_area")).append(": ").append(getValueOrDefault(ticket.getProblemArea())).append("\n");

            stringBuilder.append("<a href=\"")
                    .append(ticket.getAsanaTicketUrl())
                    .append("\">Ссылка на тикет</a>")
                    .append("\n\n");

//            stringBuilder.append("\n").append(bundle.getString("ticket_link")).append(": ").append(getValueOrDefault(ticket.getAsanaTicketUrl()));

            stringBuilder.append(getValueOrDefault(ticket.getDescription()));

            return stringBuilder;
        } catch (Exception e) {
            log.error("Can not createTicketInfo - " + e.getMessage());
            return null;
        }
    }

    @Override
    public StringBuilder createTicketInfoToServiceGroup(Ticket ticket) {
        try {
            List<String> duties = getDutyOperators();
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("🆕❇️🅿️ | ")
                    .append(ticket.getParking())
                    .append(" | ID:")
                    .append(ticket.getOrderId())
                    .append("\n");

            stringBuilder.append("IP: ")
                    .append("<a href=\"")
                    .append(ticket.getParkingEntity().getGoogleTableLink())
                    .append("\">")
                    .append(ticket.getParkingEntity().getIp())
                    .append("</a>")
                    .append("\n");

            stringBuilder.append("Создатель: @")
                    .append(getValueOrDefault(ticket.getUser().getUsername()))
                    .append("\n");

            stringBuilder.append("Дежурные: ");

            for (String duty : duties) {
                stringBuilder.append(duty).append(" ");
            }

            stringBuilder.append("\n");

            stringBuilder.append("<a href=\"")
                    .append(ticket.getAsanaTicketUrl())
                    .append("\">Ссылка на тикет</a>")
                    .append("\n\n");

            stringBuilder.append(getValueOrDefault(ticket.getDescription()));

            //stringBuilder.append("Создатель: @").append(getValueOrDefault(ticket.getUser().getUsername())).append("\n");
            //stringBuilder.append("Уровень критичности: ").append(getValueOrDefault(ticket.getCriticalityLevel().getRussianValue())).append("\n");
            //stringBuilder.append("Участок проблемы: ").append(getValueOrDefault(ticket.getProblemArea())).append("\n");

            return stringBuilder;
        } catch (Exception e) {
            log.error("Can not createTicketInfo - " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getDutyOperators() {
        LocalDateTime now = LocalDateTime.now().atZone(ZoneId.of("GMT+5")).toLocalDateTime(); // Prod
        LocalDate currentDate = now.toLocalDate();
        LocalTime workStartTime = LocalTime.of(9, 0);
        LocalTime workEndTime = LocalTime.of(18, 0);
        List<String> users = new ArrayList<>();

        // Check if it's Saturday or Sunday
        if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.info("It's a weekend, sending operators on duty");
            List<DutiesEntity> duties = dutyRepository.getAllByDutyDateEquals(currentDate);
            for (DutiesEntity duty : duties) {

                String userName = "@" + duty.getAssignedUser().getUsername();

                if (duty.getAssignedUser().getUsername() == null) {
                    userName = "+" + duty.getAssignedUser().getPhoneNumber();
                }

                if (!users.contains(userName)) {
                    users.add(userName);
                }
            }
        } else {
            // Check if it's after midnight but before work start time
            if (now.toLocalTime().isBefore(workStartTime)) {
                currentDate = currentDate.minusDays(1); // Move back to the previous day
                log.info("It's before work hours, adjusting date to previous day");
            }

            // Check if it's after work hours
            if (now.toLocalTime().isAfter(workEndTime) || now.toLocalTime().isBefore(workStartTime)) {
                log.info("Now is after work hours");
                List<DutiesEntity> duties = dutyRepository.getAllByDutyDateEquals(currentDate);
                for (DutiesEntity duty : duties) {
                    String userName = "@" + duty.getAssignedUser().getUsername();

                    if (duty.getAssignedUser().getUsername() == null) {
                        userName = "+" + duty.getAssignedUser().getPhoneNumber();
                    }

                    if (!users.contains(userName)) {
                        users.add(userName);
                    }
                }
            } else {
                log.info("It's within work hours");
                users.add("@bakhud");
                users.add("@IvanovVitaliy");
                users.add("@yerzakovichartem");
            }
        }

        if (users.isEmpty()) {
            log.info("No operators on duty found, adding default operators");
            users.add("@bakhud");
            users.add("@IvanovVitaliy");
            users.add("@yerzakovichartem");
        }

        return users;
    }

    private String getValueOrDefault(String value) {
        if (value != null && !value.isBlank()) {
            return value;
        } else {
            return "Не указано";
        }
    }
}
