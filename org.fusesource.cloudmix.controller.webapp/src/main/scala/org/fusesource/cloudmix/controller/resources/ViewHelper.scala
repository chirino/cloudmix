package org.fusesource.cloudmix.controller.resources

import org.fusesource.scalate.servlet.ServletRenderContext
import org.fusesource.cloudmix.common.dto.{AgentDetails, DependencyStatus, FeatureDetails, ProfileDetails}
import org.fusesource.cloudmix.common.URIs

class ViewHelper(implicit context: ServletRenderContext) {
  def uri(text: String) = context.uri(text)

  // Profiles

  def profileLink(resource: ProfileResource): String = {
    profileLink(resource.getProfileDetails)
  }

  def profileLink(profile: ProfileDetails): String = {
    uri("/profiles/" + profile.getId)
  }

  def propertiesLink(profile: ProfileDetails): String = {
    profileLink(profile) + "/properties"
  }

  def propertiesLink(resource: ProfileResource): String = {
    profileLink(resource) + "/properties"
  }


  // Features....

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

  // Agents
  def agentLink(agent: AgentDetails): String = {
    agentLink(agent.getId)
  }

  def agentLink(agentId: String): String = {
    uri("/agents/" + agentId)
  }

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