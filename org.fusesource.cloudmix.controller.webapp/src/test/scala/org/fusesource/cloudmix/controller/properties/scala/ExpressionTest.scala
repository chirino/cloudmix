package org.fusesource.cloudmix.controller.properties.scala


import org.junit.Test
import org.junit.Assert._


/*
import org.specs._
import org.specs.runner._

import org.junit.runner.RunWith
import org.junit.runner.runner._
*/

/*
@RunWith(classOf[JUnitSuiteRunner])
class ExpressionTest {
  @Test  
}
*/
import java.util.{HashMap => JavaHashMap}


/**
 * @version $Revision : 1.1 $
 */
//class ExpressionTest extends Specification with JUnit {

@Test
class ExpressionTest {

  //"parse expression" in {

  @Test
  def testParsing = {
    val factory = new ScalaExpressionFactory
    val expression = factory.createExpression("2 * 2")

    println("Created expression " + expression)
    
    val variables = new JavaHashMap[String,Object]()

    val answer = expression.evaluate(variables)

    println("Evaluated expression with result: " + answer)
  }
  
}