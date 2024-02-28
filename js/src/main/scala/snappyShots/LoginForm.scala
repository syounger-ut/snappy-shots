package snappyShots

import com.raquo.laminar.api.L.{*, given}
import scalacss.internal.mutable.GlobalRegistry
import snappyShots.laminar._
import snappyShots.styles.LoginStyles

import scala.language.postfixOps

import CssSettings._

object LoginForm:
  def appElement(): Element =
    LoginStyles.addToDocument()
    form(
      textAlign.left,
      label(
        "Email",
        input(
          tpe := "email",
          placeholder := "clark.kent@krypton.com",
          onInput.mapToValue --> { value =>
            println(s"Input value: $value")
          },
          LoginStyles.inputStyles
        )
      ),
      label(
        "Password",
        input(
          tpe := "password",
          formId := "password",
          placeholder := "fasterThanASpeedingTrain",
          onInput.mapToValue --> { value =>
            println(s"Input value: $value")
          },
          LoginStyles.inputStyles
        )
      ),
      input(
        tpe := "submit",
        value := "Login",
        onClick --> { event =>
          event.preventDefault()
          println("Login clicked")
        },
        LoginStyles.buttonStyles
      )
    )
  end appElement
end LoginForm
