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


