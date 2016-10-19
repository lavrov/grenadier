package com.github.devnfun.grenadier.protocol

import com.github.devnfun.grenadier.model.Direction
import io.circe.generic.JsonCodec

@JsonCodec
sealed trait ClientEvent

case class ArrowPressed(direction: Direction.Value) extends ClientEvent
case object BombDropped extends ClientEvent

object ClientEvent
