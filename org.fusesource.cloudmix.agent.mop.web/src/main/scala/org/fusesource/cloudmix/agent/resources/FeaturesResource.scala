package org.fusesource.cloudmix.agent.resources

import javax.ws.rs.{Produces, GET, PathParam, Path}
import scala.collection.jcl.Conversions._
import org.fusesource.cloudmix.common.dto.{ResourceList}
import org.fusesource.cloudmix.agent.mop.{MopAgent}
import org.fusesource.cloudmix.scalautil.Collections._

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
@Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
class FeaturesResource(val agent: MopAgent) extends ResourceSupport {
  @GET
  def resources: ResourceList = {
    val answer = new ResourceList
    for (p <- processes.values) {
      val id = p.getFeatureId
      if (!answer.containsName(id)) {
        answer.addResource("/features/" + id, id);
      }
    }
    answer
  }

  @Path("{id}")
  def featureResource(@PathParam("id") id: String) = new FeatureResource(agent, id)
}