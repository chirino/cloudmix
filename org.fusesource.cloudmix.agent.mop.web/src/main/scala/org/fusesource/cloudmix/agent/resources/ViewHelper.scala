package org.fusesource.cloudmix.agent.resources

import org.fusesource.scalate.servlet.ServletRenderContext
import org.fusesource.cloudmix.common.dto.AgentDetails
import org.fusesource.cloudmix.agent.mop.MopProcess


class ViewHelper(implicit context: ServletRenderContext) {

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

  def uri(text: String) = context.uri(text)

/*
  def siteLink(agent: AgentDetails): NodeSeq = {
    val href = agent.getHref
    if (href != null)
      <a href={href} class='site'>
        {agent.getId}
      </a>
    else
      Text("")
  }
*/

}
