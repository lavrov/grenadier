package com.github.devnfun.grenadier
package utils

import model._

object MapFactory {
  def simple(string: String): Map[Position, Cell] = {
    val rows = string.lines.toList
    for {
      (row, rowIndex) <- rows.zipWithIndex
      (cellChar, columnIndex) <- row.zipWithIndex }
    yield {
      val position = Position(columnIndex, rowIndex)
      val cell = charToCell(cellChar)
      (position, cell)
    }
  }.toMap.withDefaultValue(Abyss)

  private val charToCell = Map(
    '□' -> Abyss,
//    '←' -> Arrow(Direction.Left),
//    '↑' -> Arrow(Direction.Up),
//    '→' -> Arrow(Direction.Right),
//    '↓' -> Arrow(Direction.Down),
    ' ' -> Ground,
    '■' -> Wall,
    '#' -> Box,
    'i' -> Ice
  )
}
