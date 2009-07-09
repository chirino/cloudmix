/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import com.sun.jersey.api.view.ImplicitProduces;

import javax.ws.rs.Produces;


/**
 * A base class of helper methods
 *
 * @version $Revision: 1.1 $
 */
@ImplicitProduces("text/html;qs=5")
@Produces({"application/xml", "application/json", "text/xml", "text/json"})
public class ResourceSupport {
}
