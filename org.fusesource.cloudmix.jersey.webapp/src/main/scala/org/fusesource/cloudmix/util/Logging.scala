package org.fusesource.cloudmix.util

import apache.commons.logging.LogFactory

/**
 * A simple trait for commons logging
 *
 * @version $Revision: 1.1 $
 */
trait Logging {
  val log = LogFactory.getLog(this.getClass)
}