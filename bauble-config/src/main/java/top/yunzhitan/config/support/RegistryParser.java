package top.yunzhitan.config.support;

import com.yunzhitan.protocol.RpcProtocal;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RegistryParser implements BeanDefinitionParser{

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String id            = "test";
        String serverAddress = element.getAttribute("serverAddress");
        String protocolType  = element.getAttribute("protocol");
        String registryAddress = element.getAttribute("registryAddress");

        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(RegistryConfig.class);
        beanDefinition.getPropertyValues().addPropertyValue("serverAddress", serverAddress);
        beanDefinition.getPropertyValues().addPropertyValue("protocol", protocolType);
        beanDefinition.getPropertyValues().addPropertyValue("registryAddress", registryAddress);
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

        return beanDefinition;

    }
}
