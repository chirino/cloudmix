package org.fusesource.cloudmix.agent.resources

import javax.ws.rs.{Produces, GET, PathParam, Path}
//import scala.collection.JavaConversions._
import org.fusesource.cloudmix.scalautil.Collections._
import org.fusesource.cloudmix.common.dto.{Resource, ResourceList}
import org.fusesource.cloudmix.agent.mop.{MopAgent}

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessesResource(val agent: MopAgent) extends ResourceSupport {
  @GET
  @Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
  def resources: ResourceList = {
/*
    val resources = processes.values.map((p) => new Resource(p.getId, p.getId))
    ResourceList.newInstance(iterabletoCollection(resources))
*/
    val answer = new ResourceList
    for (p <- processes.values) {
      val id = p.getId
      answer.addResource(id, id)
    }
    answer
  }

  @Path("{id}")
  def processResource(@PathParam("id") id: String) = {
    def process = processes.get(id)
    if (process == null) {
      log.debug("could not find process for id: " + id)
      null
    }
    else {
      new ProcessResource(agent, id, process)
    }
  }
}