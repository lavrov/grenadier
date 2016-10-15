package controllers

import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import com.github.devnfun.grenadier.engine.{AgentMoves, Engine}
import play.api.mvc._
import io.circe.generic.auto._
import io.circe.syntax._
import com.github.devnfun.grenadier.model._
import play.api.libs.circe.Circe

class GameController @Inject()(implicit materializer: Materializer) extends Controller with Circe {

  var s = {
    val agent = Agent(Position(0, 0), Direction.Right)
    val map = Map[Position, Cell](Position(0, 0) -> Ground, Position(0, 1) -> Ice).withDefaultValue(Abyss)
    Stage(map, agents = Map(0 -> agent))
  }

  val engineFlow = {
    val engine = Engine(new AgentMoves)
    val (signalSink, eventSource) =
      MergeHub.source[Signal]
        .map { signal =>
          val (newStage, events) = engine(s, signal :: Nil)
          s = newStage
          events
        }
        .toMat(BroadcastHub.sink)(Keep.both).run
    Flow.fromSinkAndSource(signalSink, eventSource)
  }

  def index = Action{
    Ok(html.index())
  }

  def stage = Action {
    Ok(s.asJson)
  }

  def webSocket = WebSocket.accept[String, String] { req =>
    val signals =
      Flow.fromFunction(io.circe.parser.parse(_: String))
        .map(
          _.flatMap(_.as[Signal]).getOrElse(sys.error("not a signal")))
    val events =
      Flow.fromFunction { events: Seq[StageEvent] =>
        events.asJson.toString
      }
    signals via engineFlow via events
  }

}