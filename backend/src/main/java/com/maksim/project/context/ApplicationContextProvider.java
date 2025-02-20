package com.maksim.project.context;

import org.springframework.beans.BeansException;
import org.springframework.context.*;

public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.context = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}