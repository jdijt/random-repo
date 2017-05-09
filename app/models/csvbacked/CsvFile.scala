package models.csvbacked

import javax.inject.Inject

import models.ModelError
import play.api.{Environment, Logger}

import scala.io.Source

trait CsvFile {
  val contents: String
}

class CsvFileImpl (environment: Environment, csvResource: String) extends CsvFile {
  override lazy val contents: String = environment.resourceAsStream(csvResource) match {
    case Some(is) => Source.fromInputStream(is).mkString
    case None => {
      val message = s"Error opening CSV data from resource $csvResource"
      Logger.error(message)
      throw new ModelError(message)
    }
  }
}

trait CsvFileFactory {
  def getFile(csvResource: String): CsvFile
}

class CsvFileFactoryImpl @Inject()(environment: Environment) extends CsvFileFactory {
  def getFile(csvResource: String): CsvFile = new CsvFileImpl(environment, csvResource)
}
