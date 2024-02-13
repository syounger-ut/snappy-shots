package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.models.Photo
import com.authService.repositories._
import play.api.libs.Files
import play.api.libs.json._
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
   * Upload a photo object
   * @param photoId The identifier of the photo to upload the object to
   * @return The photo upload state
   */
  def uploadPhotoObject(
    photoId: Int
  ): Action[MultipartFormData[Files.TemporaryFile]] = {
    authAction.async(parse.multipartFormData) { request =>
      request.body.file("file") match {
        case Some(file) =>
          photosRepository
            .uploadObject(
              photoId,
              request.userId,
              file.filename,
              file.ref.path.toFile
            )
            .map {
              case Success(_) => Ok(Json.obj("message" -> "File uploaded"))
              case Failure(e) => BadRequest(Json.obj("message" -> e.getMessage))
            }
            .recover { case e: Throwable =>
              BadRequest(Json.obj("message" -> e.getMessage))
            }
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "No file found")))
      }
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
      creator_id = creatorId
    )
  }
}
