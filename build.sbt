name := "kf-etl-indexer"

version := "0.1"

scalaVersion := "2.12.11"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases"

val spark_version = "3.1.1"
val elasticsearch_spark_version = "7.12.0"
val scalatest_version = "3.2.0"

/* Runtime */
libraryDependencies += "org.apache.spark" %% "spark-sql" % spark_version % Provided
libraryDependencies += "org.elasticsearch" %% "elasticsearch-spark-30" % elasticsearch_spark_version % Provided
libraryDependencies += "bio.ferlab" %% "datalake-spark3" % "0.0.50"

/* Test */
libraryDependencies += "org.scalatest" %% "scalatest" % scalatest_version % Test
libraryDependencies += "org.apache.spark" %% "spark-hive" % spark_version % Test

assembly / test := {}

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case "META-INF/native/libnetty_transport_native_epoll_x86_64.so" => MergeStrategy.last
  case "META-INF/DISCLAIMER" => MergeStrategy.last
  case "mozilla/public-suffix-list.txt" => MergeStrategy.last
  case "overview.html" => MergeStrategy.last
  case "git.properties" => MergeStrategy.discard
  case "mime.types" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
assembly / assemblyJarName := s"kf-etl-indexer-spark3-$elasticsearch_spark_version.jar"
