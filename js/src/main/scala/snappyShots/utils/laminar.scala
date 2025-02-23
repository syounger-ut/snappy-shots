package snappyShots

import com.raquo.laminar.api.L._
import scalacss.DevDefaults.StyleA

/*
  This implicit allows us to the ScalaCSS styles on a laminar element
 */
package object laminar {
  implicit def applyStyle(styleA: StyleA): Mod[HtmlElement] =
    cls := styleA.className.value
}
