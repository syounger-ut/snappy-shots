// app/controllers/ApiController.scala

// Make sure it's in the 'controllers' package
package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.models.User
import com.authService.repositories.{DataRepository, UserRepository}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject() (
  cc: ControllerComponents,
  dataRepository: DataRepository,
  userRepository: UserRepository,
  authAction: AuthAction
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // Create a simple 'ping' endpoint for now, so that we
  // can get up and running with a basic implementation
  def ping: Action[AnyContent] = Action { implicit request =>
    Ok("Hello, Scala!")
  }

  // Get a single post
  def getPost(postId: Int): Action[AnyContent] = authAction {
    implicit request =>
      dataRepository.getPost(postId) map { post =>
        Ok(Json.toJson(post))
      } getOrElse NotFound
  }

  // Get comments for a post
  def getComments(postId: Int): Action[AnyContent] = authAction {
    implicit request =>
      // Simply return 200 OK with the comment data as JSON.
      Ok(Json.toJson(dataRepository.getComments(postId)))
  }

  def addUser(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    val user = User(id = 0, firstName = "John", lastName = "Doe", email = "jdoe@email.com")
    userRepository.addUser(user).map(_ => Ok("User added"))
  } }
}
