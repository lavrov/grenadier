package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec
sealed trait StageEvent
case class AgentMoved(agentId: Int, position: Position) extends StageEvent
object StageEvent
