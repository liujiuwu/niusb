import sbt._
import sbt.Keys._

import com.github.siasia.WebPlugin.{ container, webSettings }
import com.github.siasia.PluginKeys._
import sbtbuildinfo.Plugin._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseExecutionEnvironment

object BuildSettings {

  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")

  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")

  // call grunt init - requires npm be installed
  val gruntInit = TaskKey[Int]("grunt-init", "Initialize project for grunt")
  def gruntInitTask = (baseDirectory in Compile) map { dir =>
    Process(Seq("npm", "install"), dir) !
  }

  // call grunt compile
  val gruntCompile = TaskKey[Int]("grunt-compile", "Call the grunt compile command")
  def gruntCompileTask = (baseDirectory in Compile) map { dir =>
    Process(Seq("grunt", "compile"), dir) !
  }

  // call grunt compress
  val gruntCompress = TaskKey[Int]("grunt-compress", "Call the grunt compress command")
  def gruntCompressTask = (baseDirectory in Compile) map { dir =>
    Process(Seq("grunt", "compress"), dir) !
  }

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "niusb",
    organization := "com.niusb",
    version := "0.1",
    liftVersion <<= liftVersion ?? "2.5",
    liftEdition <<= liftVersion apply { _.substring(0, 3) },
    name <<= (name, liftEdition) { (n, e) => n + "_" + e },
    scalaVersion in ThisBuild := "2.10.0",
    //crossScalaVersions := Seq("2.10.0"),
    scalacOptions <<= scalaVersion map { sv: String =>
      if (sv.startsWith("2.10.")) {
        Seq("-encoding", "UTF-8","-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")
      } else {
        Seq("-deprecation", "-unchecked")
      }
    },
    resolvers ++= Seq("aliyun nexus" at "http://42.120.5.18:9081/nexus/content/groups/public/"))


  val niusbWebSettings =
    basicSettings ++
      webSettings ++
      buildInfoSettings ++
      noPublishing ++
      seq(
        name := "niusb-web",

        // build-info
        buildInfoPackage := "code",
        sourceGenerators in Compile <+= buildInfo,

        // grunt tasks
        gruntInit <<= gruntInitTask,
        gruntCompile <<= gruntCompileTask,
        gruntCompress <<= gruntCompressTask,
	javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),

	EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,

        EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16),

        // dependencies
        compile <<= (compile in Compile) dependsOn gruntCompile,
        // (start in container.Configuration) <<= (start in container.Configuration) dependsOn gruntCompile,
        Keys.`package` <<= (Keys.`package` in Compile) dependsOn gruntCompress,

        // add directory where grunt publishes to, to the webapp
        (webappResources in Compile) <+= (baseDirectory) { _ / "grunt-build" / "out" },
        // add assets.json to classpath
        (unmanagedResourceDirectories in Compile) <+= (baseDirectory) { _ / "grunt-build" / "hash" },
        (unmanagedResourceDirectories in Compile) <+= (baseDirectory) { _ / "src/main/webapp" })

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ())
}

