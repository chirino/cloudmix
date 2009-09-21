package org.fusesource.cloudmix.agent.resources

import javax.annotation.PostConstruct
import javax.ws.rs.core.{UriInfo, Context}
import org.fusesource.cloudmix.agent.mop.MopAgent

import _root_.com.sun.jersey.spi.inject.Inject
import _root_.javax.ws.rs.{Path}
/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
@Path("/")
//@Singleton
class AgentResource extends ResourceSupport {
  @Inject
  var agent: MopAgent = null
  @Context
  var uriInfo: UriInfo = null;

  @Path("features")
  def featuresResources = new FeaturesResource(agent)

  @Path("processes")
  def processResources = new ProcessesResource(agent)

  @Path("directory")
  def directory = new RootDirectoryResource(agent.getWorkDirectory, "/directory")

  @PostConstruct
  def start: Unit = {
    if (uriInfo == null) {
      log.error("no uriInfo injected!");
    }
    else if (agent == null) {
      log.error("no agent injected!")
    }
    else {
      // this only needs to be done once on startup
      // but we can't use @Singleton as
      // then we cannot get injected the uriInfo
      agent.setBaseHref(uriInfo.getBaseUri().toString);
    }
  }

}