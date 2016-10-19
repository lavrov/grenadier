package com.github.devnfun.grenadier.model

import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.circe.generic.JsonCodec

@JsonCodec
case class Position(x: Int, y: Int) {
  def up(increment: Int) = copy(y = y - increment)
  def down(increment: Int) = copy(y = y + increment)
  def left(increment: Int) = copy(x = x - increment)
  def right(increment: Int) = copy(x = x + increment)
}

object Direction extends Enumeration {
  implicit val DirectionEncoder = Encoder.enumEncoder(Direction)
  implicit val DirectionDecoder = Decoder.enumDecoder(Direction)

  val Up, Down, Left, Right = Value
}

case class Bomb(countDown: Long, power: Int, position: Position, direction: Option[Direction.Value])

case class Agent(position: Position, direction: Direction.Value)

@JsonCodec sealed trait Cell
case object Abyss extends Cell
case object Ground extends Cell
//case class Arrow(direction: Direction.Value) extends Cell
case object Ice extends Cell
//case object Box extends Cell
//case object Wall extends Cell
object Cell

@JsonCodec
case class Stage(map: Map[Position, Cell], agents: Map[Int, Agent])

object Stage {
  import io.circe.generic.auto._

  implicit val StageMapKeyEncoder = KeyEncoder.instance[Position](p => s"${p.x},${p.y}")
  implicit val MapKeyDecoder = KeyDecoder.instance[Position]{ str =>
    val Array(x, y) = str.split(',').map(_.toInt)
    Some(Position(x, y))
  }
}
