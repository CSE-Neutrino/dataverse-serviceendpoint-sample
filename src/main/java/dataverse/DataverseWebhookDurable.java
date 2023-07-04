package dataverse;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.microsoft.durabletask.*;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;

/**
 * Please follow the below steps to run this durable function sample
 * 1. Send an HTTP GET/POST request to endpoint `StartHelloCities` to run a
 * durable function
 * 2. Send request to statusQueryGetUri in `StartHelloCities` response to get
 * the status of durable function
 * For more instructions, please refer https://aka.ms/durable-function-java
 * 
 * Please add com.microsoft:durabletask-azure-functions to your project
 * dependencies
 * Please add `"extensions": { "durableTask": { "hubName": "JavaTestHub" }}` to
 * your host.json
 */
public class DataverseWebhookDurable {
    /**
     * This HTTP-triggered function starts the orchestration.
     */
    @FunctionName("AccountUpdated")
    public HttpResponseMessage startOrchestration(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET, HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            final ExecutionContext context) {

        context.getLogger().info("AccountUpdated");
        DurableTaskClient client = durableContext.getClient();

        // create event object
        DataverseEvent dataverseEvent = new DataverseEvent(DataverseEventType.AccountUpdated, request.getBody().orElse("[]"));

        // Start the orchestration.
        String instanceId = client.scheduleNewOrchestrationInstance("DataverseEventOrchestrator", dataverseEvent);

        // Return the instance id in the response.
        return durableContext.createCheckStatusResponse(request, instanceId);
    }

    /**
     * This is the orchestrator function. The OrchestrationRunner.loadAndRun()
     * static
     * method is used to take the function input and execute the orchestrator logic.
     */
    @FunctionName("DataverseEventOrchestrator")
    public String dataverseEventOrchestrator(@DurableOrchestrationTrigger(name = "taskOrchestrationContext") TaskOrchestrationContext ctx) {

        DataverseEvent input = ctx.getInput(DataverseEvent.class);
        final int maxAttempts = 3;
        final Duration firstRetryInterval = Duration.ofSeconds(5);
        RetryPolicy policy = new RetryPolicy(maxAttempts, firstRetryInterval);
        TaskOptions taskOptions = new TaskOptions(policy);
        return ctx.callActivity("DataverseEventPersistor", input, taskOptions, String.class).await();
    }

    /**
     * This is the activity function that gets invoked by the orchestration.
     */
    @FunctionName("DataverseEventPersistor")
    public String handleAccountUpdated(@DurableActivityTrigger(name = "dataverseEvent") DataverseEvent dataverseEvent,
            final ExecutionContext context) {

        final String tableName = "DataverseEvents";
        String connectionString = System.getenv("AzureWebJobsStorage");

        // Create a TableServiceClient with a connection string.
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Create the table if it not exists.
        TableClient tableClient = tableServiceClient.createTableIfNotExists(tableName);

        // Create a new employee TableEntity.
        String partitionKey = dataverseEvent.getEventType().toString();
        String rowKey = UUID.randomUUID().toString();
        Map<String, Object> eventInfo = new HashMap<>();
        eventInfo.put("EventPayload", dataverseEvent.getEventPayload());
        eventInfo.put("Created", Instant.now().toString());
        TableEntity eventRow = new TableEntity(partitionKey, rowKey).setProperties(eventInfo);

        // Upsert the entity into the table
        tableClient.upsertEntity(eventRow);

        return rowKey;
    }
}