package rx.lang.scala

import scala.collection.JavaConverters._

import rx.{Observable => JObservable, Scheduler, Subscription, Observer}
import RxImplicits._

object Observable {
  def apply[T](events: T*) = JObservable.from(events.asJava)
  def from[T](events: T*) = apply(events)

  def create[T](func: Observer[_ >: T] => Subscription) = JObservable.create[T](toRxFunc1(func))

  def empty[T] = apply()
  def never[T] = JObservable.never[T]
  def error[T](t: Throwable) = JObservable.error(t)

  def range(start: Int, count: Int) = JObservable.range(start, count)

  def defer[T](observableFactory: => JObservable[T]) = JObservable.defer(toRxFunc0(() => observableFactory))

  def merge[T](src: JObservable[_ >: JObservable[_ >: T]]) = JObservable.merge(src)
  def merge[T](src: JObservable[_ >: T]*) = JObservable.merge(src: _*)

  def concat[T](src: JObservable[_ >: T]*) = JObservable.concat(src: _*)
}

object Scala {
  implicit class Observable[T] private[scala] (val asJava: JObservable[T]) extends AnyVal {
  
    def subscribe(obsv: Observer[_ >: T]): Subscription = asJava subscribe obsv
    def subscribe(obsv: Observer[_ >: T], scheduler: Scheduler): Subscription = asJava subscribe (obsv, scheduler)
    def subscribe(onNext: T => Unit) = asJava subscribe toRxAction1(onNext)
    def subscribe(onNext: T => Unit, scheduler: Scheduler) = asJava subscribe (toRxAction1(onNext), scheduler)
    def subscribe(onNext: T => Unit, onError: Throwable => Unit) = asJava subscribe (toRxAction1(onNext), toRxAction1(onError))
    def subscribe(onNext: T => Unit, onError: Throwable => Unit, scheduler: Scheduler) = asJava subscribe (toRxAction1(onNext), toRxAction1(onError), scheduler)
    def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: => Unit) = asJava subscribe (toRxAction1(onNext), toRxAction1(onError), toRxAction0(() => onComplete))
    def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: => Unit, scheduler: Scheduler) = asJava subscribe (toRxAction1(onNext), toRxAction1(onError), toRxAction0(() => onComplete), scheduler)
  
    def map[R](func: T => R) = asJava.map[R](toRxFunc1(func))
    def flatMap[R](func: T => JObservable[R]) = asJava.flatMap[R](toRxFunc1(func andThen (_.asJava)))
    
    // TODO hmmm, will this ever be used?
    def toList: JObservable[List[T]] = asJava.toList.map(toRxFunc1((_: java.util.List[T]).asScala.toList))
  }
}

import org.scalatest.junit.JUnitSuite

class ScalaWrapperTests extends JUnitSuite {
  import org.junit.Test
  import org.junit.Assert._
  
  @Test def testSingle {
    assertEquals(1, Observable(1).toBlockingObservable.single)
  }
  
  @Test def testSimpleCreationViaApply {
    // TODO how to avoid the ".asScala" at the end?
    assertEquals(List(1, 2, 3), Observable(1, 2, 3).toList.toBlockingObservable.single.asScala)
  }
}
