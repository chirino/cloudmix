package org.fusesource.cloudmix.agent.snippet

import org.fusesource.cloudmix.common.dto.AgentDetails
import org.fusesource.cloudmix.agent.mop.MopProcess
import org.fusesource.cloudmix.agent.resources.{ProcessResource, ResourceSupport}
import org.fusesource.cloudmix.scalautil.TextFormatting._

import com.sun.jersey.lift.Requests.uri
import com.sun.jersey.lift.ResourceBean
import java.util.TreeMap
import java.util.Map.Entry
import net.liftweb.util.Helpers._
import scala.xml._
import scala.collection.jcl.Conversions._

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
  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case agent: ResourceSupport =>
        val details = agent.details
        val systemProperties = new TreeMap[String, String](details.getSystemProperties)
        val processes = new TreeMap[String, MopProcess](agent.processes)

        bind("agent", xhtml,
          "site" -> siteLink(details),
          "containerType" -> asText(details.getContainerType),
          "hostname" -> asText(details.getHostname),
          "maximumFeatures" -> asText("" + details.getMaximumFeatures),
          "name" -> asText(details.getName),
          "id" -> asText(details.getId),
          "os" -> asText(details.getOs),
          "pid" -> asText("" + details.getPid),
          "profile" -> asText(details.getProfile),

          "systemProperty" -> systemProperties.entrySet.flatMap {
            //case (key : String, value : String) =>
            case entry: Entry[_, _] =>
              bind("systemProperty", chooseTemplate("agent", "systemProperty", xhtml),
                "name" -> asText(entry.getKey),
                "value" -> asText(entry.getValue))
          }.toSeq,

          // TODO can we share bindings with the below??
          "process" -> processes.entrySet.flatMap {
            //case (key : String, value : String) =>
            case entry: Entry[_, _] =>
              val process: MopProcess = entry.getValue
              bind("process", chooseTemplate("agent", "process", xhtml),
                "id" -> asText(entry.getKey),
                "commandLine" -> asText(process.getCommandLine),
                "directory" -> asText(process.getWorkDirectory),
                AttrBindParam("link", Text(processLink(process)), "href"),
                AttrBindParam("dirLink", Text(directoryLink(process)), "href"),
                AttrBindParam("directoryTitle", Text(asText(process.getWorkDirectory)), "title"),
                AttrBindParam("action", Text(processLink(process)), "action"))
          }.toSeq
          )

      case _ =>
        <p>
          <b>Warning</b>
          No Agent resources found!</p>
    }
  }

  def process(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case processResource: ProcessResource =>
        val process = processResource.process

        if (process == null) {
          <p>No such Process</p>
        }
        else {
          bind("process", xhtml,
            "id" -> asText(process.getId),
            "site" -> processLink(process.getId),
            "feature" -> asText(process.getFeatureId),
            "commandLine" -> asText(process.getCommandLine),
            "directory" -> asText(process.getWorkDirectory),
            "credentials" -> asText(process.getCredentials),
            AttrBindParam("dirLink", Text(directoryLink(process)), "href"),
            AttrBindParam("featureLink", Text(featureLink(process.getFeatureId)), "href"),
            AttrBindParam("directoryTitle", Text(asText(process.getWorkDirectory)), "title"),
            AttrBindParam("action", Text(processLink(process)), "action"))

        }

      case _ =>
        <p>
          <b>Warning</b>
          No Agent resources found!</p>
    }
  }

}