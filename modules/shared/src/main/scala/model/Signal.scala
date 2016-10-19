package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec()
sealed trait Signal
case class Move(agentId: Int, direction: Direction.Value) extends Signal
case class DropBomb(agentId: Int) extends Signal

object Signal
