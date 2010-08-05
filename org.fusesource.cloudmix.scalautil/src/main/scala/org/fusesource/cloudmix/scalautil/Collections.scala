package org.fusesource.cloudmix.scalautil

import java.util.{ArrayList => JavaArrayList}
import java.util.{Collection => JavaCollection}
import java.util.{Iterator => JavaIterator}
import java.lang.{Iterable => JavaIterable}
import collection.JavaConversions

/**
 * @version $Revision: 1.1 $
 */

object Collections {

  implicit def iteratorToWrapper[T](iter: JavaIterator[T]): Iterator[T] = JavaConversions.asIterator(iter)

  implicit def iterableToWrapper[T](iter: JavaIterable[T]): Iterator[T] = JavaConversions.asIterator(iter.iterator)

  implicit def collectionToWrapper[T](collection: JavaCollection[T]): Iterator[T] = iteratorToWrapper(collection.iterator)

  implicit def iterabletoCollection[T](iterable: Iterable[T]): JavaCollection[T] = {
    val answer = new JavaArrayList[T]
    for (i <- iterable) {
      answer.add(i)
    }
    answer
  }
 
  /*

  implicit def iterabletoCollection[T](iterable: Iterable[T]): JavaCollection[T] = {
    val answer = new ArrayList[T]
    for (i <- iterable) {
      answer.add(i)
    }
    answer
  }

  */

}
