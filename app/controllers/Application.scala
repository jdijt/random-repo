package controllers

import javax.inject.Inject

import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

case class QueryData(query: String)

class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val queryForm = Form(
    mapping("query" -> text)(QueryData.apply)(QueryData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index(queryForm))
  }

  def queryPost = Action(parse.form(queryForm)) { implicit request =>
    Redirect(routes.Application.queryGet(request.body.query))
  }

  def queryGet(query: String) = Action {
    def filledForm = queryForm.fill(QueryData(query))

    Ok(views.html.queryResult(filledForm, Nil))
  }

  def dashboard = Action {
    Ok(views.html.dashboard())
  }

}