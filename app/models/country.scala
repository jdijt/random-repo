package models

import javax.inject.Inject

import play.api.{Environment, Logger}
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

case class CountryRow(id: Int, code: String, name: String, continent: String, wikipedia_link: String, keywords: String)

case class Country(countryData: CountryRow, airports: Seq[Airport])

trait CountryRepository {
  def all: Future[Seq[Country]]

  def search(nameOrCode: String): Future[Seq[Country]]
}

class CsvBackedCountryRepository @Inject()(environment: Environment, airports: AirportRepository) extends CountryRepository {
  private val countries: List[CountryRow] =
    environment.resourceAsStream("countries.csv") match {
      case Some(is) => {
        val csv = Source.fromInputStream(is).mkString
        Parser.parse[CountryRow](csv) match {
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

  override def all: Future[Seq[Country]] = Future.sequence {
    countries.map(c => for (aps <- airports.airportsByCountryIso(c.code)) yield Country(c, aps))
  }

  override def search(nameOrCode: String): Future[Seq[Country]] = {
    val lowerNameOrCode = nameOrCode.toLowerCase
    val rows = countries.filter(c => c.name.toLowerCase.contains(lowerNameOrCode) || c.code == nameOrCode)
    Future.sequence(rows.map(c => for (aps <- airports.airportsByCountryIso(c.code)) yield Country(c, aps)))
  }


}
