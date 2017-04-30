import com.google.inject.AbstractModule
import models._

class Module extends AbstractModule {
  def configure() = {
    //CSV backed repositories are heavy to load.
    //This causes them to be initialised at the start of the app, instead of lazily at the first request.
    bind(classOf[CountryRepository]).to(classOf[CsvBackedCountryRepository]).asEagerSingleton()
    bind(classOf[AirportRepository]).to(classOf[CsvBackedAirportRepository]).asEagerSingleton()
  }

}
