package com.github.gvolpe.fs2

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.async.Ref
import fs2.{Scheduler, Sink, Stream, StreamApp, async}

import scala.concurrent.ExecutionContext.Implicits.global

object CounterApp extends Counter[IO]

/**
  * Concurrent counter that demonstrates the use of [[fs2.async.Ref]].
  *
  * The workers will concurrently run and modify the value of the Ref so this is one
  * possible outcome showing "#worker >> currentCount":
  *
  * #1 >> 0
  * #3 >> 0
  * #2 >> 0
  * #1 >> 2
  * #2 >> 3
  * #3 >> 3
  * */
class Counter[F[_] : Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): fs2.Stream[F, ExitCode] =
    Scheduler(corePoolSize = 10).flatMap { implicit S =>
      for {
        ref <- Stream.eval(async.refOf[F, Int](0))
        w1  = new Worker[F](1, ref)
        w2  = new Worker[F](2, ref)
        w3  = new Worker[F](3, ref)
        ec  <- Stream(w1.start, w2.start, w3.start).join(3).drain ++ Stream.emit(ExitCode.Success)
      } yield ec
    }

}

class Worker[F[_]](number: Int, ref: Ref[F, Int])
                  (implicit F: Effect[F]) {

  private val sink: Sink[F, Int] = _.evalMap(n => F.delay(println(s"#$number >> $n")))

  def start: Stream[F, Unit] =
    for {
      _ <- Stream.eval(ref.get) to sink
      _ <- Stream.eval(ref.modify(_ + 1))
      _ <- Stream.eval(ref.get) to sink
    } yield ()

}
