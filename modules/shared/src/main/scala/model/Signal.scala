package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec()
sealed trait Signal
case class Move(agentId: Int, direction: Direction.Value) extends Signal
case object DropBomb extends Signal

object Signal
