package com.google.firebase.adclivecode.backend;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();
        context.log("InitListener");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
