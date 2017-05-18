package ch.zweifel.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Samuel Zweifel on 17.05.17.
 */
public class Service {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_OK_X = "OK (3XX)";
    public static final String STATUS_INVALID = "INVALID";
    public static final String STATUS_DOWN = "DOWN";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm";

    private String id;
    private String name;
    private String url;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String status = "";
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(using=JsonDateSerializer.class)
    @JsonDeserialize(using=JsonDateDeserializer.class)
    private Date lastCheck;

    @JsonIgnore
    private int position;

    public Service() {
    }

    public Service(String id, String name, String url){
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
            this.status = status;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    public String getLastCheckString() {
        if(lastCheck != null) {
            return new SimpleDateFormat(DATE_PATTERN).format(lastCheck);
        }
        return "";
    }

    public void setLastCheck(Date date) {
        this.lastCheck = date;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public static class JsonDateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeString(new SimpleDateFormat(DATE_PATTERN).format(date));
        }
    }

    public static class JsonDateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try {
                return new SimpleDateFormat(DATE_PATTERN).parse(p.getValueAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
