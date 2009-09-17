package org.fusesource.cloudmix.controller.snippet

import org.fusesource.cloudmix.common.dto.{AgentDetails}
import org.fusesource.cloudmix.controller.resources._
import org.fusesource.cloudmix.scalautil.TextFormatting._

import _root_.net.liftweb.util.Helpers._
import _root_.com.sun.jersey.lift.ResourceBean
import _root_.com.sun.jersey.lift.Requests.uri
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._
import _root_.java.util.ArrayList
import _root_.java.util.Map.Entry
import _root_.java.util.TreeMap

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

  def siteLink(agent: AgentDetails): NodeSeq = {
    val href = agent.getHref
    if (href != null)
      <a href={href} class='site'>
        {agent.getId}
      </a>
    else
      Text("")
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
                    "site" -> asText(agent.getHref),
                    AttrBindParam("siteLink", Text("" + agent.getHref), "href"),
                    AttrBindParam("link", Text(agentLink(agent)), "href"))
        }

      case _ =>
        <p>
          <b>Warning</b>
          No Agent resources found!</p>
    }
  }

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case resource: AgentResource =>
        val agent = resource.get
        val systemProperties = new TreeMap[String, String](agent.getSystemProperties)

        bind("agent", xhtml,
          "site" -> asText(agent.getHref),
          AttrBindParam("siteLink", Text("" + agent.getHref), "href"),
          "containerType" -> asText(agent.getContainerType),
          "hostname" -> asText(agent.getHostname),
          "maximumFeatures" -> asText("" + agent.getMaximumFeatures),
          "name" -> asText(agent.getName),
          "id" -> asText(agent.getId),
          "os" -> asText(agent.getOs),
          "pid" -> asText("" + agent.getPid),
          "profile" -> asText(agent.getProfile),
          "systemProperty" -> systemProperties.entrySet.flatMap {
            //case (key : String, value : String) =>
            case entry: Entry[_, _] =>
              bind("systemProperty", chooseTemplate("agent", "systemProperty", xhtml),
                "name" -> asText(entry.getKey),
                "value" -> asText(entry.getValue))
          }.toSeq
          )

      case _ =>
        <p>
          <b>Warning</b>
          No Agent resources found!</p>
    }
  }

}