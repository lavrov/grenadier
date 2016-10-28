package model

import akka.stream._
import akka.stream.stage._

import scala.language.higherKinds

class LastAvailable[T] extends GraphStage[FlowShape[T, T]] {
  val in = Inlet[T]("DefaultAvailable.in")
  val out = Outlet[T]("DefaultAvailable.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private var last: Option[T] = None
    private var waiting = false

    setHandlers(in, out, new InHandler with OutHandler {
      override def onPush(): Unit = {
        val value = grab(in)
        waiting = false
        if (last.isEmpty)
          push(out, value)
        last = Some(value)
      }

      override def onPull(): Unit = {
        last.foreach(value => push(out, value))
        if (!waiting) {
          pull(in)
          waiting = true
        }
      }
    })
  }
}
