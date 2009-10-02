package org.fusesource.cloudmix.controller.properties.scala

import org.fusesource.cloudmix.controller.properties.Expression
import org.fusesource.cloudmix.scalautil.Collections._
import java.lang.String
import java.util.Map
import _root_.scala.tools.nsc.{Interpreter, Settings}
import java.io.PrintWriter

/**
 * @version $Revision : 1.1 $
 */
class ScalaExpression(expression: String) extends Expression {
  def evaluate(variables: Map[String, Object]): AnyRef = {

    // TODO figure out how to configure!!!
    /*
    val settings = new Settings()
    val interpreter = new Interpreter(settings, new PrintWriter(System.out))

    println("About to run interpreter on: " + expression)

    for (entry <- variables.entrySet) {
      val key = entry.getKey()
      val value = entry.getValue()
      if (value != null) {
        val typeName = value.getClass.getCanonicalName
        interpreter.bind(key, typeName, value)
      }
    }

    val result = interpreter.interpret(expression)
    */

    val result = expression

    println("Got result: " + result)
    result

    /*
    result match {
      case s : Success =>
        s
      case _ =>
        null
    }
    */
  }
}