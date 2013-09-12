name := "niusb"

version := "1.0"

organization := "com.niusb"

scalaVersion := "2.10.2"

scanDirectories in Compile := Nil

resolvers ++= Seq("aliyun nexus" at "http://42.120.5.18:9081/nexus/content/groups/public/")

net.virtualvoid.sbt.graph.Plugin.graphSettings

seq(webSettings :_*)

port in container.Configuration := 9001

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-encoding", "UTF-8","-feature","-unchecked","-deprecation","-target:jvm-1.7","-language:postfixOps","-language:implicitConversions","-Xlog-reflective-calls","-Ywarn-adapted-args")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.7", "-target", "1.7")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

EclipseKeys.withSource := true

//ideaExcludeFolders += ".idea"

//ideaExcludeFolders += ".idea_modules"

libraryDependencies ++= {
  val liftVersion = "2.5.1"
  val scrimageVersion = "1.3.5"
  val jettyVersion = "9.0.5.v20130815"
  val seleniumVersion = "2.35.0"
  val extrasVersion = "0.2-SNAPSHOT"
  val logbackVersion = "1.0.13"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
    "net.liftmodules" %% "extras_2.5" % extrasVersion % "compile",
    "com.googlecode.xmemcached" % "xmemcached" % "1.4.2" % "compile",
    "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion excludeAll(ExclusionRule(organization = "org.slf4j")),
    "mysql" % "mysql-connector-java" % "5.1.26" % "runtime",
    "com.typesafe.slick" % "slick_2.10" % "1.0.1"  % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container,compile",
    "org.eclipse.jetty.orbit" %  "javax.servlet"     % "3.0.0.v201112011016"   %  "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "net.coobird" % "thumbnailator" % "0.4.5"  % "compile",
    "com.sksamuel.scrimage" % "scrimage-core_2.10" % scrimageVersion  % "compile",
    "com.sksamuel.scrimage" % "scrimage-filters_2.10" % scrimageVersion  % "compile",
    "junit" % "junit" % "4.10" % "test"
  )
}