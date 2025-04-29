package com.parqour.bot.Service.Impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.parqour.bot.Service.YouTrackService;
import com.parqour.bot.Singleton.*;
import com.parqour.bot.entity.TicketEntity;
import com.parqour.bot.entity.UserEntity;
import com.parqour.bot.response.BundleResponse;
import com.parqour.bot.response.BundleValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTrackServiceImpl implements YouTrackService {
    private static final String BASE_URL = "https://youtrack.parqour.com/api";
    private static final String TOKEN = "perm:c3VwcG9ydC1ib3Q=.NTYtMQ==.xGnS2sHbzeOkcjqIUNYkAJ3wnfuVNU";
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public boolean getProjects(){
        String apiUrl = BASE_URL + "/admin/projects?fields=id,name,shortName";

        // Create a POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        HttpEntity<String> request = new HttpEntity<>(headers);

        // Create the RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);


        log.info("Get projects response - \n" + response);
        return false;
    }

    @Override
    public boolean getIssues(){
        String apiUrl = BASE_URL + "/issues/SD-1281?fields=$type,id,summary,customFields($type,id,projectCustomField($type,id,field($type,id,name)),value($type,avatarUrl,buildLink,color(id),fullName,id,isResolved,localizedName,login,minutes,name,presentation,text))";

        // Create a POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        HttpEntity<String> request = new HttpEntity<>(headers);

        // Create the RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);


        log.info("Get Issues response - \n" + response);
        return false;
    }

    @Override
    public List<String> getCustomFields(){
        String apiUrl = BASE_URL + "/admin/projects/0-7/customFields/134-41?fields=bundle(values(name))";
        // Create a POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        HttpEntity<String> request = new HttpEntity<>(headers);

        // Create the RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Make the request
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, String.class);

            BundleResponse bundleResponse = objectMapper.readValue(response.getBody(), BundleResponse.class);
            List<String> parkingNames = new ArrayList<>();
            for (BundleValue value : bundleResponse.getBundle().getValues()) {
                parkingNames.add(value.getName());
            }

            log.info("Get Custom fields response status code- \n" + response.getStatusCode());
            return parkingNames;
        }catch (Exception e){
            log.error("Get Custom fields error - " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String createIssue(TicketEntity ticket) {
        try {
            // Create the Issue object
            log.info("Create Issue - " + ticket.toString());
            String apiUrl = BASE_URL + "/issues";
            Issue issue = new Issue();
            String ticketDescription = ticket.getDescription();
            if (ticketDescription != null) {
                if (!ticketDescription.isBlank() && ticketDescription.length() > 50) {
                    issue.setSummary(ticket.getParking().getName() + " " + ticket.getProblemArea() + " " + ticketDescription.substring(0, 49) + "...");
                }else {
                    issue.setSummary(ticket.getParking().getName() + " " + ticket.getProblemArea() + " " + ticketDescription);
                }
            }else {
                issue.setSummary(ticket.getParking().getName() + " " + ticket.getProblemArea());
            }
            issue.setId(ticket.getOrderId());


            StringBuilder description = new StringBuilder();
            UserEntity user = ticket.getUser();

            description.append("Описание проблемы:").append("\n")
                    .append(getValueOrDefault(ticket.getDescription())).append("\n");
            description.append("Уровень критичности: ").append(getValueOrDefault(ticket.getCriticalityLevel().getRussianValue())).append("\n");
            description.append("Создано пользователем: ").append("\n");
            description.append("User/Chat id: ").append(getValueOrDefault(user.getChatId())).append("\n");
            description.append("Роль: ").append(getValueOrDefault(user.getRole().getRussianValue())).append("\n");
            description.append("Телеграм: @").append(getValueOrDefault(user.getUsername())).append("\n");
            description.append("Ticket id - ").append(ticket.getOrderId());

            issue.setDescription(description.toString());

            issue.setType("Issue");

            JSONObject project = new JSONObject();
            project.put("id", "0-7");
            project.put("type", "Project");
            issue.setProject(project);


            List<CustomField> customFields = new ArrayList<>();

            ///1
            CustomField issueTypeCustomField = new CustomField();
            issueTypeCustomField.setId("134-39");

            ProjectCustomField issueTypeProjectCustomField = new ProjectCustomField();
            issueTypeProjectCustomField.setId("134-39");

            Field issueTypeField = new Field();
            issueTypeField.setId("111-1");
            issueTypeField.setName("Тип задачи");
            issueTypeField.setType("CustomField");

            issueTypeProjectCustomField.setField(issueTypeField);
            issueTypeProjectCustomField.setType("EnumProjectCustomField");


            Value issueTypeValue = new Value();
            issueTypeValue.setName(ticket.getProblemArea());  //Тип задачи
            issueTypeValue.setType("EnumBundleElement");

            issueTypeCustomField.setProjectCustomField(issueTypeProjectCustomField);
            issueTypeCustomField.setValue(issueTypeValue);
            issueTypeCustomField.setType("SingleEnumIssueCustomField");

            //3
            CustomField parkingCustomField = new CustomField();
            parkingCustomField.setId("134-41");

            ProjectCustomField parkingProjectCustomField = new ProjectCustomField();
            parkingProjectCustomField.setId("134-41");

            Field parkingField = new Field();
            parkingField.setId("111-4");
            parkingField.setName("Название объекта");
            parkingField.setType("CustomField");

            parkingProjectCustomField.setField(parkingField);
            parkingProjectCustomField.setType("OwnedProjectCustomField");


            Value parkingValue = new Value();
            parkingValue.setName(ticket.getParking().getName());  //Название объекта
            parkingValue.setType("OwnedBundleElement");

            parkingCustomField.setProjectCustomField(parkingProjectCustomField);
            parkingCustomField.setValue(parkingValue);
            parkingCustomField.setType("SingleOwnedIssueCustomField");


//            customFields.add(issueTypeCustomField);
            customFields.add(parkingCustomField);

            issue.setCustomFields(customFields);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(TOKEN);


            Gson gson = new Gson();
            String jsonPayload = gson.toJson(issue);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            log.info("Create Issue Request - " + request);

            // Create the RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Make the request
            ResponseEntity<Issue> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    Issue.class);

            log.info("Create issue Response - " + response);

            // Check the response status code
            if (response.getStatusCodeValue() > 199 && response.getStatusCodeValue() < 205) {
                log.info("Ticket created successfully. Issue ID: " + response.getBody());
                if(response.getBody()!=null && response.getBody().getId()!=null) {
                    return response.getBody().getId();
                }
            } else {
                log.error("Failed to create ticket. Error: " + response.getBody());
            }
            return null;
        }catch (Exception e){
            log.error("Не удалось создать тикет - " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getIssueStatus(String issueId){
        try {
            String fieldId = "134-40";
            String apiUrl = BASE_URL + "/issues/" + issueId + "/customFields/" + fieldId + "?fields=value(id,name)";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(TOKEN);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Create the RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Make the request
            ResponseEntity<CustomField> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, CustomField.class);
            response.getStatusCode();
            if (response.getStatusCode().value() == 200) {
                if (response.getBody()!= null && response.getBody().getValue() != null) {
                    return response.getBody().getValue().getName();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public List<IssueComment> getIssueComments(String issueId){
        try {
            String apiUrl = BASE_URL + "/issues/" + issueId + "/comments?fields=id,author(login,name,id),created,deleted,text,updated";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(TOKEN);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Create the RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Make the request
            ResponseEntity<IssueComment[]> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, IssueComment[].class);
            response.getStatusCode();
            if (response.getStatusCode().value() == 200) {
                if (response.getBody()!= null) {
                    return List.of(response.getBody());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e){
            return null;
        }
    }

    private String getValueOrDefault(String value) {
        if (value != null && !value.isBlank()){
            return value;
        }else {
            return "Не указано";
        }
    }
}
