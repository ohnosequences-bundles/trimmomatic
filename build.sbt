name          := "trimmomatic"
organization  := "ohnosequences-bundles"
description   := "trimmomatic project"

bucketSuffix  := "era7.com"

libraryDependencies += "ohnosequences" %% "statika" % "2.0.0"

wartremoverErrors in (Compile, compile) := Seq()

releaseOnlyTestTag := "ohnosequencesBundles.test.ReleaseOnlyTest"
