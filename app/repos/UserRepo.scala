package repos

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.{JSONCollection, JsCursor}, JsCursor._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Norbert on 19.05.2016.
  */
trait UserRepo {
  def find()(implicit ec: ExecutionContext): Future[List[JsObject]]

  def select(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Option[JsObject]]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

}

class UserRepoImpl @Inject() (reactiveMongoApi: ReactiveMongoApi) extends UserRepo {

  protected def collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("user")

  override def find()(implicit ec: ExecutionContext): Future[List[JsObject]] = {
    collection.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]()
  }

  override def select(selector: BSONDocument)(implicit ec: ExecutionContext): Future[Option[JsObject]] = {
    collection.find(selector).one[JsObject]
  }

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)
  }

}