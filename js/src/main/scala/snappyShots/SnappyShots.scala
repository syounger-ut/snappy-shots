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

def inputStyles = List(
  display.block,
  margin := "0 auto"
)

def buttonStyles = List(
  display.block,
  margin := "0 auto",
  backgroundColor := "white",
  color := "black",
  padding := "10px 20px",
  border := "none",
  borderRadius := "5px",
  cursor.pointer
)

object Main:
  def appElement(): Element =
    div(
      h1("Snappy Shots"),
      form(
        input(
          tpe := "email",
          placeholder := "Email",
          onInput.mapToValue --> { value =>
            println(s"Input value: $value")
          },
          inputStyles
        ),
        input(
          tpe := "password",
          placeholder := "Password",
          onInput.mapToValue --> { value =>
            println(s"Input value: $value")
          },
          inputStyles
        ),
        input(
          tpe := "submit",
          value := "Login",
          onClick --> { event =>
            event.preventDefault()
            println("Login clicked")
          },
          buttonStyles
        )
      ),
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
