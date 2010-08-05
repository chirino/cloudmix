package org.fusesource.cloudmix.controller.resources

import org.fusesource.cloudmix.common.dto.ProfileDetails
import org.fusesource.scalate.servlet.ServletRenderContext

class ProfileHelper(implicit context: ServletRenderContext) {

  def profileLink(resource: ProfileResource): String = {
    profileLink(resource.getProfileDetails)
  }

  def profileLink(profile: ProfileDetails): String = {
    context.uri("/profiles/" + profile.getId)
  }

  def propertiesLink(profile: ProfileDetails): String = {
    profileLink(profile) + "/properties"
  }

  def propertiesLink(resource: ProfileResource): String = {
    profileLink(resource) + "/properties"
  }

}