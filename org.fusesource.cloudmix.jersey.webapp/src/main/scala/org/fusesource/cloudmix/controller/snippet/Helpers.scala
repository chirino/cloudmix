package org.fusesource.cloudmix.controller.snippet

/**
 * @version $Revision: 1.1 $
 */

object Helpers {
  def asText(value: AnyRef): String = {
    if (value != null)
      value.toString
    else
      ""
  }
  def asText(value: boolean): String = {
    if (value)
      "true"
    else
      "false"
  }
}