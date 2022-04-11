package com.dotmarketing.osgi.restart;

import com.dotcms.rest.config.RestServiceUtil;
import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

/**
 * Adds a post hook to the contentlet api and listening a sub/pub msg
 * @author jsanca
 */
public class Activator extends GenericBundleActivator {

    private final Class clazz = MyOsgiResource.class;
    private LoggerContext pluginLoggerContext;

    @SuppressWarnings ("unchecked")
    public void start ( BundleContext context ) throws Exception {

        //Initializing log4j...
        final LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(),
                false,
                dotcmsLoggerContext,
                dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices ( context );
        //Adding hooks
        RestServiceUtil.addResource(clazz);
    }

    public void stop ( BundleContext context ) throws Exception {

        unregisterServices( context );
        Logger.info(this.getClass().getName(), "Removing new Restful Service:" + clazz.getSimpleName());
        RestServiceUtil.removeResource(clazz);
        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}
