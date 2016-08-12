package controllers

import javax.inject.Inject

import models.Sportstype.Sportstype

import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import repos.ExerciseRepoImpl

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Norbert on 19.05.2016.
  */
class Exercises @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  import models.ExerciseFields._

  def exerciseRepo = new ExerciseRepoImpl(reactiveMongoApi)

  val versionFormat = OFormat[String](
    (__ \ "version").read[String],
    OWrites[String]{ s => Json.obj( "version" -> s ) }
  )


  def index = Action.async{ implicit request => // TODO: Irgendwann nur die Übungen holen die der Nutzer sehen darf
    exerciseRepo.find()
      .map(exercises => Ok(Json.toJson(exercises)))
      .recover{case PrimaryUnavailableException => InternalServerError("Please install MongoDB")}
  }

  def create = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass title, sportstype und owner eindeutig sind sobald ich den user aus der session kriege
    implicit request =>
      val title = (request.body \ Title).as[String]
      val sportstype = (request.body \ Sportstype).as[Sportstype]
      val owner = (request.body \ Owner).as[String]
      exerciseRepo.save(BSONDocument(
        Title -> title,
        Sportstype -> sportstype.toString,
        Owner -> owner,
        Version -> 1,
        CreatedAt -> DateTime.now.toString
      )).map(result => Created("Created"))
  }

  def read(id: String) = Action.async{ implicit request =>
    exerciseRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(exercise => Ok(Json.toJson(exercise)))
  }


  def update(id: String) = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass title, sportstype und owner eindeutig sind sobald ich den user aus der session kriege
    implicit request =>
      val title = (request.body \ Title).asOpt[String]
      val sportstype = (request.body \ Sportstype).asOpt[Sportstype]
      val description = (request.body \ Description).asOpt[String]
      val oldVersion = Await.result(exerciseRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(exercise => Json.toJson(exercise) \ "version"), 3 seconds).as[Int]
      exerciseRepo.update(BSONDocument( Id -> BSONObjectID(id)),
        BSONDocument("$set" -> BSONDocument(
          Title -> title,
          Sportstype -> sportstype.toString,
          Description -> description,
          Version -> (oldVersion + 1), // TODO: Nur hochzählen falls tatsächlich geändert wurde
          UpdatedAt -> DateTime.now.toString
      ))).map(result => Accepted)
  }

  def delete(id: String) = Action.async{
    exerciseRepo.remove(BSONDocument(Id -> BSONObjectID(id))).map(result => Accepted("Deleted: "+ id ))
  }

}



