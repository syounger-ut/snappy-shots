package snappyShots

import com.raquo.laminar.api.L.{*, given}
import scalacss.internal.mutable.GlobalRegistry
import snappyShots.laminar._
import snappyShots.styles.LoginStyles

import scala.language.postfixOps

object LoginForm:
  def appElement(): Element =
    form(
      input(
        tpe := "email",
        placeholder := "Email",
        onInput.mapToValue --> { value =>
          println(s"Input value: $value")
        },
        LoginStyles.inputStyles
      ),
      input(
        tpe := "password",
        placeholder := "Password",
        onInput.mapToValue --> { value =>
          println(s"Input value: $value")
        },
        LoginStyles.inputStyles
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
