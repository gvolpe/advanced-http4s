package com.github.gvolpe.fs2

import cats.effect._
import fs2.concurrent.{Signal, SignallingRef, Topic}
import fs2.{Sink, Stream}

import scala.concurrent.duration._

/**
  * Single Publisher / Multiple Subscribers application implemented on top of
  * [[fs2.concurrent.Topic]] and [[fs2.concurrent.Signal]].
  *
  * The program ends after 15 seconds when the signal interrupts the publishing of more events
  * given that the final streaming merge halts on the end of its left stream (the publisher).
  *
  * - Subscriber #1 should receive 15 events + the initial empty event
  * - Subscriber #2 should receive 10 events
  * - Subscriber #3 should receive 5 events
  * */
final case class Event(value: String) extends AnyVal

object PubSubApp extends IOApp {
  import cats.syntax.all._

  def stream[F[_]](implicit F: ConcurrentEffect[F],
                   T: Timer[F]): fs2.Stream[F, Unit] =
    for {
      topic <- Stream.eval(Topic[F, Event](Event("")))
      signal <- Stream.eval(SignallingRef[F, Boolean](false))
      service = new EventService[F](topic, signal)
      emitStopSignal = Stream.sleep(15.seconds) *> Stream.eval(signal.set(true))
      starts = service.startPublisher concurrently service.startSubscribers
      _ <- Stream(emitStopSignal, starts).parJoin[F, Unit](2)
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    stream[IO].compile.drain.as(ExitCode.Success)

}

class EventService[F[_]: Timer](
    eventsTopic: Topic[F, Event],
    interrupter: Signal[F, Boolean])(implicit F: Concurrent[F]) {

  // Publishing events every one second until signaling interruption
  def startPublisher: Stream[F, Unit] =
    Stream
      .awakeEvery[F](1.second)
      .flatMap { _ =>
        val event = Event(System.currentTimeMillis().toString)
        Stream.eval(eventsTopic.publish1(event))
      }
      .interruptWhen(interrupter)

  // Creating 3 subscribers in a different period of time and join them to run concurrently
  def startSubscribers: Stream[F, Unit] = {
    val s1: Stream[F, Event] = eventsTopic.subscribe(10)
    val s2: Stream[F, Event] = eventsTopic.subscribe(10).delayBy(5.seconds)
    val s3: Stream[F, Event] = eventsTopic.subscribe(10).delayBy(10.seconds)

    def sink(subscriberNumber: Int): Sink[F, Event] =
      _.evalMap(e =>
        F.delay(println(s"Subscriber #$subscriberNumber processing event: $e")))

    Stream(s1 to sink(1), s2 to sink(2), s3 to sink(3)).parJoin(3)
  }

}
