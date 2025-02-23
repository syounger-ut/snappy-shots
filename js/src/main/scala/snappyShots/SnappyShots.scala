package snappyShots

import com.raquo.laminar.api.L.{*, given}
import scalacss.DevDefaults.*
import snappyShots.laminar.*
import snappyShots.styles.*
import scalacss.ProdDefaults.*
import scalacss.internal.mutable.GlobalRegistry // Always use prod settings

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
  private val loggedIn = Var(false)

  def appElement(): Element =
    println("APP ELEMENT")
    setIsLoggedIn()
    val loginForm = div(
      h1("Snappy Shots"),
      LoginForm.appElement(setIsLoggedIn)
    )
    val welcomePage = div(
      h1("Snappy Shots"),
      div("Welcome to Snappy Shots!")
    )

    div(
      child <-- loggedIn.signal.map {
        case true  => welcomePage
        case false => loginForm
      }
    )
  end appElement

  private def setIsLoggedIn(): Unit =
    loggedIn.set(isLoggedIn)
  end setIsLoggedIn

  private def isLoggedIn: Boolean =
    dom.window.localStorage.getItem("token") != null
  end isLoggedIn
end Main
