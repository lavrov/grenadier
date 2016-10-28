package controllers

import javax.inject.Inject

import akka.stream.scaladsl.{Flow, Source}
import com.github.devnfun.grenadier.model._
import com.github.devnfun.grenadier.protocol.ClientEvent
import io.circe.generic.auto._
import io.circe.syntax._
import model.{ClientSignal, GameRegistry}
import play.api.libs.circe.Circe
import play.api.mvc._

class GameController @Inject()(registry: GameRegistry) extends Controller with Circe {

  val (_, game) = registry.`new`()

  def index = Action{
    Ok(html.index())
  }

  def newGame = Action {
    Ok(game.state().asJson)
  }

  def webSocket = WebSocket.accept[String, String] { req =>
    val signals: Flow[String, ClientSignal, _] =
      Flow.fromFunction(io.circe.parser.parse(_: String))
        .map(
          _.flatMap(_.as[ClientEvent]).getOrElse(sys.error("wrong message format")))
        .via(ClientSignal.fromEvents(0))
    val events =
      Flow.fromFunction { events: Seq[GameEvent] =>
        events.asJson.toString
      }
    signals.prefixAndTail(0).map(_._2) via game.flow via events
  }

}