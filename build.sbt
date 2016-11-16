name          := "trimmomatic"
organization  := "ohnosequences-bundles"
description   := "trimmomatic project"

bucketSuffix  := "era7.com"

libraryDependencies ++= Seq(
  "ohnosequences" %% "statika" % "2.0.0-M5"
)

wartremoverErrors in (Compile, compile) := Seq()

releaseOnlyTestTag := "ohnosequencesBundles.test.ReleaseOnlyTest"