package org.fusesource.cloudmix.controller.properties.scala

import java.io.PrintWriter
import java.util.Map
import _root_.scala.tools.nsc.Interpreter
import _root_.scala.tools.nsc.Settings

import org.fusesource.cloudmix.controller.properties.Expression
import org.fusesource.cloudmix.scalautil.Collections._

//import net.liftweb.util._
//import net.liftweb.common._
//import net.liftweb.actor._

class ScalaExpression(expression: String) extends Expression {

  private val settings = new Settings(null)
  val origBootclasspath = settings.classpath.value

  lazy val pathList = List(jarPathOfClass("scala.tools.nsc.Interpreter"),
                           jarPathOfClass("scala.ScalaObject"))

  def evaluate(variables: Map[String, Object]): AnyRef = {
      settings.classpath.value = (origBootclasspath :: pathList).mkString(java.io.File.separator)

      val out = new java.io.StringWriter()
      val interpreter = new Interpreter(settings, new PrintWriter(out))

      println("scala bindings for variables: " + variables)
    
      for (entry <- variables.entrySet) {
        val key = entry.getKey()
        val value = entry.getValue()
        if (value != null) {
          val typeName = value.getClass.getCanonicalName
          println("scala value " + key + " = " + value + " typeName: " + typeName)

          // TODO
          //interpreter.bind(key, typeName, value)
        }
      }
      
      interpreter.interpret(expression)

      return out.toString
  }

  def jarPathOfClass(className: String) = {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    val indexOfFile = path.indexOf("file:")
    val indexOfSeparator = path.lastIndexOf('!')
    path.substring(indexOfFile, indexOfSeparator)

    // potentially problematic with e.g. OSGi:
    // Class.forName(className).getProtectionDomain.getCodeSource.getLocation
  }
}


/*
import java.lang.String
import java.util.Map
import _root_.scala.tools.nsc.{Interpreter, Settings}
import java.io.PrintWriter

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

  }
}
    */
