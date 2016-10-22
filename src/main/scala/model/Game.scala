package model

import javax.inject.Inject

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import com.github.devnfun.grenadier.engine.Engine
import com.github.devnfun.grenadier.model.{GameEvent, GameState, Signal}

import scala.concurrent.duration._

case class Game(
    state: () => GameState,
    flow: Flow[Signal, Seq[GameEvent], NotUsed]
)

class GameFactory @Inject()(engine: Engine, materializer: Materializer) {
  val interval = 1.second

  def create(initialStage: GameState) = {
    var _state = initialStage
    val ticker = Source.tick(interval, interval, ()).map(_ => 1).scan(0)(_ + _)
    val signalSource = MergeHub.source[Signal]
      .conflateWithSeed(List(_)) { case (list, signal) => signal :: list }
    val (signalSink, eventSource) =
      signalSource.zip(ticker)
        .map { case (signals, time) =>
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
