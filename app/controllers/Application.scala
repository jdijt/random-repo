package controllers

import javax.inject.Inject

import models.{AirportRepository, CountryRepository, RunwayRepository}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class QueryData(query: String)

class Application @Inject()(val messagesApi: MessagesApi, countries: CountryRepository, airports: AirportRepository, runways: RunwayRepository) extends Controller with I18nSupport {

  val queryForm = Form(
    mapping("query" -> text)(QueryData.apply)(QueryData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index(queryForm))
  }

  def queryPost = Action(parse.form(queryForm)) { implicit request =>
    Redirect(routes.Application.queryGet(request.body.query))
  }

  def queryGet(query: String) = Action.async {
    def filledForm = queryForm.fill(QueryData(query))

    def result = countries.search(query).flatMap{
      cs => Future.sequence(cs.map{
        c => for{
          airports_runways <- airports.airportsByCountryIso(c.code).flatMap {
            as => Future.sequence(as.map(a => for (rs <- runways.runwaysByAirport(a.id)) yield (a, rs.toList)))
          }
        } yield (c, airports_runways.toList)})
    }

    result.map(cs => Ok(views.html.queryResult(filledForm, cs.toList)))
  }

  def dashboard = Action {
    Ok(views.html.dashboard())
  }

}