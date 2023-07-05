package dataverse.models;

/*
 * Dataverse event data 
 */
public class BBEvent {

    public String EntityName;
    public String EntityId;
    public String EventType;
    public String Payload;

    public BBEvent() {
    }

    public BBEvent(String entityName, String entityId,String eventType, String eventPayload) {
        this.EntityName = entityName;
        this.EntityId = entityId;
        this.EventType = eventType;
        this.Payload = eventPayload;
    }

    public static BBEvent fromDataverseEvent(DataverseEvent dataverseEvent, String payload) {
        return new BBEvent(dataverseEvent.PrimaryEntityName,dataverseEvent.PrimaryEntityId, dataverseEvent.MessageName, payload);
    }

}
