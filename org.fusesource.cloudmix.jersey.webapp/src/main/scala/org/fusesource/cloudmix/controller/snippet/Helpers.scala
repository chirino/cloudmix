package org.fusesource.cloudmix.controller.snippet

import cloudmix.util.Logging
import io.Source
import scala.xml.parsing.XhtmlParser
import scala.xml.{Text, NodeSeq}
/**
 * @version $Revision : 1.1 $
 */

object Helpers extends Logging {

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

  def asMarkup(value: String): NodeSeq = {
    // lets parse as markup
    try {
      log.info("parsing " + value)
      
      val source = Source.fromString("<p>" + value + "</p>");
      XhtmlParser(source)
    }
    catch {
      case e =>
        log.warn("Failed to parse '" + value + "' due to " + e, e);
        Text(value)
    }
  }
}