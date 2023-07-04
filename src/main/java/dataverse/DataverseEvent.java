package dataverse;

/*
 * Dataverse event data 
 */
public class DataverseEvent implements java.io.Serializable  {

    DataverseEventType eventType;
    String eventPayload;

    public DataverseEvent(DataverseEventType eventType, String eventPayload) {
        this.eventType = eventType;
        this.eventPayload = eventPayload;
    }
    
    public DataverseEventType getEventType() {
        return eventType;
    }
    
    public void setEventType(DataverseEventType eventType) {
        this.eventType = eventType;
    }
    
    public String getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(String eventPayload) {
        this.eventPayload = eventPayload;
    }
}
