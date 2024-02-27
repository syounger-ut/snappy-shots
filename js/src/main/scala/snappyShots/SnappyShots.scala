package snappyShots

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

// import javascriptLogo from "/javascript.svg"
@js.native @JSImport("/javascript.svg", JSImport.Default)
val javascriptLogo: String = js.native

@main
def SnappyShots(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

object Main:
  def appElement(): Element =
    div(
      a(
        href := "https://vitejs.dev",
        target := "_blank",
        img(src := "/vite.svg", className := "logo", alt := "Vite logo")
      ),
      a(
        href := "https://developer.mozilla.org/en-US/docs/Web/JavaScript",
        target := "_blank",
        img(
          src := javascriptLogo,
          className := "logo vanilla",
          alt := "JavaScript logo"
        )
      ),
      h1("Hello Laminar!"),
      counterButton(),
      p(className := "read-the-docs", "Click on the Vite logo to learn more")
    )
  end appElement
end Main

def counterButton(): Element =
  val counter = Var(0)
  button(
    tpe := "button",
    "count is ",
    child.text <-- counter,
    onClick --> { event => counter.update(c => c + 1) }
  )
end counterButton
