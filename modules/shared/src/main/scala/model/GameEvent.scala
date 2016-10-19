package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec
sealed trait GameEvent
case class AgentMoved(agentId: Int, position: Position) extends GameEvent
case object GameCompleted extends GameEvent
object GameEvent
