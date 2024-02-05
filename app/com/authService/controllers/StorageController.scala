package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.repositories.StorageRepository
import play.api.libs.Files
import play.api.mvc.{
  AbstractController,
  Action,
  ControllerComponents,
  MultipartFormData
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class StorageController @Inject() (
  cc: ControllerComponents,
  storageRepository: StorageRepository,
  authAction: AuthAction
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  val BUCKET_NAME = "snappy-shots"

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = {
    authAction.async(parse.multipartFormData) { request =>
      request.body.file("file") match {
        case Some(file) =>
          storageRepository
            .uploadObject(
              BUCKET_NAME,
              file.filename,
              file.ref.path.toFile
            )
            .map {
              case Success(_) => Ok("File uploaded")
              case Failure(e) => BadRequest(e.getMessage)
            }
            .recover { case e: Throwable =>
              BadRequest(e.getMessage)
            }
        case None => Future.successful(BadRequest("No file found"))
      }
    }
  }
}
