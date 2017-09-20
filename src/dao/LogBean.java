package dao;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Entity
public class LogBean {

    @Id
    private String id;

    @Property
    private String timestamp;

    @Property
    private String key;

    @Property
    private long duration;

    public String toString() {
        return timestamp + " | " + key + " | " + duration;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
