package dataverse;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.time.Duration;
import java.util.*;

import com.microsoft.durabletask.*;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

import dataverse.models.BBEvent;
import dataverse.models.DataverseEvent;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.google.gson.Gson;

/**
 * Azure Functions with HTTP Trigger, to be used as a webhook endpoint for
 * capturing Dataverse events.
 */
public class DataverseWebhookDurable {
    /**
     * This HTTP-triggered function starts the orchestration.
     */
    @FunctionName("DataverseEventHandler")
    public HttpResponseMessage startOrchestration(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            final ExecutionContext context) {

        context.getLogger().info("AccountUpdated");
        DurableTaskClient client = durableContext.getClient();

        Gson gson = new Gson();

        // create event object
        String jsonContent = request.getBody().orElse("{}");
        DataverseEvent dataverseEvent = gson.fromJson(jsonContent, DataverseEvent.class);
        BBEvent eventData = BBEvent.fromDataverseEvent(dataverseEvent, jsonContent);

        // Start the orchestration.
        String instanceId = client.scheduleNewOrchestrationInstance("DataverseEventOrchestrator", eventData);

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

        BBEvent input = ctx.getInput(BBEvent.class);
        
        // demo retry policy
        final int maxAttempts = 3;
        final Duration firstRetryInterval = Duration.ofSeconds(5);
        RetryPolicy policy = new RetryPolicy(maxAttempts, firstRetryInterval);
        TaskOptions taskOptions = new TaskOptions(policy);
        return ctx.callActivity("DataverseEventPersistor", input, taskOptions, String.class).await();

    }

    /**
     * This is the activity function that gets invoked by the orchestration.
     * Save event data to azure table storage
     */
    @FunctionName("DataverseEventPersistor")
    public String dataverseEventPersistor(@DurableActivityTrigger(name = "dataverseEvent") BBEvent eventData,
            final ExecutionContext context) {

        final String tableName = "DataverseEvents";
        String connectionString = System.getenv("AzureWebJobsStorage");
        context.getLogger().info("DataverseEventPersistor:" + connectionString);

        // Create a TableServiceClient with a connection string.
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Create the table if it not exists.
        tableServiceClient.createTableIfNotExists(tableName);

        // Create a TableClient with a connection string and a table name.
        TableClient tableClient = new TableClientBuilder()
                .connectionString(connectionString)
                .tableName(tableName)
                .buildClient();

        // Create a new employee TableEntity.
        String partitionKey = eventData.EntityName;
        String rowKey = UUID.randomUUID().toString();
        Map<String, Object> eventInfo = new HashMap<>();
        eventInfo.put("EntityName", eventData.EntityName);
        eventInfo.put("EntityId", eventData.EntityId);
        eventInfo.put("EventType", eventData.EventType);
        eventInfo.put("Payload", eventData.Payload);
        
        TableEntity eventRow = new TableEntity(partitionKey, rowKey).setProperties(eventInfo);

        // Upsert the entity into the table
        tableClient.upsertEntity(eventRow);

        return rowKey;
    }
}