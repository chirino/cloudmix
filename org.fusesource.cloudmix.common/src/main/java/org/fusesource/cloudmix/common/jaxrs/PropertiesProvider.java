/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.jaxrs;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * An entity for {@link Properties} objects
 *
 * @version $Revision: 1.1 $
 */
@Produces("text/plain")
@Provider
public class PropertiesProvider implements MessageBodyWriter<Properties>, MessageBodyReader<Properties> {

    public void writeTo(Properties value, Class<?> type, Type genericType, Annotation annotations[],
                        MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
        value.store(out, null);
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return Properties.class.isAssignableFrom(type);
    }

    public long getSize(Properties p, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return -1;
    }

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Properties.class.isAssignableFrom(type);
    }

    public Properties readFrom(Class<Properties> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        Properties answer = new Properties();
        answer.load(inputStream);
        return answer;
    }
}
