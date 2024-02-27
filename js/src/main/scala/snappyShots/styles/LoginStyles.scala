package snappyShots.styles

import scalacss.DevDefaults._

object LoginStyles extends StyleSheet.Inline {
  import dsl._

  val inputStyles = style(
    display.block,
    backgroundColor(Color("yellow"))
  )

  val buttonStyles = style(
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
