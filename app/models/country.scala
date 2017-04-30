package models

import javax.inject.Inject

import play.api.{Configuration, Environment, Logger}
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.io.Source

case class Country(id: Int, code: String, name: String, continent: String, wikipedia_link: String, keywords: String)

trait CountryRepository {
  def all: Future[Seq[Country]]

  def search(nameOrCode: String): Future[Seq[Country]]
}

class CsvBackedCountryRepository @Inject()(environment: Environment) extends CountryRepository {
  private val countries: List[Country] =
    environment.resourceAsStream("countries.csv") match {
      case Some(is) => {
        val csv = Source.fromInputStream(is).mkString
        Parser.parse[Country](csv) match {
          case Right(cs) => cs.toList
          case Left(failure) => {
            val message = s"Error parsing countries CSV: ${failure.message}"
            Logger.error(message)
            throw new ModelError(message)
          }
        }
      }
      case None => {
        val message = s"Error finding countries CSV."
        Logger.error(message)
        throw new ModelError(message)
      }
    }

  override def all: Future[Seq[Country]] = Future(countries)

  override def search(nameOrCode: String): Future[Seq[Country]] = Future {
    val lowerNameOrCode = nameOrCode.toLowerCase
    countries.filter {
      c => c.name.toLowerCase.contains(lowerNameOrCode) || c.code == nameOrCode
    }
  }


}
