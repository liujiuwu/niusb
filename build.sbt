name := "niusb"

version := "1.0"

organization := "com.niusb"

scalaVersion := "2.10.0"

scanDirectories in Compile := Nil

resolvers ++= Seq(
	"aliyun nexus" at "http://42.120.5.18:9081/nexus/content/groups/public/"
) 

seq(com.github.siasia.WebPlugin.webSettings :_*)

port in container.Configuration := 9001

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-encoding", "UTF-8","-deprecation","-unchecked")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

//seq(lessSettings:_*)

//(sourceDirectory in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "less")

//(resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp" / "css")

//(LessKeys.mini in (Compile, LessKeys.less)) := true

//(LessKeys.filter in (Compile, LessKeys.less)) := "main.less"


EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16)

EclipseKeys.withSource := true

libraryDependencies ++= {
  val liftVersion = "2.5"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile->default"  withSources(),
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile->default"  withSources(),
    "net.liftmodules"   % "fobo_2.5_2.10"       % "1.0",
    "net.liftmodules"   %% "extras_2.5" % "0.1" % "compile",
    "org.seleniumhq.selenium" % "selenium-java" % "2.33.0",
    "mysql" 	        %  "mysql-connector-java"   % "5.1.21" % "runtime->default",
    "org.eclipse.jetty" % "jetty-webapp"        % "8.0.4.v20111024"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided->default",
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "net.coobird" % "thumbnailator" % "0.4.5",
    "com.sksamuel.scrimage" % "scrimage-core" % "1.3.1",
    "com.sksamuel.scrimage" % "scrimage-filters" % "1.3.1",
    "junit"             % "junit" % "4.10" % "test->default"
  )
}

