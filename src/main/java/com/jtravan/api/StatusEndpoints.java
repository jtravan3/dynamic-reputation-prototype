package com.jtravan.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jtravan.components.EntryPoint;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint for determining the status and API information. These endpoints are not included
 * within the FHIR Swagger
 */
@RestController
@RequestMapping("/")
public class StatusEndpoints {

    @Value("${info.app.name}")
    private String applicationName;

    @Value("${info.app.description}")
    private String applicationDescription;

    @Value("${info.app.version}")
    private String applicationVersion;

    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final EntryPoint entryPoint;

    @Autowired
    public StatusEndpoints(@NonNull EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    @GetMapping(value = "health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String health() throws JsonProcessingException {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "ok");
        return objectWriter.writeValueAsString(healthStatus);
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    public String info() throws JsonProcessingException {
        Map<String, String> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("description", applicationDescription);
        return objectWriter.writeValueAsString(info);
    }

    @GetMapping(value = "start", produces = MediaType.APPLICATION_JSON_VALUE)
    public String start() throws JsonProcessingException {
        entryPoint.setExecutionLive(true);
        entryPoint.run();
        Map<String, String> info = new HashMap<>();
        info.put("started", "true");
        return objectWriter.writeValueAsString(info);
    }

    @GetMapping(value = "stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public String stop() throws JsonProcessingException {
        entryPoint.setExecutionLive(false);
        Map<String, String> info = new HashMap<>();
        info.put("started", "false");
        return objectWriter.writeValueAsString(info);
    }
}


