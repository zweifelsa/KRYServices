package ch.zweifel.services;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by samuel on 17.05.17.
 */
public class ServiceManager {

    private static final String FILE = "./services.json";
    private static final String SERVICES = "services";
    private static ServiceManager serviceManager;

    // ensure synchronized access
    private Vertx vertx;
    private Map<String, Service> services = new HashMap<>();

    public static ServiceManager getInstance(Vertx vertx) {
        if(serviceManager == null) {
            serviceManager = new ServiceManager();
        }
        serviceManager.vertx = vertx;
        serviceManager.loadFile();
        return serviceManager;
    }

    public Service[] getAllServices() {
        return services.values().toArray(new Service[services.size()]);
    }

    public String getAllServicesJson() {
        JsonArray jsonList = new JsonArray();
        for(Service service: services.values()) {
            jsonList.add(JsonObject.mapFrom(service));
        }
        JsonObject wrapper = new JsonObject();
        wrapper.put("services", jsonList);
        return wrapper.encodePrettily();
    }

    public void addService(String name, String url) {
        //TODO duplicates?
        String id = UUID.randomUUID().toString();
        Service service = new Service(id, name, url);
        services.put(id, service);
        writeToFile();
    }

    public void deleteService(String id) {
        services.remove(id);
        writeToFile();
    }

    // synchronized
    private synchronized void loadFile() {
        Buffer buffer = vertx.fileSystem().readFileBlocking(FILE);
        services.clear();
        new JsonObject(buffer.toString()).getJsonArray(SERVICES).forEach(object -> {
            if(object instanceof JsonObject) {
                JsonObject json = (JsonObject) object;
                Service service = json.mapTo(Service.class);
                services.put(service.getId(), service);
            }
        });
    }

    // synchronized
    private synchronized void writeToFile() {
        JsonArray jsonList = new JsonArray();
        for(Service service: services.values()) {
            jsonList.add(JsonObject.mapFrom(service));
        }
        JsonObject wrapper = new JsonObject();
        wrapper.put("services", jsonList);
        vertx.fileSystem().writeFileBlocking(FILE, Buffer.buffer(getAllServicesJson()));
    }
}
