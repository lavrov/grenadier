package model

import javax.inject.Inject

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import com.github.devnfun.grenadier.engine.Engine
import com.github.devnfun.grenadier.model.{GameEvent, GameState}

import scala.concurrent.duration._

case class Game(
    state: () => GameState,
    flow: Flow[Source[ClientSignal, _], Seq[GameEvent], NotUsed]
)

class GameFactory @Inject()(engine: Engine, materializer: Materializer) {
  val interval = 100.millis

  def create(initialStage: GameState) = {
    var _state = initialStage
    val ticker = Source.tick(interval, interval, 1).scan(0l)(_ + _)
    val signalSource =
      MergeHub.source[Source[ClientSignal, _]]
        .flatMapMerge(100, signals => signals)
        .map(_ :: Nil)
    val (signalSink, eventSource) =
      signalSource.zip(ticker)
        .map { case (signals, tick) =>
          val (newStage, events) = engine.run(_state, signals)
          _state = newStage
          events
        }
        .toMat(BroadcastHub.sink)(Keep.both)
        .run()(materializer)
    Game(
      () => _state,
      Flow fromSinkAndSource (signalSink, eventSource)
    )
  }
}
