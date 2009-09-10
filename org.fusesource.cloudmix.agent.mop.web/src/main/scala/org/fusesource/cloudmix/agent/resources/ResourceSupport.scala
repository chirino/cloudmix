package org.fusesource.cloudmix.agent.resources


import _root_.org.fusesource.cloudmix.common.dto.AgentDetails
import _root_.org.fusesource.cloudmix.agent.mop.MopAgent
import _root_.org.fusesource.cloudmix.scalautil.Logging

import _root_.com.sun.jersey.api.view.ImplicitProduces
import _root_.javax.ws.rs.Produces

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

  def processes = agent.getProcesses()

}