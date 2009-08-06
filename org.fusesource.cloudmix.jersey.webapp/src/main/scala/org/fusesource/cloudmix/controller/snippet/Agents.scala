package org.fusesource.cloudmix.controller.snippet

import common.dto.{AgentDetails, DependencyStatus}
import resources._

import _root_.net.liftweb.util.Helpers._
import _root_.com.sun.jersey.lift.ResourceBean
import _root_.com.sun.jersey.lift.Requests.uri
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._
import _root_.java.util.ArrayList

/**
 * Snippets for viewing agents
 *
 * @version $Revision : 1.1 $
 */


object Agents {
  def agentLink(agent: AgentDetails): String = {
    agentLink(agent.getId)
  }

  def agentLink(agentId: String): String = {
    uri("/agents/" + agentId)
  }

}

import Agents._

class Agents {
  def list(xhtml: Group): NodeSeq = {

    ResourceBean.get match {
      case agents: AgentsResource =>
        // TODO shame there's not a standard conversion to scala list for Collection
        // wonder if we can zap this in Scala 2.8?
        def agentList = new ArrayList[AgentDetails](agents.getAgents)

        agentList.flatMap {
          agent: AgentDetails =>
                  bind("agent", xhtml,
                    "name" -> Text(agent.getId),
                    AttrBindParam("link", Text(agentLink(agent)), "href"))}

      case _ =>
        <p> <b>Warning</b>No Agent resources found!</p>
    }
  }

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case agent: AgentResource =>
        bind("agent", xhtml,
          "name" -> Text(agent.get.getId))

      case _ =>
        <p> <b>Warning</b>No Agent resources found!</p>
    }
  }

}