// app/controllers/ApiController.scala

// Make sure it's in the 'controllers' package
package com.snappyShots.controllers

import com.snappyShots.auth.AuthAction
import com.snappyShots.models.User
import com.snappyShots.repositories._
import play.api.libs.json.Json
import play.api.mvc._

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

  def addUser(): Action[AnyContent] = authAction.async {
    implicit request: Request[AnyContent] =>
      {
        val user = User(0, email = "john@email.com", password = "foobar")
        userRepository.addUser(user).map(user => Ok(Json.toJson(user)))
      }
  }
}
