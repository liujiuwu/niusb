//resolvers += Classpaths.typesafeResolver

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.12.0-0.2.11.1"))

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.3.0")

//Uncoment this line to enable the sbt idea plugin
//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")
//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

//addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5")

//Uncoment this line to enable the sbt eclipse plugin
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

//addSbtPlugin("me.lessis" % "less-sbt" % "0.1.10")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
