package org.fusesource.cloudmix.agent.web

import java.lang.reflect.Field

import _root_.javax.annotation.PostConstruct
import _root_.javax.servlet.ServletContext
import _root_.org.springframework.beans.factory.annotation.Autowired
import _root_.org.springframework.stereotype.Component
import org.fusesource.cloudmix.scalautil.Logging
import org.fusesource.cloudmix.agent.mop.MopAgent

/**
 * a helper class which on startup will extract (using a Jetty hack!) the URL
 * and register this agent with the controller
 *
 * @version $Revision : 1.1 $
 */
@Component
class WebLifecycle extends Logging {
  // TODO due to Scala adding annotations to field
  // getter & setter by default we have to write
  // separate setter method and annotate that!
  private var context: ServletContext = _
  private var agent: MopAgent = _

  def servletContext = context

  def mopAgent = agent

  @Autowired
  def servletContext_=(newValue: ServletContext) = context = newValue

  @Autowired
  def mopAgent_=(newValue: MopAgent) = agent = newValue

  @PostConstruct
  def start(): Unit = {
    log.debug("Started up with servletContext: " + context)
    log.debug("Started up with agent: " + agent)

    assert(context != null)
    assert(agent != null)

    // this is a dirty hack which only works with Jetty!
    val connectors = navigateFields(context, "this$0", "_server", "_connectors")
    log.debug("connectors " + connectors)

    type HasHostAndPort = {
      def getName(): String
      def isRunning(): boolean
      def open(): Unit
    }

    var url: String = null
    connectors match {
      case array: Array[AnyRef] =>
        for (a <- array) {
          log.debug("connector " + a)
          val hostAndPort = a.asInstanceOf[HasHostAndPort]
    
          // lets force it to open by default to get the actual local port used
          // as usually we are invoked before the connectors start so they have no port yet
          if (!hostAndPort.isRunning()) {
            hostAndPort.open()
          }
          url = "http://" + hostAndPort.getName()
          log.debug("web application has URL " + url)
        }

      case _ =>
    }

    if (url != null) {
      agent.setBaseHref(url)
    }

    //dumpFields(context, context.getClass, 12)
    ()
  }

  def navigateFields[T](value: AnyRef, names: String*): AnyRef = {
    var answer = value
    for (name <- names) {
      answer = getFieldValue(answer, answer.getClass(), name)
      if (answer == null) {
        return null
      }
    }
    answer
  }

  def getFieldValue[T](value: Object, aClass: Class[T], fieldName: String): AnyRef = {
    log.debug("Attempting to find field " + fieldName + " in type " + aClass)
    if (aClass == classOf[Object] || aClass == null) {
      return null
    }
    val fields: Array[Field] = aClass.getDeclaredFields
    for (field: Field <- fields) {
      field.setAccessible(true)
      val name = field.getName()
      if (name == fieldName) {
        return field.get(value)
      }
    }
    getFieldValue(value, aClass.getSuperclass(), fieldName)
  }


  /*
  def dumpFields[T](value: Object, aClass: Class[T], originalDepth: Int): Unit = {
    val depth = originalDepth - 1
    if (aClass != classOf[Object] && depth > 0) {
      log.debug("fields in type: " + aClass.getName())
      val fields: Array[Field] = aClass.getDeclaredFields
      for (field: Field <- fields) {
        field.setAccessible(true)
        val name = field.getName()
        var fieldValue = field.get(value)

        var done = false
        if (fieldValue.isInstanceOf[Array[AnyRef]]) {
          val array = fieldValue.asInstanceOf[Array[AnyRef]]
          for (a <- array) {
            log.debug("field name " + name + " value " + a)

            // TODO if _connectors then try invoke the getName() method?
            // via reflection???
          }
          done = true
        }
        if (!done) {
          log.debug("field name " + name + " value " + fieldValue)
        }

        if (name == "_connectors") {
          log.debug("_connectors type is " + field.getType())
        }
        if ((name == "this$0" || name == "_server") && fieldValue != null) {
          log.debug("invoking child field " + name)
          dumpFields(fieldValue, fieldValue.getClass(), depth)
        }
      }
      dumpFields(value, aClass.getSuperclass(), depth)
    }
    */
}