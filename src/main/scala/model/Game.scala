package model

import javax.inject.Inject

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import com.github.devnfun.grenadier.engine.Engine
import com.github.devnfun.grenadier.model.{GameEvent, GameState, Signal}

case class Game(
    state: () => GameState,
    flow: Flow[Signal, Seq[GameEvent], NotUsed]
)

class GameFactory @Inject()(engine: Engine, materializer: Materializer) {
  def create(initialStage: GameState) = {
    var _state = initialStage
    val (signalSink, eventSource) =
      MergeHub.source[Signal]
        .map { signal =>
          val (newStage, events) = engine.run(_state, signal :: Nil)
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
