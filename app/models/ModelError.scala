package models

class ModelError(message: String = "", cause: Throwable = None.orNull) extends Exception(message, cause) {

}
