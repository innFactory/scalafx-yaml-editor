// Name of the project
name := "ScalaFX Yaml Editor"

// Project version
version := "1.0.0"

organization := "de.innfactory"

// Version of Scala used by the project
scalaVersion := "2.12.0"


resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/"
resolvers += "jcenter" at "https://jcenter.bintray.com/"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"

libraryDependencies += "org.fxmisc.richtext" % "richtextfx" % "0.7-M3"
libraryDependencies += "org.controlsfx" % "controlsfx" % "8.40.11"
libraryDependencies += "de.jensd" % "fontawesomefx-commons" % "8.15"
libraryDependencies += "de.jensd" % "fontawesomefx-fontawesome" % "4.7.0-5"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true
