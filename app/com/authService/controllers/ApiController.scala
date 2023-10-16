// app/controllers/ApiController.scala

// Make sure it's in the 'controllers' package
package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.repositories.DataRepository
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject() (
  cc: ControllerComponents,
  dataRepository: DataRepository,
  authAction: AuthAction
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Create a simple 'ping' endpoint for now, so that we
  // can get up and running with a basic implementation
  def ping: Action[AnyContent] = Action { implicit request =>
    Ok("Hello, Scala!")
  }

  // Get a single post
  def getPost(postId: Int): Action[AnyContent] = authAction { implicit request =>
    dataRepository.getPost(postId) map { post =>
      Ok(Json.toJson(post))
    } getOrElse NotFound
  }

  // Get comments for a post
  def getComments(postId: Int): Action[AnyContent] = authAction { implicit request =>
    // Simply return 200 OK with the comment data as JSON.
    Ok(Json.toJson(dataRepository.getComments(postId)))
  }
}
