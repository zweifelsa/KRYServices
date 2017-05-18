import ch.zweifel.services.BackendVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by samuel on 17.05.17.
 */
@RunWith(VertxUnitRunner.class)
public class BackendTests {
    private static final String FILE = "./services.json";
    private static final String JSON = "{\n" +
            "  \"services\":\n" +
            "    [\n" +
            "      {\n" +
            "        \"id\": \"07a9953d-6604-4968-8bd1-df33a075980a\",\n" +
            "        \"name\": \"test service\",\n" +
            "        \"url\": \"https://kry.se\",\n" +
            "        \"status\": \"OK\",\n" +
            "        \"lastCheck\": \"2014-06-16 16:42\"\n" +
            "      }\n" +
            "    ]\n" +
            "}";
    private Vertx vertx;

    @Before
    public void setup(TestContext context) {
        vertx = Vertx.vertx();
        vertx.fileSystem().writeFileBlocking(FILE, Buffer.buffer(JSON));
        vertx.deployVerticle(new BackendVerticle(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServicesGet(TestContext context) throws Exception {
        Async async = context.async();

        HttpClient client = vertx.createHttpClient();
        client.getNow(1111, "localhost", "/services", response -> {
            response.bodyHandler(buffer -> {
                context.assertEquals(JSON, buffer.toString());
                async.complete();
            });
        });
    }

    @Test
    public void testServicesPost(TestContext context) throws Exception {
        Async async = context.async();

        JsonObject json = new JsonObject();
        json.put("name", "Vert.x");
        json.put("url", "http://vertx.io");

        WebClient client = WebClient.create(vertx);
        client.post(1111, "localhost", "/services").sendJsonObject(json, response -> {
            context.assertTrue(response.succeeded());
            async.complete();
        });
    }

    @Test
    public void testServicesDelete(TestContext context) throws Exception {
        Async async = context.async();

        HttpClient client = vertx.createHttpClient();
        client.delete(1111, "localhost", "/services/07a9953d-6604-4968-8bd1-df33a075980a", response -> {
                context.assertEquals(204, response.statusCode());
                async.complete();
        }).end();
    }
}
