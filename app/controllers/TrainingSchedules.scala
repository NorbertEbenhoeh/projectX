package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import repos.TrainingScheduleRepoImpl

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Norbert on 19.05.2016.
  */
class TrainingSchedules @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  import models.TrainingScheduleFields._

  def trainingScheduleRepo = new TrainingScheduleRepoImpl(reactiveMongoApi)

  val versionFormat = OFormat[String](
    (__ \ "version").read[String],
    OWrites[String]{ s => Json.obj( "version" -> s ) }
  )


  def index = Action.async{ implicit request => // TODO: Irgendwann nur die Übungen holen die der Nutzer sehen darf
    trainingScheduleRepo.find()
      .map(exercises => Ok(Json.toJson(exercises)))
      .recover{case PrimaryUnavailableException => InternalServerError("Please install MongoDB")}
  }

  def create = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass title und owner eindeutig sind sobald ich den user aus der session kriege
    implicit request =>
      val title = (request.body \ Title).as[String]
      val owner = (request.body \ Owner).as[String]
      trainingScheduleRepo.save(BSONDocument(
        Title -> title,
        Owner -> owner,
        Version -> 1,
        CreatedAt -> DateTime.now.toString
      )).map(result => Created("Created"))
  }

  def read(id: String) = Action.async{ implicit request =>
    trainingScheduleRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(exercise => Ok(Json.toJson(exercise)))
  }


  def update(id: String) = Action.async(BodyParsers.parse.json) { // TODO: Prüfen dass title, sportstype und owner eindeutig sind sobald ich den user aus der session kriege
    implicit request =>
      val title = (request.body \ Title).asOpt[String]
      val description = (request.body \ Description).asOpt[String]
      val content = (request.body \ Content).asOpt[String]
      val oldVersion = Await.result(trainingScheduleRepo.select(BSONDocument(Id -> BSONObjectID(id))).map(exercise => Json.toJson(exercise) \ "version"), 3 seconds).as[Int]
      trainingScheduleRepo.update(BSONDocument( Id -> BSONObjectID(id)),
        BSONDocument("$set" -> BSONDocument(
          Title -> title,
          Description -> description,
          Content -> content,
          Version -> (oldVersion + 1), // TODO: Nur hochzählen falls tatsächlich geändert wurde
          UpdatedAt -> DateTime.now.toString
      ))).map(result => Accepted)
  }

  def delete(id: String) = Action.async{
    trainingScheduleRepo.remove(BSONDocument(Id -> BSONObjectID(id))).map(result => Accepted("Deleted: "+ id ))
  }

}



