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
  val interval = 200.millis

  def create(initialStage: GameState) = {
    var _state = initialStage
    val ticker = Source.tick(interval, interval, Nil: List[Signal])
    val signalSource = MergeHub.source[Signal].groupedWithin(Int.MaxValue, interval)
    val (signalSink, eventSource) =
      signalSource.merge(ticker)
        .map { signals =>
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
