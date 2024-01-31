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
      request.body
        .file("file")
        .map { file =>
          Future
            .fromTry(
              storageRepository
                .uploadObject(
                  BUCKET_NAME,
                  file.filename,
                  file.ref.path.toFile
                )
            )
            .map { _ => Ok("File uploaded") }
            .recover { case e: IllegalStateException =>
              BadRequest(e.getMessage)
            }
        }
        .getOrElse {
          throw new IllegalStateException("File upload failed")
        }
    }
  }
}
