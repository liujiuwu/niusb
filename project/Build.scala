import sbt._
import sbt.Keys._

object LiftModuleBuild extends Build {

  import BuildSettings._

  lazy val root = Project("root", file("."))
    .aggregate(niusbWeb)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  lazy val niusbWeb = Project("niusb-web", file("niusb-web"))
    .settings(niusbWebSettings: _*)
    .settings(libraryDependencies <++= (liftVersion) { liftVersion =>
      Seq(
        "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources (),
        "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources (),
        "net.liftmodules" %% "extras_2.5" % "0.2-SNAPSHOT" % "compile",
        "org.seleniumhq.selenium" % "selenium-java" % "2.33.0",
        "mysql" % "mysql-connector-java" % "5.1.21" % "runtime->default",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container,test",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided->default",
        "ch.qos.logback" % "logback-classic" % "1.0.6",
        "net.coobird" % "thumbnailator" % "0.4.5",
        "com.sksamuel.scrimage" % "scrimage-core" % "1.3.1",
        "com.sksamuel.scrimage" % "scrimage-filters" % "1.3.1",
        "junit" % "junit" % "4.10" % "test->default")
    })
}
