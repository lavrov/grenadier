package com.github.devnfun.grenadier

import com.github.devnfun.grenadier.model._
import com.github.devnfun.grenadier.protocol._
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

    def keyHander(isDown: Boolean) = {
      (e: dom.KeyboardEvent) => {
        PartialFunction.condOpt(e.keyCode) {
          case KeyCode.Down => KeyboardEvent(isDown, ArrowKey(Direction.Down))
          case KeyCode.Up => KeyboardEvent(isDown, ArrowKey(Direction.Up))
          case KeyCode.Left => KeyboardEvent(isDown, ArrowKey(Direction.Left))
          case KeyCode.Right => KeyboardEvent(isDown, ArrowKey(Direction.Right))
          case KeyCode.Space => KeyboardEvent(isDown, BombKey)
        }
        .foreach( message =>
          webSocket.send(MessageEncoder(message).toString)
        )
      }
    }

    dom.window.addEventListener("keydown", keyHander(true) , false)
    dom.window.addEventListener("keyup", keyHander(false) , false)

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
          case Box => "#DFCF8F"
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
