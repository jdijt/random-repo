import com.google.inject.AbstractModule
import models._
import models.csvbacked._


class Module extends AbstractModule {
  def configure(): Unit = {
    //CSV backed repositories are heavy to load.
    //asEagerSingleton causes them to be initialised at the start of the app, instead of lazily at the first request.
    bind(classOf[CountryRepository]).to(classOf[CsvBackedCountryRepository]).asEagerSingleton()
    bind(classOf[AirportRepository]).to(classOf[CsvBackedAirportRepository]).asEagerSingleton()
    bind(classOf[RunwayRepository]).to(classOf[CsvBackedRunwayRepository]).asEagerSingleton()

    bind(classOf[CsvFileFactory]).to(classOf[CsvFileFactoryImpl])
  }

}
