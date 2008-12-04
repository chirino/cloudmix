/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.cloudmix.common.dto;

import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.1 $
 */
public class JaxbTest extends TestCase {
    protected final transient Log log = LogFactory.getLog(getClass());
    protected JAXBContext jaxbContext;
    private String jaxwsProvider;

    public void testMachineDetails() throws Exception {
        AgentDetails details = new AgentDetails();
        details.setHostname("localhost");
        details.setSystemProperties(new HashMap<String, String>());
        details.process().feature("activeMQBroker", "mvn:org.apache.activemq:activemq-broker:5.0.0");
        details.process().feature("serviceMixKernel", "mvn:org.apache.servicemix:kernel:1.0");
        dump(details);
    }

    public void testFeatureProfile() throws Exception {
        FeatureDetails details = new FeatureDetails();
        details.setId("serviceMixKernel");
        details.depends("activeMQBroker");
        dump(details);
    }

    public void testDetailsWithEndpoints() throws Exception {
        AgentDetails details = new AgentDetails();
        details.setHostname("localhost");
        details.setSystemProperties(new HashMap<String, String>());
        details.addEndpoint("urn:{http://cxf.apache.org}SoapPort", 
                            getEPR("http://tempuri.org/foo/bar"));
        details.addEndpoint("urn:Bank::Account/12345",
                            getEPR("corbaname:rir/NameService#account_12345"));
        dump(details);
    }

    public void testDetailsWithRemovedEndpoints() throws Exception {
        AgentDetails details = new AgentDetails();
        details.setHostname("localhost");
        details.setSystemProperties(new HashMap<String, String>());
        details.addEndpoint("urn:{http://cxf.apache.org}SoapPort", 
                            getEPR("http://tempuri.org/foo/bar"));
        details.addEndpoint("urn:Bank::Account/12345",
                            getEPR("corbaname:rir/NameService#account_12345"));
        details.removeEndpoint("urn:{http://cxf.apache.org}SoapPort"); 
	details.removeEndpoint("urn:Bank::Account/12345");
        dump(details);
    }

    protected void dump(Object dto) throws Exception {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter buffer = new StringWriter();
        marshaller.marshal(dto, buffer);
        log.info("Created: " + buffer);
    }


    protected Object parseUri(String uri) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        URL resource = getClass().getResource(uri);
        assertNotNull("Cannot find resource on the classpath: " + uri, resource);
        Object value = unmarshaller.unmarshal(resource);
        return value;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jaxbContext = JAXBContext.newInstance("org.fusesource.cloudmix.common.dto");
        setJaxwsProvider();
    }

    @Override
    protected void tearDown() throws Exception {
        unsetJaxwsProvider();
    }

    protected W3CEndpointReference getEPR(String address) {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        return builder.build();
    }

    private void setJaxwsProvider() {
        jaxwsProvider = System.getProperty("javax.xml.ws.spi.Provider");
        System.setProperty("javax.xml.ws.spi.Provider", 
                           "org.apache.cxf.jaxws.spi.ProviderImpl");
    }

    private void unsetJaxwsProvider() {
        if (jaxwsProvider != null) {
            System.setProperty("javax.xml.ws.spi.Provider", jaxwsProvider);
        }
    }
}
