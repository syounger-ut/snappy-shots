package snappyShots

import com.raquo.laminar.api.L.{_, given}
import scalacss.internal.mutable.GlobalRegistry
import snappyShots.laminar._
import snappyShots.styles.LoginStyles

import scala.language.postfixOps
import CssSettings._
import org.scalajs.dom.FormData

object LoginForm:
  case class Login(email: String, password: String)
  var login = Var(Login("", ""))

  def appElement(): Element =
    LoginStyles.addToDocument()
    form(
      textAlign.left,
      label(
        "Email",
        input(
          tpe := "email",
          placeholder("clark.kent@krypton.com"),
          onInput.mapToValue --> { value =>
            login.update(_.copy(email = value))
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
          placeholder("fasterThanASpeedingTrain"),
          onInput.mapToValue --> { value =>
            login.update(_.copy(password = value))
            println(s"Input value: $value")
          },
          LoginStyles.inputStyles
        )
      ),
      input(
        tpe := "submit",
        value := "Login",
        onClick.flatMap(ev =>
          ev.preventDefault()
          FetchStream
            .get(
              "http://localhost:8080/api/login"
            )
            .map((ev, _))
        ) --> { case (ev, responseText) =>
          println(responseText)
        },
        LoginStyles.buttonStyles
      ),
      h1(
        "",
        child.text <-- login.signal.map(_.email)
      )
    )
  end appElement
end LoginForm
