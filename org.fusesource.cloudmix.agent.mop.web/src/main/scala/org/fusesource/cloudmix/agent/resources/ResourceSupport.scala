package org.fusesource.cloudmix.agent.resources


import org.fusesource.cloudmix.common.dto.AgentDetails
import org.fusesource.cloudmix.scalautil.Logging

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import org.fusesource.cloudmix.agent.mop.{MopProcess, MopAgent}
import org.fusesource.mop.com.google.common.collect.ImmutableMap

/**
 * Base class for resources
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
abstract class ResourceSupport extends Logging {
  def agent: MopAgent

  def details: AgentDetails = {
    agent.getAgentDetails
  }

  def processes : ImmutableMap[String,MopProcess] = agent.getProcesses()

}