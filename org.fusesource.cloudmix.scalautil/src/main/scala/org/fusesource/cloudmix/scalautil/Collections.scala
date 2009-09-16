package org.fusesource.cloudmix.scalautil

import scala.collection.jcl.MutableIterator.Wrapper
import scala.collection.jcl.Conversions._

/**
 * @version $Revision: 1.1 $
 */

object Collections {

  implicit def iteratorToWrapper[T](iter: java.util.Iterator[T]): Wrapper[T] = new Wrapper[T](iter)  

  implicit def iterableToWrapper[T](iter: java.lang.Iterable[T]): Wrapper[T] = iteratorToWrapper(iter.iterator)

}
