package models

/**
  * Created by norbert on 12.08.2016.
  */
object ExerciseFields {
  val Id = "_id"
  val Title = "title"
  val Sportstype = "sportstype"
  val Description = "description" // allgemeine textuelle Beschreibung der Übung
  val Steps = "steps" // In den Schritten sind die Beschreibungen
  val Owner = "owner" // Besitzer ist auch Ersteller. Ist eindeutig.
  val Version = "version"
  val CreatedAt = "createdAt"
  val UpdatedAt = "updatedAt"
}
