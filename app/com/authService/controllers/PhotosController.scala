package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.models.Photo
import com.authService.repositories._
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

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

  /*
   * Update a photo
   * @param photoId The identifier of the photo to update
   * @return The created photo
   */
  def updatePhoto(photoId: Int): Action[AnyContent] = authAction.async {
    implicit request =>
      request.body.asJson match {
        case Some(jsValue) => Json.fromJson[Photo](jsValue) match {
          case JsSuccess(photo, _)  => photosRepository
            .update(photoId, photo)
            .map {
              case Some(updatedPhoto) => Ok(Json.toJson(updatedPhoto))
              case None               => NotFound
            }
          case _                    => Future(BadRequest)
        }
        case None => Future(BadRequest)
      }
  }
}
