name := "anb"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-async" % "0.9.6-RC2",
  "com.lihaoyi" %% "scalarx" % "0.2.8",
  "org.monifu" %% "monifu" % "1.0-RC3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "de.knutwalker" %% "typed-actors" % "1.5.1-a24",
  "de.knutwalker" %% "typed-actors-creator" % "1.5.1-a24",
  "com.lihaoyi" % "ammonite-repl" % "0.4.8" % "test" cross CrossVersion.full,
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0-M1"
)

initialCommands in (Test, console) := """ammonite.repl.Repl.run("")"""