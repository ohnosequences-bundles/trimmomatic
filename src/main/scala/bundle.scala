package ohnosequencesBundles.statika

import ohnosequences.statika._
import java.io.File
import TrimmomaticDSL._

/* Bundle */
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

  def withArgs(args: Seq[String]): Seq[String] = Seq("java", "-jar", trimmomatic.jar.getCanonicalPath) ++ args

  // def apply(
  //   threads: Option[Int]   = None,
  //   phred:   Option[Phred] = None,
  //   trimlog: Option[File]  = None
  // ): TrimmomaticCommand =
  //    TrimmomaticCommand(trimmomatic)(threads, phred, trimlog)
}
