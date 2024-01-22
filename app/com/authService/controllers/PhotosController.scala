package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.repositories._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PhotosController @Inject() (
  cc: ControllerComponents,
  photosRepository: PhotoRepository,
  authAction: AuthAction
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getPhotos: Action[AnyContent] = authAction.async {
    implicit request =>
      photosRepository.list() map {
        case List() => NotFound
        case photos => Ok(Json.toJson(photos))
      }
  }

  /*
   * Get a photo by its identifier
   * @param photoId The identifier of the photo to find
   * @return The photo if found, otherwise not found
   */
  def getPhoto(photoId: Int): Action[AnyContent] = authAction.async {
    implicit request =>
      photosRepository.get(photoId) map {
        case Some(photo) => Ok(Json.toJson(photo))
        case None        => NotFound
      }
  }
}
