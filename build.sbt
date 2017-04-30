name := "LunatechAssignment"

version := "1.0"

lazy val `lunatechassignment` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc, cache, ws, specs2 % Test,
  "zamblauskas" %% "scala-csv-parser" % "0.11.4")

unmanagedResourceDirectories in Compile <+= baseDirectory(_ / "resources")

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers ++= Seq(
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  "zamblauskas-bintray" at "https://dl.bintray.com/zamblauskas/maven"
)
