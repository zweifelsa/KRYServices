package ch.zweifel.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by samuel on 17.05.17.
 */
public class BackendVerticle extends AbstractVerticle {

    private static final String PATH_SERVICES = "/services";

    private ServiceManager serviceManager;

    @Override
    public void start() {
        serviceManager = ServiceManager.getInstance(vertx);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        //register handlers
        router.get(PATH_SERVICES).handler(this::servicesGet);
        router.post(PATH_SERVICES).handler(this::servicesPost);
        router.delete(PATH_SERVICES + "/:id").handler(this::servicesDelete);

        server.requestHandler(router::accept).listen(1111);
    }

    private void servicesGet(RoutingContext routingContext) {
        routingContext.response().end(serviceManager.getAllServicesJson());
    }

    private void servicesPost(RoutingContext routingContext) {
        routingContext.request().bodyHandler(buffer -> {
            JsonObject json = new JsonObject(buffer.toString());
            serviceManager.addService(json.getString("name"), json.getString("url"));
            routingContext.response().setStatusCode(200).end();
        });
    }

    private void servicesDelete(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            serviceManager.deleteService(id);
        }
        routingContext.response().setStatusCode(204).end();    }
}
