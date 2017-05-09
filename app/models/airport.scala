package models

import scala.concurrent.Future

case class AirportRow(id: Int
                      , ident: String
                      , aType: String
                      , name: String
                      , latitude: Float
                      , longitude: Float
                      , elevation: Option[Int]
                      , continent: String
                      , iso_country: String
                      , iso_region: String
                      , municipality: Option[String]
                      , scheduled_service: Option[String]
                      , gps_code: Option[String]
                      , iata_code: Option[String]
                      , local_code: Option[String]
                      , home_link: Option[String]
                      , wikipedia_link: Option[String]
                      , keywords: Option[String]
                     )

case class Airport(airportData: AirportRow, runways: Seq[Runway])

trait AirportRepository {
  def all(): Future[Seq[Airport]]

  def airportsByCountryIso(countryIso: String): Future[Seq[Airport]]
}


