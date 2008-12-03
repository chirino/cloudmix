package org.fusesource.cloudmix.agent.resources;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.TestCase;

import org.fusesource.cloudmix.agent.EndpointRegistry;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.common.EndpointRefBuilder;
import org.fusesource.cloudmix.agent.dir.DirectoryInstallerAgent;
import org.fusesource.cloudmix.agent.webapp.GridAgentWebapp;
import org.fusesource.cloudmix.common.dto.AgentDetails;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class AgentResourceTest extends TestCase {

    private static final String AGENT_URI = "http://localhost:8080/agent";
    private IMocksControl control;
    private AgentResource resource;
    private GridAgentWebapp webapp;
    private DirectoryInstallerAgent agent; 
    private AgentDetails details;
    private RestGridClient client;
    private UriInfo uriInfo;
    private ServletConfig config;

    public void testAddEndpoint() throws Exception {
        AgentResource resource = setUpResource();
        String id = "urn:{http://cxf.apache.org}SoapPort";
        String encodedId = "urn:%7Bhttp:%2F%2Fcxf.apache.org%7DSoapPort";
        W3CEndpointReference ref = 
            EndpointRefBuilder.create("http://tempuri.org/foo/bar");
        String agentId = "localhost_agent_1";
        setUpWebapp(agentId);

        details.addEndpoint(id, ref);
        EasyMock.expectLastCall();
        client.updateAgentDetails(agentId, details);
        EasyMock.expectLastCall();
        uriInfo.getAbsolutePath();
        EasyMock.expectLastCall().andReturn(new URI(AGENT_URI + "/endpoint/" + encodedId));
        control.replay();
        
        verifyResponse(resource.addEndpoint(id, ref), 201);
       
        control.verify();
    }

    public void testRemoveEndpoint() throws Exception {
        AgentResource resource = setUpResource();
        String id = "urn:{http://cxf.apache.org}SoapPort";
        String agentId = "localhost_agent_1";
        setUpWebapp(agentId);

        details.removeEndpoint(id);
        EasyMock.expectLastCall().andReturn(true);
        client.updateAgentDetails(agentId, details);
        EasyMock.expectLastCall();
        control.replay();
        
        verifyResponse(resource.removeEndpoint(id), 200);
       
        control.verify();
    }

    public void testRemoveNonExistentEndpoint() throws Exception {
        AgentResource resource = setUpResource();
        String id = "urn:{http://cxf.apache.org}SoapPort";
        setUpWebapp(null);

        details.removeEndpoint(id);
        EasyMock.expectLastCall().andReturn(false);
        control.replay();
        
        verifyResponse(resource.removeEndpoint(id), 404);
       
        control.verify();
    }
    
    public void testInit() throws Exception {
         AgentResource resource = setUpResource();
        webapp.init(config);
        EasyMock.replay(webapp);

        resource.init();

        EasyMock.verify(webapp);
    }

    public void testDestroy() throws Exception {
         AgentResource resource = setUpResource();
        webapp.destroy();
        EasyMock.replay(webapp);

        resource.destroy();

        EasyMock.verify(webapp);
    }

    public void testGetStatus() throws Exception {
        AgentResource resource = setUpResource();
        webapp.getStatus();
        EasyMock.expectLastCall().andReturn("<html></html>");
        control.replay();
        
        assertEquals("<html></html>", resource.getStatus());
       
        control.verify();
    }

    public void testGetImage() throws Exception {
        AgentResource resource = setUpResource();
        String image = "logo.gif";
        ServletContext context = control.createMock(ServletContext.class);
        InputStream is = control.createMock(InputStream.class);
        config.getServletContext();
        EasyMock.expectLastCall().andReturn(context);
        context.getResourceAsStream("images/" + image);
        EasyMock.expectLastCall().andReturn(is);
        control.replay();
        
        verifyResponse(resource.getImage(image), 200);
       
        control.verify();
    }

    public void testStyleSheet() throws Exception {
        AgentResource resource = setUpResource();
        String css = "css/main.css";
        ServletContext context = control.createMock(ServletContext.class);
        InputStream is = control.createMock(InputStream.class);
        config.getServletContext();
        EasyMock.expectLastCall().andReturn(context);
        context.getResourceAsStream(css);
        EasyMock.expectLastCall().andReturn(is);
        control.replay();
        
        verifyResponse(resource.getStyleSheet(), 200);
       
        control.verify();
    }

    public void testAddEndpointAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method addEndpoint = 
           cls.getDeclaredMethod("addEndpoint",
                                 String.class,
                                 W3CEndpointReference.class);
        assertNotNull(addEndpoint.getAnnotation(PUT.class));
        Path path = addEndpoint.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("endpoint/{id}", path.value());
        Consumes cm = addEndpoint.getAnnotation(Consumes.class);
        assertNotNull(cm);
        assertEquals(1, cm.value().length);
        assertEquals("application/xml", cm.value()[0]);
        Annotation ann = addEndpoint.getParameterAnnotations()[0][0];
        assertTrue(ann instanceof PathParam);
        assertEquals("id", ((PathParam)ann).value());
    }

    public void testRemoveEndpointAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method removeEndpoint = 
           cls.getDeclaredMethod("removeEndpoint", String.class);
        assertNotNull(removeEndpoint.getAnnotation(DELETE.class));
        Path path = removeEndpoint.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("endpoint/{id}", path.value());
        Annotation ann = removeEndpoint.getParameterAnnotations()[0][0];
        assertTrue(ann instanceof PathParam);
        assertEquals("id", ((PathParam)ann).value());
    }

    public void testGetStatusAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method getStatus = cls.getDeclaredMethod("getStatus");
        assertNotNull(getStatus.getAnnotation(GET.class));
        Path path = getStatus.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("status", path.value());
        Produces pm = getStatus.getAnnotation(Produces.class);
        assertNotNull(pm);
        assertEquals(1, pm.value().length);
        assertEquals("text/html", pm.value()[0]);
    }

    public void testGetImageAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method getImage = cls.getDeclaredMethod("getImage", String.class);
        assertNotNull(getImage.getAnnotation(GET.class));
        Path path = getImage.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("images/{image}", path.value());
        Produces pm = getImage.getAnnotation(Produces.class);
        assertNotNull(pm);
        assertEquals(1, pm.value().length);
        assertEquals("image/gif", pm.value()[0]);
        Annotation ann = getImage.getParameterAnnotations()[0][0];
        assertTrue(ann instanceof PathParam);
        assertEquals("image", ((PathParam)ann).value());
    }

    public void testGetStyleSheetAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method getStyleSheet = cls.getDeclaredMethod("getStyleSheet");
        assertNotNull(getStyleSheet.getAnnotation(GET.class));
        Path path = getStyleSheet.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("css/main.css", path.value());
        Produces pm = getStyleSheet.getAnnotation(Produces.class);
        assertNotNull(pm);
        assertEquals(1, pm.value().length);
        assertEquals("text/xml", pm.value()[0]);
    }


    private AgentResource setUpResource() {
        AgentResource resource = new AgentResource();
        control = EasyMock.createNiceControl();
        webapp = control.createMock(GridAgentWebapp.class);
        uriInfo = control.createMock(UriInfo.class);
        config = control.createMock(ServletConfig.class);
        resource.setGridAgentWebapp(webapp);
        resource.setUriInfo(uriInfo);
        resource.setConfig(config);

        agent = control.createMock(DirectoryInstallerAgent.class);
        details = control.createMock(AgentDetails.class);
        client = control.createMock(RestGridClient.class);

        EndpointRegistry endpointRegistry = new EndpointRegistry();
        endpointRegistry.setClient(client);
        endpointRegistry.setAgent(agent);
        resource.setEndpointRegistry(endpointRegistry);
        return resource;
    }

    private void setUpWebapp(String agentId) throws Exception {
        webapp.getAgent();
        EasyMock.expectLastCall().andReturn(agent).anyTimes();
        agent.getAgentDetails();
        EasyMock.expectLastCall().andReturn(details).anyTimes();
        if (agentId != null) {
            webapp.getClient();
            EasyMock.expectLastCall().andReturn(client).anyTimes();
            agent.getAgentId();
            EasyMock.expectLastCall().andReturn(agentId);
        }
    }

    private void verifyResponse(Response resp, int status) {
        assertNotNull(resp);
        // assert expected Response.getStatus() when service-grid
        // upgraded to more recent version of JAX-RS
    }
}
