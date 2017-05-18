package ch.zweifel.services.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zweifel.services.Service;

/**
 * Created by samuel on 18.05.17.
 */

public class ServiceRequest {
        private static final String URL = "http://192.168.1.109:1111/services/";
        private String method;
        private Service service;

        public ServiceRequest(String method) {
            this.method = method;
        }
        public ServiceRequest(Service service, String method)  {
            this.service = service;
            this.method = method;
        }

        public Service getService() {
            return service;
        }

        public String getUrl() {
            return URL + ((method.equals("DELETE")) ? service.getId(): "");
        }

        public String getMethod() {
            return method;
        }

        public String getBodyAsString() {
            if(service != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.writeValueAsString(this.service);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }
    }
