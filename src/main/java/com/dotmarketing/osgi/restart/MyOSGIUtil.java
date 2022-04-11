package com.dotmarketing.osgi.restart;

import com.dotcms.dotpubsub.DotPubSubEvent;
import com.dotcms.dotpubsub.DotPubSubProvider;
import com.dotcms.dotpubsub.DotPubSubProviderLocator;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;
import org.apache.felix.framework.OSGIUtil;

public class MyOSGIUtil {

    // PUBSUB
    private final static  String TOPIC_NAME = OsgiRestartTopic.OSGI_RESTART_TOPIC;
    private final  DotPubSubProvider pubsub;
    private final OsgiRestartTopic osgiRestartTopic;

    public static MyOSGIUtil getInstance() {
        return OSGIUtilHolder.instance;
    }

    private static class OSGIUtilHolder{
        private static MyOSGIUtil instance = new MyOSGIUtil();
    }

    private MyOSGIUtil() {

        this.pubsub           = DotPubSubProviderLocator.provider.get();
        this.osgiRestartTopic = new OsgiRestartTopic();
        Logger.debug(this.getClass(), "Starting hook with PubSub on OSGI");

        this.pubsub.start();
        this.pubsub.subscribe(this.osgiRestartTopic);
    }

    /**
     * Restart the current instance and notify the rest of the nodes in the cluster that restart is needed
     */
    public void restartOsgiClusterWide() {

        restartOsgiOnlyLocal();
        Logger.debug(MyOSGIUtil.class.getName(), ()-> "Sending a PubSub Osgi Restart event");

        final DotPubSubEvent event = new DotPubSubEvent.Builder ()
                .addPayload("sourceNode", APILocator.getServerAPI().readServerId())
                .withTopic(TOPIC_NAME)
                .withType(OsgiRestartTopic.EventType.OGSI_RESTART_REQUEST.name())
                .build();

        pubsub.publish(event);
    }

    /**
     * Do the restart only for the current node (locally)
     */
    public void restartOsgiOnlyLocal() {

        //Remove Portlets in the list
        OSGIUtil.getInstance().portletIDsStopped.stream().forEach(APILocator.getPortletAPI()::deletePortlet);
        Logger.info(MyOSGIUtil.class.getName(), "Portlets Removed: " + OSGIUtil.getInstance().portletIDsStopped.toString());

        //Remove Actionlets in the list
        if (null  != OSGIUtil.getInstance().workflowOsgiService) {
            OSGIUtil.getInstance().actionletsStopped.stream().forEach(OSGIUtil.getInstance().workflowOsgiService::removeActionlet);
            Logger.info(this, "Actionlets Removed: " + OSGIUtil.getInstance().actionletsStopped.toString());
        }

        //Cleanup lists
        OSGIUtil.getInstance().portletIDsStopped.clear();
        OSGIUtil.getInstance().actionletsStopped.clear();

        //First we need to stop the framework
        OSGIUtil.getInstance().stopFramework();

        //Now we need to initialize it
        OSGIUtil.getInstance().initializeFramework();
    }
}
