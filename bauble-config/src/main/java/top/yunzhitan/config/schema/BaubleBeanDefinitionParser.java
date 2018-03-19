package top.yunzhitan.config.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class BaubleBeanDefinitionParser implements BeanDefinitionParser {

    private Class<?> beanClass;

    public BaubleBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return null;
    }
}
