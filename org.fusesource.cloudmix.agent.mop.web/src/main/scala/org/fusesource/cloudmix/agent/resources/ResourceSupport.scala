package org.fusesource.cloudmix.agent.resources


import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import scalautil.Logging

/**
 * Base class for resources
 *
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
abstract class ResourceSupport extends Logging {

}