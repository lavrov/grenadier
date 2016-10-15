package com.github.devnfun.grenadier

import com.github.devnfun.grenadier.model._
import io.circe.{Decoder, Encoder, KeyDecoder}
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}
import org.scalajs.dom.{CanvasRenderingContext2D, Event, MessageEvent}
import org.scalajs.dom.html.Canvas

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object Main extends js.JSApp {

  implicit val SignalEncoder = Encoder[Signal]
  implicit val StageDecoder = Decoder[Stage]

  val gridStep = 10

  def main() = {
    Ajax.get("/stage").map { response =>
      val stage = io.circe.parser.parse(response.responseText).flatMap(_.as(StageDecoder)).getOrElse(
        throw new Exception("not a Stage")
      )
      init(stage)
    }
  }

  def init(initStage: Stage) = {
    var stage = initStage
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
      val events = io.circe.parser.parse(data).flatMap(_.as[List[StageEvent]]).getOrElse(
        throw new Exception("wrong message format")
      )

      stage = StageMutation(stage, events)
      render(stage, canvas)
    }

    dom.window.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      val signal =
        PartialFunction.condOpt(e.keyCode) {
          case KeyCode.Down => Move(0, Direction.Down)
          case KeyCode.Up => Move(0, Direction.Up)
          case KeyCode.Left => Move(0, Direction.Left)
          case KeyCode.Right => Move(0, Direction.Right)
        }
      signal.foreach(s =>
        webSocket.send(SignalEncoder(s).toString())
      )
    }, false)

    render(stage, canvas)
  }

  def render(stage: Stage, canvas: Canvas) = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.fillStyle = "rgb(20, 250, 250)"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    stage.agents.foreach {
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
