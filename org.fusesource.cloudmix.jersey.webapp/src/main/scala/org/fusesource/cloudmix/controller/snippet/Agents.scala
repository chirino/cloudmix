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
        // TODO cleaner way to do this???
        def agentList = new ArrayList[AgentDetails](agents.getAgents)
        //def agentList : List[AgentDetails] = List.fromIterator(agents.getAgents.iterator)
        //agentList ++ 

        //agentList.addAll(agents.getAgents)
        
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