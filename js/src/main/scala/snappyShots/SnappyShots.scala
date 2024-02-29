package snappyShots

import com.raquo.laminar.api.L.{*, given}
import scalacss.DevDefaults._
import snappyShots.laminar._
import snappyShots.styles._

import scalacss.ProdDefaults._ // Always use prod settings

// This will choose between dev/prod depending on:
//   1. `sbt -Dscalacss.mode=dev` or `sbt -Dscalacss.mode=prod`
//   2. Defaults to dev-mode unless in `fullOptJS`
//
val CssSettings = scalacss.devOrProdDefaults

import CssSettings._

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import org.scalajs.dom

import scala.language.postfixOps

// import javascriptLogo from "/javascript.svg"
@js.native @JSImport("/javascript.svg", JSImport.Default)
val javascriptLogo: String = js.native

@main
def SnappyShots(): Unit =
  GlobalStyles.addToDocument()

  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

object Main:
  def appElement(): Element =
    div(
      h1("Snappy Shots"),
      LoginForm.appElement(),
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
