import sbt._

class Twitter2Tumblr(info: ProjectInfo) extends DefaultProject(info) {
  val scalatoolsRelease = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-releases/"

  val httpClientVersion = "4.1"

  override def libraryDependencies = Set(
    "org.apache.httpcomponents" % "httpclient" % httpClientVersion % "compile->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  ) ++ super.libraryDependencies
}