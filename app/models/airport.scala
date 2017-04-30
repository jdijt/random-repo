package models

import javax.inject.Inject

import play.api.{Configuration, Environment, Logger}
import zamblauskas.csv.parser._
import zamblauskas.functional._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.io.Source

case class Airport(id: Int
                   , ident: String
                   , aType: String
                   , name: String
                   , latitude: Float
                   , longitude: Float
                   , elevation: Option[Int]
                   , continent: String
                   , isoCountry: String
                   , isoRegion: String
                   , municipality: Option[String]
                   , scheduledService: Option[String]
                   , gpsCode: Option[String]
                   , iataCode: Option[String]
                   , localCode: Option[String]
                   , home: Option[String]
                   , wikipedia: Option[String]
                   , keywords: Option[String]
                  )

trait AirportRepository {
  def all(): Future[Seq[Airport]]
  def airportsByCountryIso(countryIso: String): Future[Seq[Airport]]
}


class CsvBackedAirportRepository @Inject()(environment: Environment) extends AirportRepository {
  //Manual override because scala hates members named "type"
  implicit val airportReads: ColumnReads[Airport] = (
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
  )(Airport)

  private val airports: List[Airport] =
    environment.resourceAsStream("airports.csv") match {
      case Some(is) => {
        val csv = Source.fromInputStream(is).mkString
        Parser.parse[Airport](csv) match {
          case Right(airports) => airports.toList
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


  override def all(): Future[Seq[Airport]] = Future(airports)
  override def airportsByCountryIso(countryIso: String): Future[Seq[Airport]] = Future{
    airports.filter(_.isoCountry == countryIso)
  }
}