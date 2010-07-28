package org.fusesource.cloudmix.controller.properties.scala


import org.junit.Test
import org.fusesource.cloudmix.common.dto.AgentDetails
import java.util.{ArrayList, HashMap}


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

/**
 * @version $Revision : 1.1 $
 */
//class ExpressionTest extends Specification with JUnit {

@Test
class ExpressionTest {
  val factory = new ScalaExpressionFactory

  @Test
  def testParsing: Unit = {
    evaluteExpression("2 * 2")
    evaluteExpression("agents.size()")
    evaluteExpression("agents.size()")
    evaluteExpression("agents.map(\"tcp://\" + _.getHostname + \":61616\").mkString(\"failover:(\", \",\", \")\")");
  }


  def evaluteExpression(text: String) = {
    val expression = factory.createExpression(text)

    println("Created expression " + expression)

    val variables = new HashMap[String, Object]()
    val agents = new ArrayList[AgentDetails]
    agents.add(new AgentDetails("a", "AgentA", "host1"))
    agents.add(new AgentDetails("b", "AgentB", "host2"))

    variables.put("agents", agents)

    val answer = expression.evaluate(variables)

    println("Evaluated expression with result: " + answer)

    answer
  }
}