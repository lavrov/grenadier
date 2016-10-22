package com.github.devnfun.grenadier

import com.github.devnfun.grenadier.model._
import com.github.devnfun.grenadier.protocol.{ArrowPressed, BombDropped, ClientEvent}
import io.circe.Encoder
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, KeyCode}
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.{Event, MessageEvent}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object Main extends js.JSApp {

  implicit val MessageEncoder = Encoder[ClientEvent]

  val gridStep = 30

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

    canvas.width = (state.stage.map.keys.map(_.x).max + 1) * gridStep
    canvas.height = (state.stage.map.keys.map(_.y).max + 1) * gridStep
    dom.document.body.appendChild(canvas)

    val wsUrl = WebSocketUrlBuilder.fullUrl("/ws")
    val webSocket = new dom.WebSocket(wsUrl)
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
        case KeyCode.Space => BombDropped
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

    state.stage.map.foreach {
      case (position, cell) =>
        ctx.beginPath()
        val style = cell match {
          case Wall => "grey"
          case Ground => "white"
          case _ => sys.error("unknown cell type")
        }
        val x = position.x * gridStep
        val y = position.y * gridStep
        ctx.rect(x, y, gridStep, gridStep)
        ctx.fillRect(x, y, gridStep, gridStep)
        ctx.fillStyle = style
        ctx.fill()
    }

    state.stage.agents.foreach {
      case (index, agent) =>
        ctx.beginPath()
        ctx.fillStyle = "blue"
        val radius = gridStep / 2
        val x = agent.position.x * gridStep + radius
        val y = agent.position.y * gridStep + radius
        ctx.arc(x, y, radius, 0, Math.PI * 2, false)
        ctx.fill()
    }

    state.stage.bombs.foreach { bomb =>
      ctx.beginPath()
      ctx.fillStyle = "red"
      val radius = gridStep / 2
      val x = bomb.position.x * gridStep + radius
      val y = bomb.position.y * gridStep + radius
      ctx.arc(x, y, radius, 0, Math.PI * 2, false)
      ctx.fill()
      ctx.beginPath()
      ctx.strokeText(bomb.countDown.toString, x, y)
    }
  }
}
