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
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  def login: Action[AnyContent] = Action.async { request =>
    val payload = request.body.asJson.get
    val email = (payload \ "email").as[String]
    val password = (payload \ "password").as[String]

    userRepository
      .findUser(email)
      .map {
        case Some(user) =>
          password.isBcryptedSafeBounded(user.password) match {
            case Success(true) =>
              Ok(
                Json.obj(
                  "token" -> authService.createToken(user.id),
                  "user" -> Json.toJson(
                    Map("id" -> user.id.toString, "email" -> user.email)
                  ),
                  "message" -> "Valid credentials"
                )
              )
            case Success(false) =>
              Unauthorized(Json.obj("message" -> "Invalid credentials"))
            case Failure(e) => Unauthorized(Json.obj("message" -> e.getMessage))
          }
        case None => NotFound(Json.obj("message" -> "User not found"))
      }
      .recover { case e =>
        BadRequest(e.getMessage)
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
          .map { newUser =>
            Ok(
              Json.obj(
                "token" -> authService.createToken(newUser.id),
                "user" -> Json.toJson(
                  Map("id" -> newUser.id.toString, "email" -> newUser.email)
                ),
                "message" -> "User created successfully"
              )
            )
          }
          .recover { case e =>
            BadRequest(Json.obj("message" -> e.getMessage))
          }
      }
      case Failure(e) =>
        Future.successful(BadRequest(Json.obj("message" -> e.getMessage)))
    }
  }
}
