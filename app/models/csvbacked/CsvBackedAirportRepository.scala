package models.csvbacked

import javax.inject.Inject

import models._
import play.api.Logger
import zamblauskas.csv.parser._
import zamblauskas.functional._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class CsvBackedAirportRepository @Inject()(csvFileFactory: CsvFileFactory, runways: RunwayRepository) extends AirportRepository {
  //Manual override because scala hates members named "type"
  private implicit val columnReads: ColumnReads[AirportRow] = (
    column("id").as[Int] and
      column("ident").as[String] and
      column("type").as[String] and
      column("name").as[String] and
      column("latitude_deg").as[Float] and
      column("longitude_deg").as[Float] and
      column("elevation_ft").asOpt[Int] and
      column("continent").as[String] and
      column("iso_country").as[String] and
      column("iso_region").as[String] and
      column("municipality").asOpt[String] and
      column("scheduled_service").asOpt[String] and
      column("gps_code").asOpt[String] and
      column("iata_code").asOpt[String] and
      column("local_code").asOpt[String] and
      column("home_link").asOpt[String] and
      column("wikipedia_link").asOpt[String] and
      column("keywords").asOpt[String]
    ) (AirportRow)

  private val airports: List[AirportRow] =
    Parser.parse[AirportRow](csvFileFactory.getFile("airports.csv").contents) match {
      case Right(as) => as.toList
      case Left(_) => {
        val message = s"Error parsing CSV data from resource airports.csv"
        Logger.error(message)
        throw new ModelError(message)
      }
    }

  private val airportRowsByCountryIso: Map[String, Seq[AirportRow]] = airports.groupBy(_.iso_country)

  override def all(): Future[Seq[Airport]] = Future.sequence {
    airports.map(ar => for (rws <- runways.runwaysByAirport(ar.id)) yield Airport(ar, rws))
  }

  override def airportsByCountryIso(countryIso: String): Future[Seq[Airport]] = {
    val airports = airportRowsByCountryIso.getOrElse(countryIso, Nil)
    Future.sequence(airports.map(ar => for (rws <- runways.runwaysByAirport(ar.id)) yield Airport(ar, rws)))
  }
}
