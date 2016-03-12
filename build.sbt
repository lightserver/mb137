import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._

import sbt.Keys._

name := "mb137"

version := "0.1"

scalaJSStage in Global := FastOptStage

skip in packageJSDependencies := false

val app = crossProject.settings(
  scalaVersion := "2.11.7",

  unmanagedSourceDirectories in Compile +=
    baseDirectory.value  / "shared" / "main" / "scala",

  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.3.8",
    "pl.setblack.lsa" %%% "cataracta" % "0.96"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework")


).jsSettings(
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.4"
    ),

  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "0.14.3" / "react-with-addons.js" commonJSName "React",
    "org.webjars.bower" % "react" % "0.14.3" / "react-dom.js" commonJSName "ReactDOM"),
    skip in packageJSDependencies := false ,// creates app-jsdeps.js with the react JS lib inside
     persistLauncher in Compile := true
  ).jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.1",
      "com.typesafe.akka" %% "akka-remote" % "2.4.1",
      "org.scalaz" %% "scalaz-core" % "7.1.2",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.0.1",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    )
  )

lazy val appJS = app.js.settings(


)

lazy val appJVM = app.jvm.settings(


  version := "0.31",

   resourceDirectory in Compile <<= baseDirectory(_ / "../shared/src/main/resources"),

  unmanagedResourceDirectories in Compile <+= baseDirectory(_ / "../jvm/src/main/resources"),

  resourceGenerators in Compile <+= Def.task {
    val log = streams.value.log
    //log.info(s"APP: ${((classDirectory in Compile).value / "material-ui-app.html").getCanonicalPath}")
    val mui = baseDirectory(_ / "../../web/.tmp" ).value
    val muiFiles = (mui ** ("*.js" || "*.css" || "*.eot" || "*.svg" || "*.svg" || "*.ttf" || "*.woff" || "*.html")).filter(_.isFile).get
    import Path.rebase
    val mappings = muiFiles pair rebase(Seq(mui), (resourceManaged in Compile).value / "web")
    IO.copy(mappings, true)
    mappings.map(_._2)
  }


).enablePlugins(JavaAppPackaging)