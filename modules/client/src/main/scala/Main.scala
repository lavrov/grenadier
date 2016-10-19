package com.github.devnfun.grenadier

import com.github.devnfun.grenadier.model._
import com.github.devnfun.grenadier.protocol.{ArrowPressed, ClientEvent}
import io.circe.Encoder
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.{Event, MessageEvent}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object Main extends js.JSApp {

  implicit val MessageEncoder = Encoder[ClientEvent]

  val gridStep = 10

  def main() = {
    Ajax.post("/newGame").map { response =>
      val stage = io.circe.parser.parse(response.responseText).flatMap(_.as[GameState]).getOrElse(
        throw new Exception("wrong game format")
      )
      init(stage)
    }
  }

  def init(initState: GameState) = {
    var state = initState
    val canvas = dom.document.createElement("canvas").asInstanceOf[Canvas]

    canvas.width = 600
    canvas.height = 600
    dom.document.body.appendChild(canvas)

    val wsUrl = WebSocketUrlBuilder.fullUrl("/ws")
    val webSocket = new dom.WebSocket(wsUrl)
    webSocket.onopen = { _: Event =>
//      dom.window.alert("Opened WS connection")
    }
    webSocket.onmessage = { message: MessageEvent =>
      val data = message.data.asInstanceOf[String]
      val events = io.circe.parser.parse(data).flatMap(_.as[List[GameEvent]]).getOrElse(
        throw new Exception("wrong message format")
      )

      state = GameMutation(state, events)
      render(state, canvas)
    }

    dom.window.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      PartialFunction.condOpt(e.keyCode) {
        case KeyCode.Down => ArrowPressed(Direction.Down)
        case KeyCode.Up => ArrowPressed(Direction.Up)
        case KeyCode.Left => ArrowPressed(Direction.Left)
        case KeyCode.Right => ArrowPressed(Direction.Right)
      }
      .foreach( message =>
        webSocket.send(MessageEncoder(message).toString)
      )
    }, false)

    render(state, canvas)
  }

  def render(state: GameState, canvas: Canvas) = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.fillStyle = "rgb(20, 250, 250)"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    state.stage.agents.foreach {
      case (index, agent) =>
        ctx.beginPath()
        ctx.arc(agent.position.x * gridStep, agent.position.y * gridStep, gridStep / 2, 0, Math.PI * 2, false)
        ctx.fillStyle = "red"
        ctx.fill()
        ctx.lineWidth = 2
        ctx.strokeStyle = "#003300"
        ctx.stroke()
    }
  }
}
