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
    "com.lihaoyi" %%% "scalatags" % "0.5.1",
    "com.lihaoyi" %%% "upickle" % "0.3.7",
    "pl.setblack.lsa" %%% "cataracta" % "0.95"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework")


).jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.4",
      "com.lihaoyi" %%% "scalarx" % "0.2.8"
    ),
    // React itself (react-with-addons.js can be react.js, react.min.js, react-with-addons.min.js)
  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "0.14.3" / "react-with-addons.js" commonJSName "React",
    "org.webjars.bower" % "react" % "0.14.3" / "react-dom.js" commonJSName "ReactDOM"),
    skip in packageJSDependencies := false ,// creates app-jsdeps.js with the react JS lib inside
     persistLauncher in Compile := true
  ).jvmSettings(
    libraryDependencies ++= Seq(
      "io.spray" %% "spray-can" % "1.3.2",
      "io.spray" %% "spray-routing" % "1.3.2",
      "com.typesafe.akka" %% "akka-actor" % "2.3.6",
      "com.typesafe.akka" %% "akka-remote" % "2.3.6",
      "org.scalaz" %% "scalaz-core" % "7.1.2",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.0.1",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    )
  )

lazy val appJS = app.js.settings(


)

lazy val appJVM = app.jvm.settings(


  version := "0.31",

  // JS files like app-fastopt.js and app-jsdeps.js need to be copied to the server
 /* (resources in Compile) += (fastOptJS in (appJS, Compile)).value.map({ outDir: File =>
     outDir
  }) .data,
  (resources in Compile) += (fastOptJS in (appJS, Compile)).value.map({ outDir: File =>
    println("mapuje : " + outDir.toString)
    val mapping =  new File( outDir.getAbsolutePath + ".map")
    mapping
  }) .data,*/
  /*(resources in Compile) += (packageJSDependencies in (appJS, Compile)).value,
  (resources in Compile) += (packageScalaJSLauncher in (appJS, Compile)).value.data,*/

  // copy resources like quiz.css to the server
  resourceDirectory in Compile <<= baseDirectory(_ / "../shared/src/main/resources"),

  // allow the server to access shared source
 // that was a problem: unmanagedSourceDirectories in Compile <+= baseDirectory(_ / "../shared/src/main/scala"),

  // application.conf too must be in the classpath
  // application.conf too must be in the classpath
  unmanagedResourceDirectories in Compile <+= baseDirectory(_ / "../jvm/src/main/resources"),
  /*(unmanagedResourceDirectories in Compile) += baseDirectory(_ / "../../web/.tmp" ).value.map(
  { outDir: File =>
    println("mapujex : " + outDir.toString)
    val mapping =  new File( outDir.getAbsolutePath + ".map")
    mapping
  }
  ),*/
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