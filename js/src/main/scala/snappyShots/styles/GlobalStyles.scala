package snappyShots.styles

import scalacss.DevDefaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  style(
    unsafeRoot("*")(
      boxSizing.borderBox
    ),
    inputStyles
  )

  private def inputStyles: Style.UnsafeExt = unsafeRoot("input, label")(
    padding(0.px),
    margin(0.px),
    border.none,
    fontFamily :=! "inherit",
    fontSize(16.px),
    verticalAlign.baseline
  )
}
