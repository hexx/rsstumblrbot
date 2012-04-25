organization := "com.github.hexx"

name := "rsstumblrbot"

version := "0.0.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.1",
  "net.databinder" %% "dispatch-gae" % "0.8.8",
  "rome" % "rome" % "1.0",
  "xerces" % "xercesImpl" % "2.9.1",
  "com.github.hexx" %% "dispatch-tumblr" % "0.0.1",
  "com.github.hexx" %% "gaeds" % "0.0.1",
  "javax.servlet" % "servlet-api" % "2.3" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

seq(appengineSettings: _*)
