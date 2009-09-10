package org.fusesource.cloudmix.agent.resources

import _root_.javax.ws.rs.{PathParam, Path}
import _root_.org.fusesource.cloudmix.agent.mop.MopAgent

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessesResource(val agent: MopAgent) extends ResourceSupport {
  @Path("{id}")
  def processResource(@PathParam("id") id: String) = {
    def process = processes.get(id)
    if (processes == null) {
      log.debug("could not find process for id: " + id)
      null
    }
    else {
      new ProcessResource(agent, id, process)
    }
  }
}