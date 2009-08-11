package org.fusesource.cloudmix.agent.resources

import mop.{MopProcess, MopAgent}

/**
 * Represents the MOP Agent's resource
 *
 * @version $Revision : 1.1 $
 */
class ProcessResource(val agent: MopAgent, val id: String, val process: MopProcess) extends ResourceSupport {
}