package models

import javax.inject.Inject

import play.api.{Configuration, Environment, Logger}
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.io.Source

case class Country(id: Int, code: String, name: String, continent: String, wikipedia_link: String, keywords: String)

trait CountryRepository {
  def allCountries: Future[Iterator[Country]]

  def search(nameOrCode: String): Future[Iterator[Country]]
}

class CsvBackedCountryRepository @Inject()(environment: Environment, configuration: Configuration) extends CountryRepository {
  private val countries: List[Country] =
    environment.resourceAsStream("countries.csv") match {
      case Some(is) => {
        val csv = Source.fromInputStream(is).mkString
        Parser.parse[Country](csv) match {
          case Right(countries) => countries.toList
          case Left(failure) => {
            val message = s"Error parsing CSV: ${failure.message}"
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

  override def allCountries: Future[Iterator[Country]] = Future(countries.iterator)

  override def search(nameOrCode: String): Future[Iterator[Country]] = Future {
    val lowerNameOrCode = nameOrCode.toLowerCase
    countries.filter {
      c => c.name.toLowerCase.contains(lowerNameOrCode) || c.code.toLowerCase.contains(lowerNameOrCode)
    }.iterator
  }


}
