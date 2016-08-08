package models

import play.api.libs.json._

/**
  * Created by Norbert on 04.06.2016.
  */
object Sportstype extends Enumeration {
  type Sportstype = Value
  val Badminton, Soccer, Tennis = Value

  implicit val enumFormat = new Format[Sportstype] {
    def reads(json: JsValue) = JsSuccess(Sportstype.withName(json.as[String]))
    def writes(enum: Sportstype) = JsString(enum.toString)
  }

}

