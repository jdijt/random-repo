package controllers

import javax.inject.Inject

import models.{CountryRepository, RunwayRepository}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits._

case class QueryData(query: String)


class Application @Inject()(val messagesApi: MessagesApi, countries: CountryRepository, runways: RunwayRepository) extends Controller with I18nSupport {

  val queryForm = Form(
    mapping("query" -> text)(QueryData.apply)(QueryData.unapply)
  )

  def index: Action[AnyContent] = Action {
    Ok(views.html.index(queryForm))
  }

  def queryPost: Action[QueryData] = Action(parse.form(queryForm)) { implicit request =>
    Redirect(routes.Application.queryGet(request.body.query))
  }

  def queryGet(query: String): Action[AnyContent] = Action.async {
    def filledForm = queryForm.fill(QueryData(query))

    countries.search(query.trim).map {
      cs => Ok(views.html.queryResult(filledForm, cs.toList))
    }
  }

  def dashboard: Action[AnyContent] = Action.async {
    val airportCounts = countries.airportCounts
    val runwayTypes = countries.runwayTypes
    val runwayIdent = runways.runwayLeIdentSummary

    for {
      acs <- airportCounts
      rts <- runwayTypes
      rid <- runwayIdent
    } yield Ok(views.html.dashboard(
      acs.takeRight(10).reverse,
      acs.take(10),
      rts,
      rid.takeRight(10).reverse
    ))
  }

}