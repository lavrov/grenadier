package model

import akka.stream._
import akka.stream.stage._
import cats.{Applicative, Monoid}

import scala.language.higherKinds

class Accumulate[T, F[_]](implicit M: Monoid[F[T]], A: Applicative[F]) extends GraphStage[FlowShape[T, F[T]]] {
  val in = Inlet[T]("DefaultAvailable.in")
  val out = Outlet[F[T]]("DefaultAvailable.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private var current = M.empty

    setHandlers(in, out, new InHandler with OutHandler {
      override def onPush(): Unit = {
        current = M.combine(current, A pure grab(in))
        pull(in)
      }

      override def onPull(): Unit = {
        push(out, current)
        current = M.empty
      }
    })

    override def preStart(): Unit = {
      pull(in)
    }
  }

}
