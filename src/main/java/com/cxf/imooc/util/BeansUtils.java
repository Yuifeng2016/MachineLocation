package com.cxf.imooc.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/5 9:24
 * @description：spring util
 */
@Component
public class BeansUtils  implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeansUtils.context = applicationContext;
    }

    public static   <T> T getBean(Class<T> bean) {
        return context.getBean(bean);
    }
    public  static  <T> T getBean(String var1, @Nullable Class<T> var2){
        return context.getBean(var1, var2);
    }

    public static   ApplicationContext getContext() {
        return context;
    }

}
