package models.csvbacked

import javax.inject.Inject

import models.{ModelError, Runway, RunwayRepository}
import play.api.Logger
import zamblauskas.csv.parser._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future


class CsvBackedRunwayRepository @Inject()(csvFileFactory: CsvFileFactory) extends RunwayRepository {
  private val runways: List[Runway] =
    Parser.parse[Runway](csvFileFactory.getFile("runways.csv").contents) match {
      case Right(rws) => rws.toList
      case Left(_) => {
        val message = s"Error parsing CSV data from resource countries.csv"
        Logger.error(message)
        throw new ModelError(message)
      }
    }

  private val runwaysByAirport: Map[Int, Seq[Runway]] = runways.groupBy(_.airport_ref)

  override def all(): Future[Seq[Runway]] = Future(runways)

  override def runwaysByAirport(airportRef: Int): Future[Seq[Runway]] = Future {
    runwaysByAirport.getOrElse(airportRef, Nil)
  }

  override def runwayLeIdentSummary(): Future[Seq[(String, Int)]] = Future {
    runways.groupBy(_.le_ident).map {
      case (id, rws) => (id, rws.size)
    }.toSeq.sortBy {
      case (_, count) => count
    }
  }
}
