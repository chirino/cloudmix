package org.fusesource.cloudmix.agent.resources

import com.google.common.base.Predicate
import com.sun.jersey.api.view.ImplicitProduces
import java.io.{FileReader, BufferedReader, File}
import javax.ws.rs.GET
import javax.ws.rs.core.{UriInfo, Context, MultivaluedMap}
import org.fusesource.cloudmix.agent.logging.{LogRecord, LogHandler, LogPredicate}


/**
 * Represents a log file
 *
 * @version $Revision : 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
class LogResource(file: File, parentPath: String) extends FileSystemResource(file, parentPath) {
  def isDirectory = false
  def icon: String = iconPrefix + "text.gif"
  var handler: LogHandler = null  
  var lock: AnyRef = new Object()
  
  @Context
  var uInfo: UriInfo = null;
  
  @GET
  def logRecords = {
    lock.synchronized {
       if (handler == null) {
           handler = new LogHandler(new BufferedReader(new FileReader(file)));
       }
    }

    val predicate = createPredicate()
    if (predicate != null) {
      handler.findWithPredicate(predicate)
    } else {
      handler.getAllRecords()  
    }
  }
  
  def createPredicate(): Predicate[LogRecord] = {
      if (uInfo != null) {
         val queries = uInfo.getQueryParameters(true)
         if (queries.isEmpty()) {
             null
         } 
         val record = new LogRecord();
         
         // lets start with single values only
         // TODO : for multiple values like level=info&level=warn create 'or' predicates
         // given that a single record can only meet a single criteria like level=info
         // Ex : level=info&level=warn&cat=jetty&cat=felix will result in a composite
         // 'or' predicate containing 4 LogPredicates
         
         val levels = queries.get("level")
         if (levels != null && !levels.isEmpty()) {
             record.setLevel(levels.get(0))
         } 
      
         new LogPredicate(record)
      } else {
         null
      }
  }
}