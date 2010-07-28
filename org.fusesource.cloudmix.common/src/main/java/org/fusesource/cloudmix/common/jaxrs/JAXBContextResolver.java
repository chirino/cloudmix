/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.jaxrs;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.fusesource.cloudmix.common.dto.ObjectFactory;

/**
 * A resolver of the JAXB context primed for our XML languages
 *
 * @version $Revision$
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    private static final String JAXB_PACKAGES 
        = "org.fusesource.cloudmix.common.dto:javax.xml.ws.wsaddressing";

    private final JAXBContext context;

    public JAXBContextResolver() throws JAXBException {
        this.context = JAXBContext.newInstance(ObjectFactory.class, W3CEndpointReference.class);
    }

    public JAXBContext getContext(Class<?> objectType) {
        Package aPackage = objectType.getPackage();
        if (aPackage != null) {
            String name = aPackage.getName();
            if (name.length() > 0) {
                if (JAXB_PACKAGES.contains(name)) {
                    return context;
                }
            }
        }
        return null;
    }

    public JAXBContext getContext() {
        return context;
    }
}

