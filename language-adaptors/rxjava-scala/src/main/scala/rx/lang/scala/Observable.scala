package rx.lang.scala

import rx.{Observable => JObservable, Scheduler, Subscription, Observer}
import RxImplicits._
import JavaConverters._

object Observable {
  def apply[T](events: T*) = JObservable.from(events).asScala
  def from[T](events: T*) = apply(events)

  def create[T](func: Observer[_ >: T] => Subscription) = JObservable.create[T](toRxFunc1(func)).asScala

  def empty[T] = apply()
  def never[T] = JObservable.never[T].asScala
  def error[T](t: Throwable) = JObservable.error(t).asScala

  def range(start: Int, count: Int) = JObservable.range(start, count).asScala

  def defer[T](observableFactory: => Observable[T]) = JObservable.defer(toRxFunc0(() => observableFactory.asJava)).asScala

  def merge[T](src: Observable[_ >: Observable[_ >: T]]) = JObservable.merge(src.asJava).asScala
  def merge[T](src: Observable[_ >: T]*) = JObservable.merge(src.map(_.asJava): _*).asScala

  def concat[T](src: Observable[_ >: T]*) = JObservable.concat(src.map(_.asJava): _*).asScala
}

class Observable[T] private[scala] (jobsv: JObservable[T]) {
  def asJava = jobsv

  def subscribe(obsv: Observer[_ >: T]): Subscription = jobsv subscribe obsv
  def subscribe(obsv: Observer[_ >: T], scheduler: Scheduler): Subscription = jobsv subscribe (obsv, scheduler)
  def subscribe(onNext: T => Unit) = jobsv subscribe toRxAction1(onNext)
  def subscribe(onNext: T => Unit, scheduler: Scheduler) = jobsv subscribe (toRxAction1(onNext), scheduler)
  def subscribe(onNext: T => Unit, onError: Throwable => Unit) = jobsv subscribe (toRxAction1(onNext), toRxAction1(onError))
  def subscribe(onNext: T => Unit, onError: Throwable => Unit, scheduler: Scheduler) = jobsv subscribe (toRxAction1(onNext), toRxAction1(onError), scheduler)
  def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: => Unit) = jobsv subscribe (toRxAction1(onNext), toRxAction1(onError), toRxAction0(() => onComplete))
  def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: => Unit, scheduler: Scheduler) = jobsv subscribe (toRxAction1(onNext), toRxAction1(onError), toRxAction0(() => onComplete), scheduler)

  def map[R](func: T => R) = jobsv.map[R](toRxFunc1(func)).asScala
  def flatMap[R](func: T => Observable[R]) = jobsv.flatMap[R](toRxFunc1(func andThen (_.asJava))).asScala
}
