package models

import javax.inject.Inject

import play.api.{Environment, Logger}
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.io.Source


case class Runway(id: Int
                  , airport_ref: Int
                  , airport_ident: String
                  , length_ft: Option[Int]
                  , width_ft: Option[Int]
                  , surface: String
                  , lighted: Int
                  , closed: Int
                  , le_ident: String
                  , le_latitude_deg: Option[Float]
                  , le_longitude_deg: Option[Float]
                  , le_elevation_ft: Option[Int]
                  , le_heading_degT: Option[Float]
                  , le_displaced_threshold_ft: Option[Int]
                  , he_ident: String
                  , he_latitude_deg: Option[Float]
                  , he_longitude_deg: Option[Float]
                  , he_elevation_ft: Option[Int]
                  , he_heading_degT: Option[Float]
                  , he_displaced_threshold_ft: Option[Int]
                 )

trait RunwayRepository {
  def all(): Future[Seq[Runway]]

  def runwaysByAirport(airportRef: Int): Future[Seq[Runway]]
}


