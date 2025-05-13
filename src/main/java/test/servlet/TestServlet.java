package test.servlet;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import test.ejb.CallbackIF;

/**
 * This class illustrates WebServlet annotation.
 *
 * @author Shing Wai Chan
 */
@WebServlet(name = "TestServlet", urlPatterns = {"/servlet_vehicle"})
public class TestServlet extends HttpServlet {

    //@Resource(name = "url/webServer")
    URL url;
    @EJB(beanName = "StatelessBean")
    CallbackIF callbackBean;

    @Resource(lookup = "java:app/AppName")
    String appName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("TestServlet init");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        try(ObjectInputStream objInStream = new ObjectInputStream(new BufferedInputStream(req.getInputStream()))) {
            System.out.println("ServletVehicle - got InputStream");
            Properties properties = (Properties) objInStream.readObject();
            System.out.println("read properties!!!");

            // create an instance of the test client and run here
            String testClassName = properties.getProperty("test_classname");
            String[] arguments = (String[]) objInStream.readObject();

            PrintWriter writer = res.getWriter();
            writer.println("Ran test_classname: " + testClassName);
            writer.println("Ran callbackBean.echo: " + callbackBean.echo(testClassName));
            writer.println("Injected appName: " + appName);
            writer.println("Ran callbackBean.getAppName: " + callbackBean.getAppName());
            writer.flush();
            res.setStatus(200);
        } catch (Exception e) {
            throw new ServletException("IO error", e);
        }
    }
}
