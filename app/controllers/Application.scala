package controllers

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.bson.BSONCountCommand.{Count, CountResult}
import reactivemongo.api.commands.bson.BSONCountCommandImplicits._
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def jsonCollection = reactiveMongoApi.db.collection[JSONCollection]("exercises");
  def bsonCollection = reactiveMongoApi.db.collection[BSONCollection]("exercises");

  def index = Action {
    Logger.info("Application startup...")

    val exercises = List(
      Json.obj(
        "title" -> "Anstrengend",
        "sportstype" -> "Badminton",
        "owner" -> "Nobbi",
        "version" -> 1,
        "createdAt" -> DateTime.now.toString
      ),
      Json.obj(
        "title" -> "Lasch",
        "sportstype" -> "Soccer",
        "owner" -> "Lina",
        "version" -> 1,
        "createdAt" -> DateTime.now.toString
      )
    )

    val query = BSONDocument("title" -> BSONDocument("$exists" -> true))
    val command = Count(query)
    val result: Future[CountResult] = bsonCollection.runCommand(command)

    result.map { res =>
      val numberOfDocs: Int = res.value
      if(numberOfDocs < 1) {
        jsonCollection.bulkInsert(exercises.toStream, ordered = true).foreach(i => Logger.info("Record added."))
      }
    }

    Ok("Your database is ready.")
  }

  def cleanup = Action {
    jsonCollection.drop(true).onComplete {
      case _ => Logger.info("Database collection dropped")
    }
    Ok("Your database is clean.")
  }

}
