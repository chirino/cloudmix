package org.fusesource.cloudmix.controller.properties.scala

import org.fusesource.cloudmix.controller.properties.ExpressionFactory
import java.lang.String

/**
 * @version $Revision: 1.1 $
 */
class ScalaExpressionFactory extends ExpressionFactory {
  def createExpression(expression: String) = new ScalaExpression(expression)
}