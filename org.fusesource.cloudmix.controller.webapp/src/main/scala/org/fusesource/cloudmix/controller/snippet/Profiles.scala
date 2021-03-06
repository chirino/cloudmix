package org.fusesource.cloudmix.controller.snippet

import org.fusesource.cloudmix.common.dto.{DependencyStatus, ProfileDetails}
import org.fusesource.cloudmix.controller.resources._
import org.fusesource.cloudmix.scalautil.TextFormatting._

import scala.xml._
import scala.collection.JavaConversions._

/**
 * Snippets for viewing profiles
 *
 * @version $Revision : 1.1 $
 */
object Profiles {
  def profileLink(resource: ProfileResource): String = {
    profileLink(resource.getProfileDetails)
  }

  def profileLink(profile: ProfileDetails): String = {
    // TODO
    //uri("/profiles/" + profile.getId)
    "/profiles/" + profile.getId
  }

  def propertiesLink(profile: ProfileDetails): String = {
    profileLink(profile) + "/properties"
  }

  def propertiesLink(resource: ProfileResource): String = {
    profileLink(resource) + "/properties"
  }
}

import Features._
import Profiles._

class Profiles {
  /*
  def list(xhtml: Group): NodeSeq = {

    ResourceBean.get match {
      case profiles: ProfilesResource =>
        profiles.getProfiles.flatMap {
          profile: ProfileDetails =>
                  bind("profile", xhtml,
                    "name" -> asText(profile.getId),
                    "description" -> asMarkup(profile.getDescription),
                    AttrBindParam("action", Text(profileLink(profile)), "action"),
                    AttrBindParam("propertiesLink", Text(propertiesLink(profile)), "href"),
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
          "name" -> asText(profile.getProfileId),
          "description" -> asMarkup(profile.getProfileDetails.getDescription),
          AttrBindParam("action", Text(profileLink(profile)), "action"),
          AttrBindParam("link", Text(profileLink(profile)), "href"),
          AttrBindParam("propertiesLink", Text(propertiesLink(profile)), "href"),


          "feature" -> features.flatMap {
            feature: DependencyStatus =>
              bind("feature", chooseTemplate("profile", "feature", xhtml),
                "name" -> asText(feature.getFeatureId),
                "provisioned" -> asText(feature.isProvisioned),
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
  */
}