package org.fusesource.cloudmix.agent.bundle;

import java.util.jar.Manifest;

import org.fusesource.cloudmix.agent.EndpointRegistry;

import org.apache.servicemix.kernel.testing.support.AbstractIntegrationTest;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class EndpointRegistryRetrievalTest extends AbstractIntegrationTest {

    private static final String CLASS_NAME = EndpointRegistry.class.getName();

    static {
        System.setProperty("servicemix.startLocalConsole", "false");
        System.setProperty("servicemix.startRemoteShell", "false");
    }

    private EndpointRegistry endpointRegistry;
    private ServiceReference endpointRegistryRef;

    @Override
    protected String[] getTestBundlesNames() {
        return new String[] {
                getBundle("org.apache.felix", "org.apache.felix.prefs"),
                getBundle("org.apache.felix", "org.apache.felix.prefs"),
                getBundle("org.apache.servicemix.kernel.jaas", "org.apache.servicemix.kernel.jaas.config"),
                getBundle("org.apache.servicemix.kernel.gshell", "org.apache.servicemix.kernel.gshell.core"),
                getBundle("org.apache.servicemix.kernel.gshell", "org.apache.servicemix.kernel.gshell.features"),
                getBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"),
                getBundle("org.apache.geronimo.specs", "geronimo-activation_1.1_spec"),
                getBundle("org.apache.geronimo.specs", "geronimo-javamail_1.4_spec"),
                getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.saaj-api-1.3"),
                getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.stax-api-1.0"),
                getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jaxb-api-2.1"),
                getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jaxws-api-2.1"),
                getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.asm"),
                getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"),
                getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-codec"),
                getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-impl"),
                getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.mina"),
                getBundle("org.apache.servicemix.grid", "org.apache.servicemix.grid.jersey.wrapper"),
                getBundle("org.apache.servicemix.grid", "org.fusesource.cloudmix.common"),
                getBundle("org.apache.servicemix.grid", "org.fusesource.cloudmix.agent"),
                getBundle("org.apache.servicemix.grid", "org.fusesource.cloudmix.agent.smx4"),
        };
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"/org/fusesource/cloudmix/agent/bundle/endpoint-registry-retrieval-test.xml"};
    }

    @Override
    protected Manifest getManifest() {
        Manifest mf = super.getManifest();
        String imports = 
            mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
        mf.getMainAttributes().putValue(Constants.IMPORT_PACKAGE,
                                        imports + ",javax.xml.ws.wsaddressing,com.sun.xml.bind.v2");
        return mf;
    }

    @Override
    protected void onTearDown() throws Exception {
        super.onTearDown();
        endpointRegistryRef = null;
    }

    public void testEndpointRegistryExplicitLookup() throws Exception {
        verifyReference(bundleContext.getServiceReference(CLASS_NAME));
    }

    public void testEndpointRegistryServiceTracker() throws Exception {
        ServiceTracker tracker = null;
        try {
            tracker = new ServiceTracker(bundleContext, CLASS_NAME, null) {
                @Override
                public Object addingService(final ServiceReference reference) {
                    Object result = super.addingService(reference);
                    endpointRegistryRef = reference;
                    return result;
                }
            };
            tracker.open();
            Thread.sleep(2 * 1000);
        } finally {
            verifyReference(endpointRegistryRef);
            if (tracker != null) {
                tracker.close();
            }
        }
    }

    public void testEndpointRegistryInjection() throws Exception {
        verifyReference(endpointRegistry);
    }

    public void setEndpointRegistryOSGiService(EndpointRegistry ref) {
        endpointRegistry = ref;
    }

    private void verifyReference(ServiceReference endpointRegistryRef) {
        assertNotNull(endpointRegistryRef);
        Object o = bundleContext.getService(endpointRegistryRef);
        assertNotNull(o);
        assertTrue(o instanceof EndpointRegistry);
        EndpointRegistry registry = (EndpointRegistry)o;
        verifyReference(registry);
    }

    private void verifyReference(EndpointRegistry registry) {
        assertFalse(registry.removeEndpoint("urn:{http://cxf.apache.org}SoapPort"));
    }
}
