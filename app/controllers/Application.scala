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

  def index = Action { implicit request =>
    Ok(views.html.index(queryForm))
  }

  def queryPost = Action(parse.form(queryForm)) { implicit request =>
    Redirect(routes.Application.queryGet(request.body.query))
  }

  def queryGet(query: String) = Action.async {
    def filledForm = queryForm.fill(QueryData(query))

    countries.search(query).map(cs => Ok(views.html.queryResult(filledForm, cs.toList)))
  }

  def dashboard = Action.async {
    val airportCounts = for {
      countries <- countries.all
    } yield countries.map(c => (c.countryData.name, c.airports.size)).sortBy{case (_, count) => count}

    val runwayTypes = for {
      countries <- countries.all
    } yield countries.map(c => (c.countryData.name, c.airports.flatMap(_.runways.map(_.surface)).toSet))

    val runwayIdent = for {
      runways <- runways.all()
    } yield runways.groupBy(_.le_ident).map{case (id, rws) => (id, rws.size)}.toSeq.sortBy{case (_, count) => count}

    val completeResult = for {
      airportCounts <- airportCounts
      runwayTypes <- runwayTypes
      runwayIdent <- runwayIdent
    } yield(airportCounts, runwayTypes, runwayIdent)

    completeResult.map{
      case (apCounts, rwsType, rwsIdent) => Ok(views.html.dashboard(apCounts.takeRight(10).reverse, apCounts.take(10), rwsType, rwsIdent.takeRight(10).reverse))
    }
  }

}