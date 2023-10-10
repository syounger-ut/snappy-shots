package controllers

import auth.{AuthAction, AuthService}

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.libs.json.Json
import repositories.DataRepository

@Singleton
class AuthenticationController @Inject()(
  cc: ControllerComponents,
  authService: AuthService
) extends AbstractController(cc) {
  
  def login: Action[AnyContent] = Action {
    Ok(Json.obj("token" -> authService.createToken()))
  }
}
