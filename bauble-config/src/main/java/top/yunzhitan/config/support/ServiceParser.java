package top.yunzhitan.config.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServiceParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String interfaceName = element.getAttribute("interfaceName");
        String ref = element.getAttribute("ref");

        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(ServiceConfig.class);
        beanDefinition.setLazyInit(false);
        beanDefinition.getPropertyValues().addPropertyValue("interfaceName",interfaceName);
        beanDefinition.getPropertyValues().addPropertyValue("ref",ref);

        parserContext.getRegistry().registerBeanDefinition(interfaceName,beanDefinition);
        return beanDefinition;
    }
}
