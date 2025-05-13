package test.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.ejb.CallbackIF;
import test.ejb.StatelessBean;
import test.servlet.TestServlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Properties;

@ExtendWith(ArquillianExtension.class)
public class ClientTest {

    @Deployment(name = "client_war", order = 2)
    public static EnterpriseArchive createDeploymentVehicle() {
        // War
        // the war with the correct archive name
        WebArchive client_war = ShrinkWrap.create(WebArchive.class, "client_war.war");
        // The class files
        client_war.addClasses(TestServlet.class, CallbackIF.class, StatelessBean.class);
        // The web.xml descriptor
        URL warResURL = ClientTest.class.getResource("/web.xml");
        client_war.addAsWebInfResource(warResURL, "web.xml");
        // The sun-web.xml descriptor
        warResURL = ClientTest.class.getResource("/sun-web.xml");
        client_war.addAsWebInfResource(warResURL, "sun-web.xml");

        // The application.xml descriptor
        URL earURL = ClientTest.class.getResource("/application.xml");
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "application.ear");
        ear.addAsManifestResource(earURL, "application.xml");
        ear.addAsModule(client_war);

        return ear;
    }

    @Test
    @RunAsClient
    public void testSendObjectStream() throws Exception {
        // Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        Properties p = System.getProperties();
        p.setProperty("test_classname", "test.servlet.SomeTestClass");
        String[] argv = {"arg1", "arg2"};
        String contentType = "java-internal/" + p.getClass().getName();
        // connection.connect();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(baos);
        objOut.writeObject(p);
        objOut.writeObject(argv);
        objOut.flush();
        objOut.close();

        // Binary data to send
        byte[] binaryData = baos.toByteArray();

        // Build the HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/client-root/servlet_vehicle"))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(binaryData))
                .build();
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response status code and body
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        Assertions.assertEquals(200, response.statusCode());

        Optional<String> xAppNameOpt = response.headers().firstValue("X-AppName");
        Assertions.assertTrue(xAppNameOpt.isPresent());
        System.out.println("X-AppName: " + xAppNameOpt.get());
        Assertions.assertEquals("descriptor-appname", xAppNameOpt.get());
    }
}
