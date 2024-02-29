package snappyShots

import com.raquo.laminar.api.L.{_, given}
import CssSettings._
import upickle.default._
import snappyShots.laminar._
import snappyShots.styles.LoginStyles

object LoginForm:
  case class Login(email: String, password: String) derives ReadWriter

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
            .post(
              "http://localhost:8080/auth/login",
              _.headers("Content-Type" -> "application/json"),
              _.body(write(login.now()))
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
