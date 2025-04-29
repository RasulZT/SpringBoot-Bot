package com.parqour.bot.webhook;

import com.parqour.bot.Components.ParkingsUpdater;
import com.parqour.bot.Service.AsanaService;
import com.parqour.bot.Service.TicketService;
import com.parqour.bot.entity.TicketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asana")
@RequiredArgsConstructor
@Slf4j
public class AsanaWebhookController {
    private final TicketService ticketService;
    private final AsanaService asanaService;
    private final ParkingsUpdater parkingsUpdater;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleAsanaWebhook(
            @RequestHeader(value = "X-Hook-Secret", required = false) String hookSecret,
            @RequestBody(required = false) Map<String, Object> requestBody
    ) {
        // Подтверждение вебхука
        if (hookSecret != null) {
            log.info("Asana webhook handshake received");
            return ResponseEntity.ok()
                    .header("X-Hook-Secret", hookSecret)
                    .body("OK");
        }

        log.info("Asana webhook event received: {}", requestBody);

        // Отправка добавленного комментария в сервис
        processWebhookEvents(requestBody);

        return ResponseEntity.ok("OK");
    }

    private void processWebhookEvents(Map<String, Object> payload) {
        List<Map<String, Object>> events = (List<Map<String, Object>>) payload.get("events");
        if (events == null) {
            log.warn("No 'events' found in Asana webhook payload");
            return;
        }

        for (Map<String, Object> event : events) {
            String action = (String) event.get("action");
            Map<String, Object> resource = (Map<String, Object>) event.get("resource");
            Map<String, Object> parent = (Map<String, Object>) event.get("parent");

            if (resource != null
                    && "story".equals(resource.get("resource_type"))
                    && "comment_added".equals(resource.get("resource_subtype"))
                    && "added".equals(action)
            ) {
                // New comment
                if (parent != null) {
                    String parentTaskGid = (String) parent.get("gid");
                    log.info("New comment on task GID: {}", parentTaskGid);

                    StringBuilder text = asanaService.updateAsanaComment(parentTaskGid);

                    if (text != null && !text.isEmpty()) {
                        TicketEntity ticket = ticketService.findByAsanaIssueId(parentTaskGid);
                        if (ticket != null && ticket.getParking() != null) {
                            Long chatId = ticket.getParking().getGroupChatId();
                            parkingsUpdater.sendMessageAboutTicketUpdates(String.valueOf(text), chatId);
                        } else {
                            log.warn("Ticket or parking not found for parentTaskGid={}. Cannot get chatId.", parentTaskGid);
                        }
                    }
                }
            }
        }
    }
}
