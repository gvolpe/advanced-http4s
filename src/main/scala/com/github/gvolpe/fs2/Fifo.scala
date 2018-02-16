package com.github.gvolpe.fs2

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.async.mutable.Queue
import fs2.{Scheduler, Stream, StreamApp, async}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object FifoApp extends Fifo[IO]

/**
  * Represents a FIFO (First IN First OUT) system built on top of two [[fs2.async.mutable.Queue]].
  *
  * q1 has a buffer size of 1 while q2 has a buffer size of 100 so you will notice the buffering when
  * pulling elements out of the q2.
  * */
class Fifo[F[_]: Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): fs2.Stream[F, ExitCode] =
    Scheduler(corePoolSize = 4).flatMap { implicit S =>
      for {
        q1 <- Stream.eval(async.boundedQueue[F, Int](1))
        q2 <- Stream.eval(async.boundedQueue[F, Int](100))
        bp = new Buffering[F](q1, q2)
        ec <- S.delay(Stream.emit(ExitCode.Success).covary[F], 5.seconds) concurrently bp.start.drain
      } yield ec
    }

}

class Buffering[F[_]](q1: Queue[F, Int], q2: Queue[F, Int])(implicit F: Effect[F]) {

  def start: Stream[F, Unit] =
    Stream(
      Stream.range(0, 1000).covary[F] to q1.enqueue,
      q1.dequeue to q2.enqueue,
      //.map won't work here as you're trying to map a pure value with a side effect. Use `evalMap` instead.
      q2.dequeue.evalMap(n => F.delay(println(s"Pulling out $n from Queue #2")))
    ).join(3)

}