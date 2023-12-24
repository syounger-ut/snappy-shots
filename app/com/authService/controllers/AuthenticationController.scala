package com.authService.controllers

import com.authService.auth.AuthService
import com.authService.models.User
import com.authService.repositories.UserRepository
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuthenticationController @Inject() (
  cc: ControllerComponents,
  authService: AuthService,
  userRepository: UserRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def login(email: String, password: String): Action[AnyContent] = Action.async {
    userRepository
      .findUser(email)
      .map {
        user => {
          if (user.isEmpty) {
            NotFound(Json.obj("message" -> "User not found"))
          }

          if (user.get.password != password) {
            Unauthorized(Json.obj("message" -> "Invalid credentials"))
          }

          Ok(Json.obj(
            "token" -> authService.createToken(user.get.id),
            "user" -> Json.toJson(user),
            "message" -> "User created successfully"
          ))
      }
  }
  }

  def register(email: String, password: String): Action[AnyContent] = Action.async {
    userRepository
      .addUser(User(0, email, password))
      .map {
        newUser =>
          Ok(Json.obj(
            "token" -> authService.createToken(newUser.id),
            "user" -> Json.toJson(newUser),
            "message" -> "User created successfully"
          ))
      }
  }
}
