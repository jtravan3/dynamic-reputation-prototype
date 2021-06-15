package com.jtravan.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jtravan.components.ConfigurationService;
import com.jtravan.components.EntryPoint;
import com.jtravan.model.Configuration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    private final ConfigurationService configurationService;

    @Autowired
    public StatusEndpoints(@NonNull EntryPoint entryPoint,
                           @NonNull ConfigurationService configurationService) {
        this.entryPoint = entryPoint;
        this.configurationService = configurationService;
    }

    @GetMapping(value = "health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String health() throws JsonProcessingException {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "ok");
        return objectWriter.writeValueAsString(healthStatus);
    }

    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    public String info() throws JsonProcessingException {
        Map<String, String> info = getRunInfoMap();
        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("description", applicationDescription);
        return objectWriter.writeValueAsString(info);
    }

    @GetMapping(value = "start", produces = MediaType.APPLICATION_JSON_VALUE)
    public String start() throws JsonProcessingException {
        Configuration configuration = configurationService.getConfiguration();
        configuration.setIsExecutionLive(true);
        configurationService.setConfiguration(configuration);
        entryPoint.run();
        return objectWriter.writeValueAsString(getRunInfoMap());
    }

    @GetMapping(value = "stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public String stop() throws JsonProcessingException {
        Configuration configuration = configurationService.getConfiguration();
        configuration.setIsExecutionLive(false);
        configurationService.setConfiguration(configuration);
        return objectWriter.writeValueAsString(getRunInfoMap());
    }

    @PostMapping(value = "update", consumes = "application/json", produces = "application/json")
    public String updateConfiguration(@RequestBody @NonNull Configuration configuration) throws JsonProcessingException {
        configurationService.setConfiguration(configuration);
        return objectWriter.writeValueAsString(getRunInfoMap());
    }

    private Map<String,String> getRunInfoMap() {
        Configuration configuration = configurationService.getConfiguration();
        Map<String, String> info = new HashMap<>();
        info.put("isExecutionLive", String.valueOf(configuration.getIsExecutionLive()));
        info.put("conflictingPercentage", String.valueOf(configuration.getConflictingPercentage()));
        info.put("abortPercentage", String.valueOf(configuration.getAbortPercentage()));
        info.put("recalculationPercentage", String.valueOf(configuration.getRecalculationPercentage()));
        info.put("totalAffectedTransactions", String.valueOf(configuration.getTotalAffectedTransactions()));
        info.put("totalTransactionsExecuted", String.valueOf(configuration.getTotalTransactionsExecuted()));
        info.put("percentageAffected", String.valueOf(configurationService.getPercentageAffected()));
        info.put("minimumTransactionsInTheSystem", String.valueOf(configuration.getMinimumTransactionsInTheSystem()));
        info.put("transactionThreshold", String.valueOf(configuration.getTransactionThreshold()));
        return info;
    }
}


