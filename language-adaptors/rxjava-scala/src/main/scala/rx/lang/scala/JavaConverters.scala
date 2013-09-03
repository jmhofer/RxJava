package rx.lang.scala

import rx.{ Observable => JObservable }

object JavaConverters {
  implicit class EnrichedJavaObservable[T](jobsv: JObservable[T]) {
    def asScala = new Observable(jobsv)
  }
}