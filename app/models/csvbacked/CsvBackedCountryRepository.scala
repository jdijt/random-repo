package models.csvbacked

import javax.inject.Inject

import models._
import play.api.Logger
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class CsvBackedCountryRepository @Inject()(airports: AirportRepository, csvFileFactory: CsvFileFactory) extends CountryRepository {
  private val countries: List[CountryRow] =
    Parser.parse[CountryRow](csvFileFactory.getFile("countries.csv").contents) match {
      case Right(countries) => countries.toList
      case Left(_) => {
        val message = s"Error parsing CSV data from resource countries.csv"
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