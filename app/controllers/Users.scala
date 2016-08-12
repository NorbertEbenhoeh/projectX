package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import repos.UserRepoImpl


/**
  * Created by Norbert on 19.05.2016.
  */
class Users @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  import models.UserFields._

  def userRepo = new UserRepoImpl(reactiveMongoApi)

  def create = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass name und email eindeutig sind beim Anlegen
    implicit request =>
      val name = (request.body \ Name).as[String]
      val password = (request.body \ Password).as[String]
      val email = (request.body \ EMail).as[String]
      userRepo.save(BSONDocument(
        Name -> name,
        Password -> password,
        EMail -> email,
        CreatedAt -> DateTime.now.toString
      )).map(result => Created("Created"))
  }

  def read(id: String) = Action.async{ implicit request =>
    userRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(exercise => Ok(Json.toJson(exercise)))
  }


  def update(id: String) = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass name und email eindeutig sind beim updaten
    implicit request =>
      val name = (request.body \ Name).asOpt[String]
      val password = (request.body \ Password).asOpt[String]
      val email = (request.body \ EMail).asOpt[String]
      userRepo.update(BSONDocument( Id -> BSONObjectID(id)),
        BSONDocument("$set" -> BSONDocument(
          Name -> name,
          Password -> password,
          EMail -> EMail
      ))).map(result => Accepted)
  }

  def delete(id: String) = Action.async{
    userRepo.remove(BSONDocument(Id -> BSONObjectID(id))).map(result => Accepted("Deleted: "+ id ))
  }

}



