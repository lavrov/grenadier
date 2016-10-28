package com.github.devnfun.grenadier.protocol

import com.github.devnfun.grenadier.model.Direction
import io.circe.generic.JsonCodec

@JsonCodec
sealed trait ClientEvent

@JsonCodec
sealed trait Key
case class ArrowKey(direction: Direction.Value) extends Key
case object BombKey extends Key
object Key

case class KeyboardEvent(isDown: Boolean, key: Key) extends ClientEvent

object ClientEvent
