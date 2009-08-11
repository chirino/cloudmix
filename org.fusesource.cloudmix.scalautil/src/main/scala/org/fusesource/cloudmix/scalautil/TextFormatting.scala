package org.fusesource.cloudmix.scalautil

import io.Source
import _root_.scala.xml.parsing.XhtmlParser
import _root_.scala.xml.{Text, NodeSeq}

/**
 * @version $Revision : 1.1 $
 */
object TextFormatting extends Logging {

  def asText(value: AnyRef): String = {
    if (value != null)
      value.toString
    else
      ""
  }

  def asText(value: Boolean): String = {
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