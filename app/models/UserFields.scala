package models

/**
  * Created by norbert on 12.08.2016.
  */

case class UserFields(id: Int, email: String, password: String, name: String, role: Role) // TODO: role noch im object hinzufügen und Verwendungen refactoren

object UserFields {
  val Id = "_id"
  val Name = "name"
  val Password = "password"
  val EMail = "email"
  val CreatedAt = "createdAt"
}
