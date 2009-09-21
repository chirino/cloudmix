package org.fusesource.cloudmix.agent.resources

import scala.collection.jcl.Conversions._
import javax.ws.rs.{Produces, GET}
import org.fusesource.cloudmix.common.dto.{ResourceList}
import org.fusesource.cloudmix.agent.mop.{MopAgent}
import org.fusesource.cloudmix.scalautil.Collections._

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class FeatureResource(val agent: MopAgent, val id: String) extends ResourceSupport {

  @GET
  @Produces(Array("application/xml", "application/json", "text/xml", "text/json"))
  def resources: ResourceList = {
    val answer = new ResourceList
    for (p <- processes.values) {
      val featureId = p.getFeatureId
      if (id == featureId) {
        val pid = p.getId
        answer.addResource("/processes/" + pid, pid);
      }
    }
    answer
  }

}