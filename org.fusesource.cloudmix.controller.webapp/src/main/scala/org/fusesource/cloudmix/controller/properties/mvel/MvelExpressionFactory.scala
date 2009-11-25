package org.fusesource.cloudmix.controller.properties.mvel

import org.fusesource.cloudmix.controller.properties.ExpressionFactory

/**
 * @version $Revision: 1.1 $
 */
class MvelExpressionFactory extends ExpressionFactory {
  def createExpression(expression: String) = new MvelExpression(expression)
}