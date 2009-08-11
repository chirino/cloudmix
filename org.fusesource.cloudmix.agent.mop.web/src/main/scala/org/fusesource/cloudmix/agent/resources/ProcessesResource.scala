package org.fusesource.cloudmix.agent.resources

import _root_.javax.ws.rs.{PathParam, Path}
import mop.MopAgent

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessesResource(val agent: MopAgent) extends ResourceSupport {
  @Path("{id}")
  def processResource(@PathParam("id") id: String) = {
    log.info("Looking up id " + id)
    def process = processes.get(id)
    if (processes == null) {
      log.info("not Found!!!")
      null
    }
    else {
      log.info("Found!!!")
      new ProcessResource(agent, id, process)
    }
  }
}