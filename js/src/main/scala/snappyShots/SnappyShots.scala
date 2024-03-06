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

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import org.scalajs.dom

import scala.language.postfixOps

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
      LoginForm.appElement()
    )
  end appElement
end Main
