package org.fusesource.cloudmix.controller.bundle;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.HttpAuthenticator;
import org.osgi.service.http.HttpService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
//import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import org.fusesource.cloudmix.common.spring.SpringServlet;

@SuppressWarnings("unused")
public class JerseyServletFactory implements ApplicationContextAware, InitializingBean {
    private static final transient Log LOG = LogFactory.getLog(JerseyServletFactory.class);

    private String classNames;
    private String rootContext;
    
    private ApplicationContext applicationContext;
    private HttpService httpService;
	private HttpAuthenticator authenticator;

    public void setJerseyClassNames(String ... names) {
        StringBuilder sb = new StringBuilder();
        
        boolean firstTime = true;
        for (String name : names) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(';');
            }
            sb.append(name);
        }
        
        classNames = sb.toString();
    }
    
    public void setRootContext(String ctx) {
        rootContext = ctx;
    }
    
    public void setHttpService(HttpService svc) throws Exception {
        httpService = svc;
    }

    public void setAuthenticator(HttpAuthenticator ca) {
    	authenticator = ca;
    }
    
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        applicationContext = ctx;
    }
    
    public void afterPropertiesSet() throws Exception {
        // This property is needed for JAXB to properly operate inside OSGi
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize","true");
        
        Hashtable<String, String> initParams = new Hashtable<String, String>();
        initParams.put("com.sun.jersey.config.feature.Redirect", "true");
        initParams.put("com.sun.jersey.config.feature.ImplicitViewables", "true");
        
        initParams.put(
            "com.sun.jersey.config.property.resourceConfigClass",
            "org.fusesource.cloudmix.controller.bundle.OSGiResourceConfig");
        initParams.put(
            "jersey_osgi.classnames",
            classNames); 
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            // TODO is there another way to do this now using the standard Jersey stuff?
            SpringServlet jerseyServlet = new SpringServlet(applicationContext, authenticator);
            //SpringServlet jerseyServlet = new SpringServlet();
            httpService.registerServlet(rootContext, jerseyServlet, initParams, null);
            LOG.info("Registered servlet at: " + rootContext);
            LOG.info("With initialization  : " + initParams);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
