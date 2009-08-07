/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.common;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

public class EndpointRefBuilder {
    public static W3CEndpointReference create(String address) {
        String jaxwsProvider = System.getProperty("javax.xml.ws.spi.Provider");
        System.setProperty("javax.xml.ws.spi.Provider", 
                           "org.apache.cxf.jaxws.spi.ProviderImpl");
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            builder.address(address);
            return builder.build();
        } finally {
            if (jaxwsProvider != null) {
                System.setProperty("javax.xml.ws.spi.Provider", jaxwsProvider);
            }
        }
    }

    public static void marshal(W3CEndpointReference ref, OutputStream os)
        throws Exception {
        JAXBContext ctx = JAXBContext.newInstance("javax.xml.ws.wsaddressing");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.marshal(ref, os);
    }
                                                
}
