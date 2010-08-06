package org.fusesource.cloudmix.agent.resources


import org.fusesource.cloudmix.common.dto.AgentDetails
import org.fusesource.cloudmix.scalautil.Logging

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import org.fusesource.cloudmix.agent.mop.{MopProcess, MopAgent}
import java.util.Map
import java.net.URI

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

  def name = details.getName

  def processes: Map[String, MopProcess] = agent.getProcesses()


  def agentLink(agent: AgentDetails): String = {
    agentLink(agent.getId)
  }

  def agentLink(agentId: String): String = {
    uri("/agents/" + agentId)
  }

  def featureLink(featureId: String): String = {
    uri("/features/" + featureId)
  }

  def processLink(process: MopProcess): String = {
    processLink(process.getId)
  }

  def processLink(processId: String): String = {
    uri("/processes/" + processId)
  }

  def directoryLink(process: MopProcess): String = {
    processLink(process) + "/directory"
  }


  def uri(text: String): String = {
    // TODO dirty hack as we can't rely on all sub resources being IoC injected right now via Jersey
    // as we manually create sub resources

    // plus can't rely on render context as we invoke this before rendering
    //ServletRenderContext.renderContext.uri(text)

    val base = new URI(agent.getBaseHref).getPath

    println("######## using base ref: " + base)
    
    if (base.endsWith("/") && text.startsWith("/")) {
      base + text.stripPrefix("/")
    }
    else {
      base + text
    }
  }
}