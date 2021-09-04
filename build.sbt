//


lazy val model = (project in file("model"))
  .settings(
    commonSettings,
    testSettings,
    assemblySettings,
    runLocalSettings,
    name := "model"
  )

lazy val analytics = (project in file("analytics"))
  .settings(
    commonSettings,
    testSettings,
    assemblySettings,
    runLocalSettings,
    name := "analytics"
  ).dependsOn(model)

// Overarching project
lazy val root = (project in file("."))
  .settings(
    name := "schiphol-assessment"
  )
  .aggregate(
    model
  )


// Libraries
val sparkVersion = "3.1.2"
val slf4jVersion = "1.7.32"
val pureconfigVersion = "0.16.0"

val sparkLibs = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-avro" % sparkVersion % Provided
)

val loggingLibs = Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion % Provided,
  "org.slf4j" % "jul-to-slf4j" % slf4jVersion % Provided,
  "org.slf4j" % "slf4j-log4j12" % slf4jVersion % Provided,
  "commons-logging" % "commons-logging" % "1.2"
)

val configLibs = Seq(
  "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
  "com.github.pureconfig" %% "pureconfig-joda" % pureconfigVersion
)

val testingLibs = Seq(
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)

// Settings for each module/subproject
lazy val commonSettings = Seq(
  organization := "xyz.graphiq",
  scalaVersion := "2.12.14",
  libraryDependencies ++= sparkLibs ++ loggingLibs ++ configLibs ++ testingLibs,
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
  )
)

// Test settings
lazy val testSettings = Seq(
  Test / testOptions += Tests.Argument("-oDT"),
  Test / parallelExecution := false
)


// Assembly options
lazy val assemblySettings = Seq(
  assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false),
  assembly / assemblyOutputPath := baseDirectory.value / "output" / (assembly / assemblyJarName).value,
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", _@_*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  },
  assembly / logLevel := sbt.util.Level.Error,
  assembly / test := {},
  pomIncludeRepository := { _ => false }
)

// Include "provided" dependencies back to default run task
lazy val runLocalSettings = Seq(
  Compile / run := Defaults
    .runTask(
      Compile / fullClasspath,
      Compile / run / mainClass,
      Compile / run / runner
    )
    .evaluated,
  Compile / runMain := Defaults
    .runTask(
      Compile / fullClasspath,
      Compile / run / mainClass,
      Compile / run / runner
    )
    .evaluated
)




