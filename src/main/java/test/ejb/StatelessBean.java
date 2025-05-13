package test.ejb;

import jakarta.annotation.Resource;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

@Stateless(name = "StatelessBean")
@Remote({ CallbackIF.class })
public class StatelessBean implements CallbackIF {
    @Resource(lookup = "java:app/AppName")
    String appName;

    @Override
    public String echo(String info) {
        return "StatelessBean.echo(" + info + ")";
    }

    @Override
    public String getAppName() {
        return appName;
    }
}
