package org.fusesource.cloudmix.controller.snippet

import common.dto.{DependencyStatus, ProfileDetails}
import resources._

import _root_.net.liftweb.util.Helpers._
import _root_.com.sun.jersey.lift.ResourceBean
import _root_.com.sun.jersey.lift.Requests.uri
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._

/**
 * Snippets for viewing profiles
 *
 * @version $Revision : 1.1 $
 */
object Profiles {
  def profileLink(profile: ProfileDetails): String = {
    uri("/profiles/" + profile.getId)
  }
}

import Features._
import Profiles._

class Profiles {
  def list(xhtml: Group): NodeSeq = {

    ResourceBean.get match {
      case profiles: ProfilesResource =>
        profiles.getProfiles.flatMap {
          profile: ProfileDetails =>
                  bind("profile", xhtml,
                    "name" -> Text(profile.getId),
                    AttrBindParam("link", Text(profileLink(profile)), "href"))
        }

      case _ =>
        <p> <b>Warning</b>No Profile resources found!</p>
    }
  }

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case profile: ProfileResource =>
        val features = profile.getStatus.getStatus.getFeatures

        bind("profile", xhtml,
          "name" -> Text(profile.getProfileId),

          "feature" -> features.flatMap {
            feature: DependencyStatus =>
              bind("feature", chooseTemplate("profile", "feature", xhtml),
                "name" -> Text(feature.getFeatureId),
                "provisioned" -> Text(if (feature.isProvisioned) "true" else "false"),
                AttrBindParam("link", Text(featureLink(feature)), "href"))
          })

      case _ =>
        <p> <b>Warning</b>No Profile resources found!</p>
    }
  }

  /*
   AttrBindParam("link", Text(linkUri(profile)), "href"),
   AttrBindParam("sendLink", Text(sendLink(profile)), "href"),
   AttrBindParam("subscribeLink", Text(subscribeLink(profile)), "href"),
   AttrBindParam("subscribeAction", Text(subscriptionsLink(profile)), "action"),
   AttrBindParam("subscriptionsLink", Text(subscriptionsLink(profile)), "href"),
   AttrBindParam("profilesLink", Text(profile.getContainerLink), "href"),
   "form" -> <form action={linkUri(profile)} method="post" name="sendMessage">{chooseTemplate("profile", "form", xhtml)}</form>,

   // these are actually profile specific
   AttrBindParam("messagesLink", Text(messagesLink(profile)), "href"),
   "messageCount" -> Text("" + messages.size),
  */

  /*
  def sendLink(profile: ProfileResource): String = {
    uri(profile.getLink) + "/send"
  }

  def messagesLink(profile: ProfileResource): String = {
    uri(profile.getLink) + "/messages"
  }

  def subscriptionsLink(profile: ProfileResource): String = {
    uri(profile.getLink) + "/subscriptions"
  }

  def subscribeLink(profile: ProfileResource): String = {
    uri(profile.getLink) + "/subscribe"
  }
  */
}