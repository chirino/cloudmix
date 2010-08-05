package org.fusesource.cloudmix.agent.snippet

import org.fusesource.cloudmix.scalautil.TextFormatting._

import net.liftweb.util.Helpers._
import scala.xml._
import scala.collection.JavaConversions._
import org.fusesource.cloudmix.agent.logging.LogRecord
import org.springframework.web.context.support.WebApplicationContextUtils
import net.liftweb.http.S
import org.fusesource.cloudmix.common.CloudmixHelper

/**
 * @version $Revision : 1.1 $
 */
class Templates {
  def index(xhtml: Group): NodeSeq = {


    // TODO find this from the ApplicationContext
    // TODO but how to find the ServletContext???
    // var servletContext = ???
    // var appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
    // var client = appContext.get("gridClient")
    // var cloudmixLink = client.getRootUri

    val cloudmixLink = System.getProperty("agent.controller.uri", CloudmixHelper.getDefaultRootUrl)

    bind("template", xhtml,
      AttrBindParam("cloudmixLink", Text(cloudmixLink), "href"))
  }
}