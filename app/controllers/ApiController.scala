// app/controllers/ApiController.scala

// Make sure it's in the 'controllers' package
package controllers

import auth.{AuthAction, AuthService}

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.libs.json.Json
import repositories.DataRepository

@Singleton
class ApiController @Inject()(
  cc: ControllerComponents,
  dataRepository: DataRepository,
  authAction: AuthAction,
  authService: AuthService
)
  extends AbstractController(cc) {
  
  // Create a simple 'ping' endpoint for now, so that we
  // can get up and running with a basic implementation
  def ping = Action { implicit request =>
    Ok("Hello, Scala!")
  }

  // Login
  def login = Action { implicit request =>
    Ok(Json.obj("token" -> authService.createToken()))
  }
  
  // Get a single post
  def getPost(postId: Int) = authAction { implicit request =>
    dataRepository.getPost(postId) map { post =>
      // If the post was found, return a 200 with the post data as JSON
      Ok(Json.toJson(post))
    } getOrElse NotFound    // otherwise, return Not Found
  }
  
  // Get comments for a post
  def getComments(postId: Int) = authAction { implicit request =>
    // Simply return 200 OK with the comment data as JSON.
    Ok(Json.toJson(dataRepository.getComments(postId)))
  }
}
