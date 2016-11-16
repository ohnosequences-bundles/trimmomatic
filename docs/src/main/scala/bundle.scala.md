
```scala
package ohnosequencesBundles.statika

import ohnosequences.statika._
import java.io.File
```

Bundle

```scala
abstract class Trimmomatic(val version: String) extends Bundle() with TrimmomaticCommand { trimmomatic =>

  lazy val baseName = s"Trimmomatic-${version}"
  lazy val trimmomaticZip = s"${baseName}.zip"
  lazy val jar = new File(trimmomatic.baseName, s"trimmomatic-${version}.jar")

  lazy val downloadZip = cmd("wget")(
    s"http://s3-eu-west-1.amazonaws.com/resources.ohnosequences.com/trimmomatic/${version}/${trimmomaticZip}"
  )

  def instructions: AnyInstructions =
    downloadZip -&-
    cmd("unzip")(trimmomaticZip)
}

```




[main/scala/bundle.scala]: bundle.scala.md
[main/scala/dsl.scala]: dsl.scala.md