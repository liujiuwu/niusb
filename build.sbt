name := "niusb"

version := "1.0"

organization := "com.niusb"

scalaVersion := "2.10.0"

scanDirectories in Compile := Nil

resolvers ++= Seq("aliyun nexus" at "http://42.120.5.18:9081/nexus/content/groups/public/")

seq(com.github.siasia.WebPlugin.webSettings :_*)

port in container.Configuration := 9001

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-encoding", "utf8","-feature","-unchecked","-deprecation","-target:jvm-1.6","-language:postfixOps","-language:implicitConversions","-Xlog-reflective-calls","-Ywarn-adapted-args")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16)

EclipseKeys.withSource := true

libraryDependencies ++= {
  val liftVersion = "2.5"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources(),
    "net.liftmodules" %% "extras_2.5" % "0.2-SNAPSHOT" % "compile",
    //"org.pegdown"             %  "pegdown"           % "1.4.1",
    "com.tristanhunt" %% "knockoff" % "0.8.1",
    "com.googlecode.xmemcached" % "xmemcached" % "1.4.2",
    "org.seleniumhq.selenium" % "selenium-java" % "2.33.0",
    "mysql" % "mysql-connector-java" % "5.1.21" % "runtime->default",
    "org.seleniumhq.selenium" % "selenium-java" % "2.33.0",
    "mysql" % "mysql-connector-java" % "5.1.21" % "runtime->default",
    //"com.typesafe.slick" % "slick_2.10" % "2.0.0-M2",
    "com.typesafe.slick" % "slick_2.10" % "1.0.1",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided->default",
    "ch.qos.logback" % "logback-classic" % "1.0.6",
    "net.coobird" % "thumbnailator" % "0.4.5",
    "com.sksamuel.scrimage" % "scrimage-core" % "1.3.1",
    "com.sksamuel.scrimage" % "scrimage-filters" % "1.3.1",
    "junit" % "junit" % "4.10" % "test->default"
  )
}