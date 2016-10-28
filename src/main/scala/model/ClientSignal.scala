package model

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.github.devnfun.grenadier.model.Direction
import com.github.devnfun.grenadier.protocol.{ArrowKey, BombKey, ClientEvent, KeyboardEvent}
import play.api.Logger

case class ClientSignal(
    clientId: Int,
    signal: Signal
)

case class Signal(
    move: Set[Direction.Value] = Set.empty,
    bomb: Boolean = false
)

object ClientSignal {
  def fromEvents(clientId: Int): Flow[ClientEvent, ClientSignal, NotUsed] = {
    Flow[ClientEvent].scan(Signal()) {
      case (signal, KeyboardEvent(isDown, key)) => key match {
        case ArrowKey(direction) => signal.copy(move = if (isDown) signal.move + direction else signal.move - direction)
        case BombKey => signal.copy(bomb = isDown)
      }
    }
    .scan(Signal() -> true){
      case ((prev, _), current) => (current, prev != current)
    }
    .collect {
      case (signal, true) => signal
    }
    .map(ClientSignal(clientId, _))
    .via(new LastAvailable[ClientSignal])
  }
}
