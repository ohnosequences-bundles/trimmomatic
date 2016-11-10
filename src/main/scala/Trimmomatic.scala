package ohnosequencesBundles.statika

import ohnosequences.statika._
import java.io.File

abstract class Trimmomatic(val version: String) extends Bundle() { trimmomatic =>

  lazy val baseName = s"Trimmomatic-${version}"
  lazy val trimmomaticZip = s"${baseName}.zip"
  lazy val jar = new File(trimmomatic.baseName, s"trimmomatic-${version}.jar")

  lazy val downloadZip = cmd("wget")(
    s"http://s3-eu-west-1.amazonaws.com/resources.ohnosequences.com/trimmomatic/${version}/${trimmomaticZip}"
  )

  def instructions: AnyInstructions =
    downloadZip -&-
    cmd("unzip")(trimmomaticZip)

  def run(args: String*): CmdInstructions = cmd("java")("-jar", trimmomatic.jar.getCanonicalPath, args: _*)
}
