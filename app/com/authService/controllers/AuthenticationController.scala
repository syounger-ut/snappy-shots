package com.authService.controllers

import com.authService.auth.AuthService
import play.api.libs.json.Json
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

import javax.inject.{Inject, Singleton}

@Singleton
class AuthenticationController @Inject() (
  cc: ControllerComponents,
  authService: AuthService
) extends AbstractController(cc) {

  def login: Action[AnyContent] = Action {
    Ok(Json.obj("token" -> authService.createToken()))
  }
}
