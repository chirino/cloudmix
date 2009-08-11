package org.fusesource.cloudmix.agent.dto


import javax.xml.bind.annotation.XmlRootElement
import mop.MopProcess
import reflect.BeanProperty

/**
 * @version $Revision : 1.1 $
 */
object ProcessDetails {
  def apply(process: MopProcess)= new ProcessDetails(process.getId, process.getCommandLine, process.getCredentials)
}

@XmlRootElement {val name="process"}
class ProcessDetails(@BeanProperty id: String, @BeanProperty commandLine: String, @BeanProperty credentials: String) {

  // JAXB mandates empty constructor
  def this() = this (null, null, null)
}