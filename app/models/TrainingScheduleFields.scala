package models

/**
  * Created by norbert on 12.08.2016.
  */
object TrainingScheduleFields {
  val Id = "_id"
  val Title = "title"
  val Description = "description"
  val Content = "content" // Enthält weitere Trainingspläne oder Übungen
  val Owner = "owner" // Besitzer ist auch Ersteller. Ist eindeutig.
  val Version = "version"
  val CreatedAt = "createdAt"
  val UpdatedAt = "updatedAt"

}
