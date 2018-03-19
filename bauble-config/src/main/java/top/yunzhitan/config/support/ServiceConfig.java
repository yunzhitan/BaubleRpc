package top.yunzhitan.config.support;

import com.yunzhitan.event.ServerStartEvent;
import com.yunzhitan.server.RpcServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class ServiceConfig implements ApplicationContextAware, ApplicationListener {
    private String interfaceName;
    private String ref;
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        Object serviceBean = applicationContext.getBean(ref);
        RpcServer.getInstance().getServiceBeanMap().put(interfaceName, serviceBean);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
        applicationContext.publishEvent(new ServerStartEvent(new Object()));
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}

