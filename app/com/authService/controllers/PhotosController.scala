package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.models.Photo
import com.authService.repositories._
import play.api.libs.json._
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

@Singleton
class PhotosController @Inject() (
  cc: ControllerComponents,
  photosRepository: PhotoRepository,
  authAction: AuthAction
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /*
   * Create a photo
   * @return The created photo
   */
  def createPhoto: Action[AnyContent] = authAction.async { implicit request =>
    request.body.asJson match {
      case Some(jsValue) =>
        Try(parsePhoto(jsValue, request.userId)) match {
          case Success(photo) =>
            photosRepository.create(photo) map { createdPhoto =>
              Created(Json.obj("photo" -> Json.toJson(createdPhoto)))
            }
          case _ => Future(BadRequest(Json.obj("message" -> "Invalid photo")))
        }
      case _ =>
        Future(
          BadRequest(
            Json.obj("message" -> "Invalid photo, no payload was provided")
          )
        )
    }
  }

  def getPhotos: Action[AnyContent] = authAction.async { implicit request =>
    photosRepository.list(request.userId) map {
      case List() =>
        NotFound(
          Json.obj("message" -> "Photos not found", "photos" -> Json.arr())
        )
      case photos => Ok(Json.obj("photos" -> Json.toJson(photos)))
    }
  }

  /*
   * Get a photo by its identifier
   * @param photoId The identifier of the photo to find
   * @return The photo if found, otherwise not found
   */
  def getPhoto(photoId: Int): Action[AnyContent] = authAction.async {
    implicit request =>
      photosRepository.get(photoId, request.userId) map {
        case Some(photo) => Ok(Json.toJson(photo))
        case None        => NotFound(Json.obj("message" -> "Photo not found"))
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
        case Some(jsValue) =>
          Json.fromJson[Photo](jsValue) match {
            case JsSuccess(photo, _) =>
              photosRepository
                .update(photoId, request.userId, photo)
                .map {
                  case Some(updatedPhoto) => Ok(Json.toJson(updatedPhoto))
                  case None =>
                    NotFound(Json.obj("message" -> "Photo not updated"))
                }
            case _ => Future(BadRequest)
          }
        case None => Future(BadRequest)
      }
  }

  /*
   * Delete a photo
   * @param photoId The identifier of the photo to delete
   * @return The created photo
   */
  def deletePhoto(photoId: Int): Action[AnyContent] = authAction.async {
    implicit request =>
      photosRepository.delete(photoId, request.userId) map {
        case 1 => Ok
        case _ => NotFound(Json.obj("message" -> "Photo not deleted"))
      }
  }

  private def parsePhoto(json: JsValue, creatorId: Long): Photo = {
    Photo(
      id = 0,
      title = (json \ "title").as[String],
      description = (json \ "description").asOpt[String],
      source = (json \ "source").asOpt[String],
      creator_id = creatorId,
      created_at = None,
      updated_at = None
    )
  }
}
