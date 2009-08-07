package org.fusesource.cloudmix.controller.snippet


import io.Source
import scala.xml.parsing.{XhtmlParser, ConstructingParser}
import scala.xml.{Text, NodeSeq}
/**
 * @version $Revision : 1.1 $
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

  def asMarkup(value: String): NodeSeq = {
    // lets parse as markup
    try {
      val source = Source.fromString("<p>" + value + "</p>");
      XhtmlParser(source)
    }
    catch {
      case e =>
        // TODO log the error with clogging!
        println("Failed to parse '" + value + "' due to " + e);
        Text(value)
    }
  }
}