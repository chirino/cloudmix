package org.fusesource.cloudmix.controller.snippet

import org.fusesource.cloudmix.common.dto.{AgentDetails, FeatureDetails, DependencyStatus}
import org.fusesource.cloudmix.controller.resources._
import org.fusesource.cloudmix.scalautil.TextFormatting._

import net.liftweb.util.Helpers._
import com.sun.jersey.lift.ResourceBean
import com.sun.jersey.lift.Requests.uri
import scala.xml._
import scala.collection.jcl.Conversions._
import org.fusesource.cloudmix.common.URIs

/**
 * Snippets for viewing features
 *
 * @version $Revision : 1.1 $
 */
object Features {
  def featureLink(feature: FeatureDetails): String = {
    featureLink(feature.getId)
  }

  def featureLink(feature: DependencyStatus): String = {
    featureLink(feature.getFeatureId)
  }

  def featureLink(featureId: String): String = {
    uri("/features/" + featureId)
  }

  def agentFeatureLink(agent: AgentDetails, featureId: String): String = {
    URIs.appendPaths(agent.getHref, "features", featureId)
  }
}

import Features._

class Features {
  def list(xhtml: Group): NodeSeq = {

    ResourceBean.get match {
      case features: FeaturesResource =>
        features.getFeatures.flatMap {
          feature: FeatureDetails =>
                  bind("feature", xhtml,
                    "name" -> Text(feature.getId),
                    AttrBindParam("link", Text(featureLink(feature)), "href"))
        }

      case _ =>
        <p> <b>Warning</b>No Feature resources found!</p>
    }
  }

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case feature: FeatureResource =>
        //val features = feature.getStatus.getStatus.getFeatures

        val agents = feature.getAgents

        bind("feature", xhtml,
          "name" -> Text(feature.getFeatureId),

          "agent" -> agents.flatMap {
            agent: AgentDetails =>
             var name = agent.getName
             if (name == null) {
               name = "agent"
             }
             val featureLink = agentFeatureLink(agent, feature.getFeatureId)
             println("featureLink is " + featureLink + " for agent " + agent + " with href " + agent.getHref)
             bind("agent", chooseTemplate("feature", "agent", xhtml),
                "name" -> Text(name),
                AttrBindParam("featureLink", Text(featureLink), "href"))
          })

/*

          "feature" -> features.flatMap {
            feature: DependencyStatus =>
              bind("feature", chooseTemplate("feature", "feature", xhtml),
                "name" -> Text(feature.getFeatureId),
                "provisioned" -> Text(if (feature.isProvisioned) "true" else "false"),
                AttrBindParam("link", Text(linkUri(feature)), "href"))
          })

*/
      case _ =>
        <p> <b>Warning</b>No Feature resources found!</p>
    }
  }

  /*
   AttrBindParam("link", Text(linkUri(feature)), "href"),
   AttrBindParam("sendLink", Text(sendLink(feature)), "href"),
   AttrBindParam("subscribeLink", Text(subscribeLink(feature)), "href"),
   AttrBindParam("subscribeAction", Text(subscriptionsLink(feature)), "action"),
   AttrBindParam("subscriptionsLink", Text(subscriptionsLink(feature)), "href"),
   AttrBindParam("featuresLink", Text(feature.getContainerLink), "href"),
   "form" -> <form action={linkUri(feature)} method="post" name="sendMessage">{chooseTemplate("feature", "form", xhtml)}</form>,

   // these are actually feature specific
   AttrBindParam("messagesLink", Text(messagesLink(feature)), "href"),
   "messageCount" -> Text("" + messages.size),
  */

  /*
  def sendLink(feature: FeatureResource): String = {
    uri(feature.getLink) + "/send"
  }

  def messagesLink(feature: FeatureResource): String = {
    uri(feature.getLink) + "/messages"
  }

  def subscriptionsLink(feature: FeatureResource): String = {
    uri(feature.getLink) + "/subscriptions"
  }

  def subscribeLink(feature: FeatureResource): String = {
    uri(feature.getLink) + "/subscribe"
  }
  */
}