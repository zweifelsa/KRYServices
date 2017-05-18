import ch.zweifel.services.Service;
import ch.zweifel.services.ServiceManager;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by samuel on 17.05.17.
 */
@RunWith(VertxUnitRunner.class)
public class ServiceManagerTests {

    private static final String FILE = "./services.json";

    @Before
    public void setup(TestContext context) {
        String json = "{\n" +
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
        Vertx.vertx().fileSystem().writeFileBlocking(FILE, Buffer.buffer(json));
    }

    @Test
    public void addServiceTest(TestContext context) {
        ServiceManager serviceManager = ServiceManager.getInstance(Vertx.vertx());
        serviceManager.addService("Vert.x", "vertx.io");
        context.assertEquals(2, serviceManager.getAllServices().length);
    }

    @Test
    public void getAllServicesTest(TestContext context) {
        ServiceManager serviceManager = ServiceManager.getInstance(Vertx.vertx());
        context.assertEquals(2, serviceManager.getAllServices().length);
    }

    @Test
    public void deleteServiceTest(TestContext context) {
        ServiceManager serviceManager = ServiceManager.getInstance(Vertx.vertx());
        serviceManager.deleteService(serviceManager.getAllServices()[0].getId());
        context.assertEquals(0, serviceManager.getAllServices().length);
    }
}
