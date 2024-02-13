package com.authService.controllers

import com.authService.auth.AuthAction
import com.authService.repositories.StorageRepository
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
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

  def delete(fileName: String): Action[AnyContent] = {
    authAction.async { request =>
      storageRepository
        .deleteObject(BUCKET_NAME, fileName)
        .map(_ => Ok(Json.obj("message" -> "File deleted")))
        .recover { case e: Throwable =>
          BadRequest(Json.obj("message" -> e.getMessage))
        }
    }
  }
}
