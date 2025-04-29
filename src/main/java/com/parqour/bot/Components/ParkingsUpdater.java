package com.parqour.bot.Components;

import com.parqour.bot.Enums.IssueStatusClosed;
import com.parqour.bot.Enums.TicketSection;
import com.parqour.bot.Enums.YouTrackIssueStatus;
import com.parqour.bot.MatchingBot;
import com.parqour.bot.Service.*;
import com.parqour.bot.Model.AsanaIssueComment;
import com.parqour.bot.Enums.AsanaIssueStatus;
import com.parqour.bot.Singleton.IssueComment;
import com.parqour.bot.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParkingsUpdater {
    private final YouTrackService youTrackService;
    private final ParkingService parkingService;
    private final MatchingBot matchingBot;
    private final TicketService ticketService;
    private final UserService userService;
    private final AsanaService asanaService;
    private final List<String> issueClosedStatuses = List.of(IssueStatusClosed.ALL);
    private final List<String> issueStatusesAll = List.of(YouTrackIssueStatus.ALL);
    private final List<String> asanaIssueStatusesAll = List.of(AsanaIssueStatus.ALL);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZoneId zoneId = TimeZone.getTimeZone("UTC").toZoneId();
    private final LocalDateTime serviceStartedTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
    private ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru"));
    private ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en"));

    @Value("${asana.project-id}")
    private String asanaProjectId;

    //Using updating
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours // Run every 10 seconds
    public void updateParkings() {
        List<String> parkings = youTrackService.getCustomFields();
        if (!parkings.isEmpty()) {
            parkings.removeIf(str -> str.equals("Выберете парковку"));
            parkingService.updateParkingNames(parkings);
        }

    }

    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours // Run every 10 seconds
    public void updateAsanaParkings() {
        List<String> parkings = asanaService.getCustomFieldsString();
        if (!parkings.isEmpty()) {
            parkings.removeIf(str -> str.equals("Выберете парковку"));
            parkingService.updateParkingNames(parkings);
        }
    }

    @Scheduled(fixedRate = 10 * 1000) // Run every 2 hours // Run every 10 seconds
    public void updateIssues() {
        List<TicketEntity> asanaTickets = ticketService.getAsanaTicketsForUpdate();

        boolean sectionChanged;
        AsanaIssueStatus newStatus = null;
        TicketSection newSection = null;

        for (TicketEntity asanaTicket : asanaTickets) {
            sectionChanged = false;

            if (asanaTicket.getParking() != null) {
                String issueId = asanaTicket.getAsanaIssueId();

                AsanaIssueStatus asanaTicketCurrentStatus = asanaTicket.getAsanaIssueStatus();
                TicketSection currentSection = asanaTicket.getSection();

                log.info("Current section: {}, for asana task: {}", currentSection, asanaTicket.getAsanaIssueId());
                log.info("Current status: {}, for asana task: {}", asanaTicketCurrentStatus, asanaTicket.getAsanaIssueId());

                // Обработка статуса
                String asanaStatus = asanaService.getIssueStatus(issueId);
                if (asanaStatus != null) {
                    newStatus = AsanaIssueStatus.getByValue(asanaStatus);

                    if (asanaStatus.equals("COMPLETED")) {
                        asanaTicket.setTicketClosed(true);
                    } else if (asanaStatus.equals("NOT_COMPLETED")) {
                        asanaTicket.setTicketClosed(false);
                    }

                    if (newStatus != null && newStatus != asanaTicketCurrentStatus) {
                        log.info("Status changed from {} to {} for Asana task: {}", asanaTicketCurrentStatus, newStatus, asanaTicket.getAsanaIssueId());
                        asanaTicket.setAsanaIssueStatus(newStatus);
                    }
                } else {
                    log.warn("Asana returned null status, marking ticket as closed for task: {}", asanaTicket.getAsanaIssueId());
                    asanaTicket.setTicketClosed(true);
                }

                // 🔹 Обработка секции
                String newSectionStr = asanaService.getIssueSection(issueId);
                if (newSectionStr != null) {
                    newSectionStr = newSectionStr.replaceAll("\\s*-\\s*", "-");

                    newSection = TicketSection.getByValue(newSectionStr);
                    if (newSection != null && newSection != currentSection && newSection.isWorkSection()) {
                        log.info("Section changed from {} to {} for Asana task: {}", currentSection, newSection, asanaTicket.getAsanaIssueId());
                        sectionChanged = true;
                        asanaTicket.setSection(newSection);
                    }
                }

                if (sectionChanged) {
                    String messageText = buildTicketUpdateMessage(asanaTicket, newSection);
                    log.info("Sending section update message for Asana task: {}", asanaTicket.getAsanaIssueId());
                    sendMessageAboutTicketUpdates(messageText, asanaTicket.getParking().getGroupChatId());
                }
            }
            log.debug("Saving ticket changes for Asana task: {}", asanaTicket.getAsanaIssueId());
            ticketService.saveAsanaTicket(asanaTicket);
        }
    }

    public void sendMessageAboutTicketUpdates(String text, Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            sendMessage.enableHtml(true);
            sendMessage.setParseMode("HTML");
            matchingBot.sendMessage(sendMessage, chatId.toString());
        } catch (Exception e) {
            log.error("sendMessageToParkingGroup error - " + e.getMessage());
        }
    }

    private StringBuilder createIssueCommentsTicketInfo(TicketEntity ticket, List<AsanaIssueEntity> comments, ResourceBundle bundle) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String asanaTicketUrl = "https://app.asana.com/0/" + asanaProjectId + "/" + ticket.getAsanaIssueId();

            stringBuilder.append("<b>📩")
                    .append(bundle.getString("added_comment_ticket"))
                    .append(" ID:")
                    .append(ticket.getOrderId())
                    .append("</b>\n");

            stringBuilder.append("<a href=\"")
                    .append(asanaTicketUrl)
                    .append("\">")
                    .append(bundle.getString("ticket_link"))
                    .append("</a>\n");

            String issueComments = issueCommentsText(comments);

            if (issueComments == null) {
                return null;
            }

            stringBuilder.append("\n");
            stringBuilder.append(issueComments).append("\n");

            return stringBuilder;
        } catch (Exception e) {
            log.error("Can not createIssueCommentsTicketInfo - " + e.getMessage());
            return null;
        }
    }

    private String buildTicketUpdateMessage(TicketEntity ticket, TicketSection newSection) {
        String asanaTicketUrl = "https://app.asana.com/0/" + asanaProjectId + "/" + ticket.getAsanaIssueId();

        StringBuilder message = new StringBuilder();

        message.append("🔄 Обновлён раздел тикета ID: ").append(ticket.getAsanaIssueId()).append("\n");
        message.append("Объект: ").append(ticket.getParking().getName()).append("\n");

        if (newSection != null) {
            message.append("📌 Статус: ").append(newSection.getRussianValue()).append("\n");
        }

        message.append("\n🔗 <a href=\"").append(asanaTicketUrl).append("\">Ссылка на тикет</a>");

        return message.toString();
    }

    private String issueCommentsText(List<AsanaIssueEntity> comments) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<AsanaIssueEntity> sentComments = new HashSet<>();
        for (AsanaIssueEntity comment : comments) {
            if (!comment.isCommentSent()) {
                if (comment.getText().toLowerCase().contains("/sb")) {
                    LocalDateTime commentCreatedTime = comment.getCreatedAt();
                    if (commentCreatedTime != null) {
                        if (!commentCreatedTime.isBefore(serviceStartedTime)) {
                            stringBuilder.append(commentCreatedTime.format(dateFormatter))
                                    .append(" ").append(comment.getAuthorName())
                                    .append(": ").append("\n");
                            stringBuilder.append(comment.getText()).append("\n");
                            comment.setCommentSent(true);
                            sentComments.add(comment);
                        }
                    }
                }
            }
        }

        ticketService.saveAllAsanaComments(sentComments);
        if (!stringBuilder.isEmpty()) {
            return stringBuilder.toString();
        } else {
            return null;
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
        if (value != null && !value.isBlank()) {
            return value;
        } else {
            return "Not identified";
        }
    }
}
