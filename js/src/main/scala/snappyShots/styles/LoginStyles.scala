package snappyShots.styles

import scalacss.DevDefaults._

object LoginStyles extends StyleSheet.Inline {
  import dsl._

  val inputStyles: StyleA = style(
    display.block,
    margin(0.px, auto, 10.px, 0.px),
    backgroundColor(Color("yellow")),
    color(Color("black")),
    padding(5.px),
    width(100.%%)
  )

  val buttonStyles: StyleA = style(
    display.block,
    margin(0.px, auto),
    backgroundColor(Color("white")),
    color(Color("black")),
    padding(10.px, 20.px),
    borderRadius(5.px),
    cursor.pointer,
    width(100.%%)
  )
}
