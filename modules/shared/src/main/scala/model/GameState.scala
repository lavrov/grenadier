package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec
case class GameState(
    status: GameState.Status,
    stage: Stage
)

object GameState {

  @JsonCodec
  sealed trait Status
  case object Completed extends Status
  case object Active extends Status
  object Status

}

