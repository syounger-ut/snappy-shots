package com.authService.controllers

import com.authService.auth.AuthService
import com.authService.models.User
import com.authService.repositories.UserRepository
import play.api.libs.json.Json
import play.api.mvc._
import com.github.t3hnar.bcrypt._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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
          } else if (user.get.password != password) {
            Unauthorized(Json.obj("message" -> "Invalid credentials"))
          } else {
            Ok(Json.obj(
              "token" -> authService.createToken(user.get.id),
              "user" -> Json.toJson(Map("id" -> user.get.id.toString, "email" -> user.get.email)),
              "message" -> "User created successfully"
            ))
          }
        }
      }
  }

  def register(): Action[AnyContent] = Action.async { request =>
    val payload = request.body.asJson.get
    val email = (payload \ "email").as[String]
    val password = (payload \ "password").as[String]

    password.bcryptSafeBounded match {
      case Success(hashedPassword) => {
        userRepository
          .addUser(User(0, email, hashedPassword))
          .map {
            newUser =>
              Ok(Json.obj(
                "token" -> authService.createToken(newUser.id),
                "user" -> Json.toJson(Map("id" -> newUser.id.toString, "email" -> newUser.email)),
                "message" -> "User created successfully"
              ))
          }
      }
      case Failure(e) => Future.successful(BadRequest(Json.obj("message" -> e.getMessage)))
    }
  }
}
