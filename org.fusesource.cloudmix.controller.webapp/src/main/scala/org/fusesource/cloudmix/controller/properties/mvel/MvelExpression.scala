package org.fusesource.cloudmix.controller.properties.mvel



import java.io.PrintWriter
import java.util.Map
import _root_.scala.tools.nsc.Interpreter
import _root_.scala.tools.nsc.Settings

import org.fusesource.cloudmix.controller.properties.Expression
import org.fusesource.cloudmix.scalautil.Collections._

import net.liftweb.util._
import org.mvel2.{ParserContext, MVEL}
import org.fusesource.cloudmix.common.util.Strings
//import net.liftweb.common._
//import net.liftweb.actor._

/**
 * @version $Revision: 1.1 $
 */
class MvelExpression(expression: String) extends Expression {

  def evaluate(variables: Map[String, Object]): AnyRef = {
    val context = new ParserContext();
    context.addImport("Strings", classOf[Strings]);

    val compiled = MVEL.compileExpression(expression, context);

    MVEL.executeExpression(compiled, variables)
  }

  override def toString = "Mvel[" + expression + "]"
}
