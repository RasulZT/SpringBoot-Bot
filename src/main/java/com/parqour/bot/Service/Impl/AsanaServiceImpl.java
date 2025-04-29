package com.parqour.bot.Service.Impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.parqour.bot.Model.AsanaCustomField;
import com.parqour.bot.Model.AsanaIssueComment;
import com.parqour.bot.Model.AsanaTask;
import com.parqour.bot.Service.AsanaService;
import com.parqour.bot.Service.TicketService;
import com.parqour.bot.entity.AsanaIssueEntity;
import com.parqour.bot.entity.ParkingEntity;
import com.parqour.bot.entity.TicketEntity;
import com.parqour.bot.response.AsanaTaskResponse;
import com.parqour.bot.response.IssueCommentsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsanaServiceImpl implements AsanaService {
    @Value("${asana.url}")
    private String BASE_URL;

    @Value("${asana.token}")
    private String TOKEN;

    @Value("${asana.workspace-id}")
    private String workspaceId;

    @Value("${asana.project-id}")
    private String projectId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime serviceStartedTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
    private ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru"));
    private ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en"));

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final TicketService ticketService;

    @Override
    public String getProjects() {
        String apiUrl = BASE_URL + "/projects";

        try {
            RestTemplate restTemplate = createRestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            log.info("Get project response: \n{}", response);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error while getting project response: {}", e.getMessage());
        }
        return "";
    }

    @Override
    public boolean getTasks() {
        String apiUrl = BASE_URL + "/tasks";

        try {
            RestTemplate restTemplate = createRestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            log.info("Get tasks response - \n{}", response.getBody());

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Error while getting tasks: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getCustomFieldsString() {
        String apiUrl = BASE_URL + "/projects/" + projectId + "/custom_field_settings?opt_expand=custom_field";

        try {
            RestTemplate restTemplate = createRestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            List<String> customFields = new ArrayList<>();

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            JsonNode dataList = rootNode.path("data");
            if (dataList.isArray()) {
                for (JsonNode item : dataList) {
                    JsonNode customField = item.path("custom_field");
                    if (!customField.isMissingNode()) {
                        customFields.add(customField.path("name").asText());
                    }
                }
            }

            return customFields;
        } catch (Exception e) {
            log.error("Error while getting custom fields: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Map<String, String>> getCustomFields() {
        String apiUrl = BASE_URL + "/projects/" + projectId + "/custom_field_settings?opt_expand=custom_field";

        try {
            RestTemplate restTemplate = createRestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            Map<String, Map<String, String>> customFieldsMap = new HashMap<>();

            JsonNode dataList = rootNode.path("data");
            if (dataList.isArray()) {
                for (JsonNode item : dataList) {
                    JsonNode customField = item.path("custom_field");
                    String fieldName = customField.path("name").asText();
                    String fieldGid = customField.path("gid").asText();

                    JsonNode enumOptions = customField.path("enum_options");
                    Map<String, String> enumMap = new HashMap<>();

                    enumMap.put("field_gid", fieldGid);

                    if (enumOptions.isArray()) {
                        for (JsonNode enumOption : enumOptions) {
                            String optionName = enumOption.path("name").asText();
                            String optionGid = enumOption.path("gid").asText();
                            enumMap.put(optionName, optionGid);
                        }
                    }

                    customFieldsMap.put(fieldName, enumMap);
                }
            }

            return customFieldsMap;
        } catch (Exception e) {
            log.error("Get Custom fields error: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public String createIssue(TicketEntity ticket) {
        try {
            log.info("Create Asana Issue - {}", ticket.toString());

            // Example: "[Incident] ParkingName - Ticket Id"
            String taskName = "[" + ticket.getCriticalityLevel() + "] "
                    + ticket.getParking().getName()
                    + " - " + ticket.getOrderId();

            String notes = buildNotesForAsana(ticket);

            Map<String, Map<String, String>> customFieldsMap = getCustomFields();

            Map<String, String> customFieldsFromTicket = Map.of(
                    "Type", String.valueOf(ticket.getCriticalityLevel()),
                    "Site", ticket.getParking().getName()
            );

            AsanaCustomField asanaCustomFields = new AsanaCustomField();

            for (Map.Entry<String, String> entry : customFieldsFromTicket.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue().trim();

                if (customFieldsMap.containsKey(fieldName)) {
                    Map<String, String> fieldData = customFieldsMap.get(fieldName);

                    Map<String, String> normalizedFieldData = fieldData.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().trim().toLowerCase(), Map.Entry::getValue));

                    String fieldGid = fieldData.get("field_gid");

                    log.info("Проверяем поле: '{}'", fieldName);
                    log.info("Значение из тикета: '{}'", fieldValue);
                    log.info("Доступные ключи в fieldData: {}", fieldData.keySet());

                    String optionGid = normalizedFieldData.get(fieldValue.trim().toLowerCase());

                    if (fieldGid != null && optionGid != null) {
                        asanaCustomFields.addField(fieldGid, optionGid);
                    } else {
                        log.warn("Не удалось найти GID для поля '{}' со значением '{}'", fieldName, fieldValue);
                    }
                } else {
                    log.warn("Не удалось найти настройки поля '{}' в Asana", fieldName);
                }
            }

            return createTaskInAsana(taskName, notes, asanaCustomFields);
        } catch (Exception e) {
            log.error("Error while creating task in asana: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public StringBuilder updateAsanaComment(String asanaIssueId) {
        TicketEntity ticket = new TicketEntity();
        try {
            List<AsanaIssueComment> asanaIssueComments = List.of();
            StringBuilder groupTicketText = new StringBuilder();

            ticket = ticketService.findByAsanaIssueId(asanaIssueId);

            log.info("Starting to update comment for ticket: {}", asanaIssueId);

            if (ticket.getAsanaIssueId() != null) {
                asanaIssueComments = getIssueComments(ticket.getAsanaIssueId());
                log.info("Current comments from asana: issueId - {}", ticket.getAsanaIssueId());
            }

            if (asanaIssueComments != null && !asanaIssueComments.isEmpty()) {
                ticket.setCommentsUpdatedTime(LocalDateTime.now());

                List<AsanaIssueEntity> savedComments = ticketService.saveAllAsanaComments(asanaIssueComments, ticket);

                if (savedComments != null && !savedComments.isEmpty()) {
                    ticket.getAsanaComments().addAll(savedComments);
                    log.info("Sending comments update for ticket: {}", ticket.getAsanaIssueId());
                    groupTicketText = createIssueCommentsTicketInfo(
                            ticket,
                            savedComments,
                            getBundleFromParking(ticket.getParking())
                    );
                }
            } else {
                ticket.setCommentsUpdatedTime(LocalDateTime.now());
            }

            ticketService.saveAsanaTicket(ticket);
            log.info("Finished updating ticket comment with issueId: {}", asanaIssueId);
            return groupTicketText;
        } catch (Exception e) {
            log.error("Error while updating comments for ticket: {}", ticket.getId(), e);
        }
        return null;
    }

    @Override
    public String getIssueStatus(String issueId) {
        String apiUrl = BASE_URL + "/tasks/" + issueId;

        try {
            RestTemplate restTemplate = createRestTemplate();
            ResponseEntity<AsanaTaskResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, AsanaTaskResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                AsanaTaskResponse taskResponse = response.getBody();
                if (taskResponse != null && taskResponse.getData() != null) {
                    boolean completed = taskResponse.getData().isCompleted();
                    return completed ? "COMPLETED" : "NOT_COMPLETED";
                } else {
                    log.warn("No data found for task {}", issueId);
                    return null;
                }
            } else {
                log.error("Failed to fetch task {}. HTTP status: {}", issueId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Не удалось получить статус тикета: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getIssueSection(String issueId) {
        String apiUrl = BASE_URL + "/tasks/" + issueId;

        try {
            RestTemplate restTemplate = createRestTemplate();
            ResponseEntity<AsanaTaskResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, AsanaTaskResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                AsanaTaskResponse taskResponse = response.getBody();
                if (taskResponse != null && taskResponse.getData() != null) {
                    List<AsanaTask.Membership> memberships = taskResponse.getData().getMemberships();
                    if (memberships != null && !memberships.isEmpty()) {
                        AsanaTask.Section section = memberships.get(0).getSection();
                        if (section != null) {
                            return section.getName();
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error while fetching issue section for {}: {}", issueId, e.getMessage());
            return null;
        }

    }

    @Override
    public List<AsanaIssueComment> getIssueComments(String issueId) {
        try {
            String apiUrl = BASE_URL + "/tasks/" + issueId + "/stories"; // todo: Params: fields=id,author(login,name,id),created,deleted,text,updated";

            RestTemplate restTemplate = createRestTemplate();

            // Make the request
            ResponseEntity<IssueCommentsResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, IssueCommentsResponse.class);

            if (response.getStatusCode().value() == 200) {
                IssueCommentsResponse comments = response.getBody();
                if (comments != null) {
                    log.info("Successfully fetched comments for task {}", issueId);

                    return comments.getData().stream()
                            .filter(comment -> comment.getText() != null && comment.getText().contains("/sb"))
                            .sorted(Comparator.comparing(AsanaIssueComment::getCreatedAt)) // сортировка по времени создания
                            .collect(Collectors.toList());
                } else {
                    log.warn("No comments found for task {}", issueId);
                    return null;
                }
            } else {
                log.error("Failed to fetch comments for task {}. Status code: {}", issueId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error while fetching comments for task {}: {}", issueId, e.getMessage());
            return null;
        }
    }

    private String createTaskInAsana(String taskName,
                                     String notes,
                                     AsanaCustomField customFields) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        Map<String, Object> data = new HashMap<>();
        data.put("name", taskName);
        data.put("notes", notes);
        data.put("workspace", workspaceId);
        data.put("projects", List.of(projectId));

        data.put("assignee", null);

        // Formatting the date
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAfter = today.plusDays(2);
        String dueOn = twoDaysAfter.format(DateTimeFormatter.ISO_LOCAL_DATE);

        data.put("due_on", dueOn);

        data.put("custom_fields", customFields.getFields());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestBody);

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        log.info("Create Asana Issue Request - {}", request);

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL + "/tasks",
                request,
                Map.class);

        log.info("Create Asana issue Response - {}", response);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("Ticket created successfully. Task GID: {}", response.getBody().get("gid"));
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            return responseData.get("gid").toString(); // return task GID
        } else {
            log.error("Failed to create ticket. Error: " + response.getBody());
        }

        return null;
    }

    private StringBuilder createIssueCommentsTicketInfo(TicketEntity ticket, List<AsanaIssueEntity> comments, ResourceBundle bundle) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String asanaTicketUrl = "https://app.asana.com/0/" + projectId + "/" + ticket.getAsanaIssueId();

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

    private String buildNotesForAsana(TicketEntity ticket) {
        StringBuilder sb = new StringBuilder();

        sb.append("Ticket ID: ").append(ticket.getOrderId()).append("\n");
        if (ticket.getUser() != null) {
            sb.append("Создано пользователем: @").append(ticket.getUser().getUsername()).append("\n");
        }

        sb.append("------------------------------------------------").append("\n\n");

        sb.append(ticket.getDescription()).append("\n");

        return sb.toString();
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(TOKEN);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
