package top.yunzhitan.config.support;

import com.yunzhitan.event.ProviderEvent;
import com.yunzhitan.rpc.RegistryLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.Executor;

public class ProviderBean implements ApplicationContextAware,InitializingBean,ApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(ProviderBean.class);

    private int weight;         //权重
    private Executor executor;  //服务私有的线程池
    private Object   serviceImpl;  //实际的服务对象

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.publishEvent(new ProviderEvent(new Object()));
    }

    @Override
    public void afterPropertiesSet() {
        RegistryLocal registry =
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

    }
}
