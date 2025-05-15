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

import javax.naming.Context;
import javax.naming.InitialContext;

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
    @Resource(lookup = "java:module/ModuleName")
    String moduleName;

    @Resource(lookup="java:app/client_war!ROOT")
    URL myWebApp;

    String beanGlobalLookup;
    String beanAppLookup;
    String beanModuleLookup;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("TestServlet init, "+myWebApp);
        try {
            InitialContext ctx = new InitialContext();
            lookup(ctx, "java:app/AppName");
            lookup(ctx, "java:module/ModuleName");
            lookup(ctx, "java:global/descriptor-appname/client_war!ROOT");
            lookup(ctx, "java:app/client_war!ROOT");
            lookup(ctx, "java:app/client_war/ROOT");
            lookup(ctx, "java:module/client_war!ROOT");
            beanGlobalLookup = lookup(ctx, "java:global/descriptor-appname/client_war/StatelessBean");
            beanAppLookup = lookup(ctx, "java:app/client_war/StatelessBean");
            beanModuleLookup = lookup(ctx, "java:module/StatelessBean");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String lookup(Context ctx, String name) {
        String result = null;
        try {
            Object obj = ctx.lookup(name);
            System.out.println("lookup: " + name + " = " + obj);
            result = obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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

            res.setHeader("X-AppName", appName);
            res.setHeader("X-ModuleName", moduleName);
            res.setHeader("X-beanGlobalLookup", beanGlobalLookup);
            res.setHeader("X-beanAppLookup", beanAppLookup);
            res.setHeader("X-beanModuleLookup", beanModuleLookup);
            PrintWriter writer = res.getWriter();
            writer.println("Ran test_classname: " + testClassName);
            writer.println("Ran callbackBean.echo: " + callbackBean.echo(testClassName));
            writer.println("Injected appName: " + appName);
            writer.println("Injected moduleName: " + moduleName);
            writer.println("Ran callbackBean.getAppName: " + callbackBean.getAppName());
            writer.flush();

            res.setStatus(200);
        } catch (Exception e) {
            throw new ServletException("IO error", e);
        }
    }
}
