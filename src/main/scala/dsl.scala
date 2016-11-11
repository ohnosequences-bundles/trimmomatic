package ohnosequencesBundles.statika

import java.io.File

/* Mini DSL for Trimmomatic */
case object TrimmomaticDSL {

  /* Base quality encoding */
  sealed trait Phred
  case object phred33 extends Phred
  case object phred64 extends Phred
  /* If no quality encoding is specified, it will be determined automatically (since version 0.32) */

  implicit def phredToOption(p: Phred): Some[Phred] = Some(p)

  def threads(t: Int): Some[Int] = Some(t)
  def trimlog(f: File): Some[File] = Some(f)

  sealed class Step(opts: String*) { step: Product =>
    override def toString = (step.productPrefix +: opts).mkString(":")
  }

  case class SLIDINGWINDOW(
    windowSize: Int,
    requiredQuality: Int
  ) extends Step(windowSize.toString, requiredQuality.toString)

  case class  LEADING(quality: Int) extends Step(quality.toString)
  case class TRAILING(quality: Int) extends Step(quality.toString)

  case class     CROP(length: Int) extends Step(length.toString)
  case class HEADCROP(length: Int) extends Step(length.toString)

  case class MINLEN(length: Int) extends Step(length.toString)

  case object TOPHRED33 extends Step()
  case object TOPHRED64 extends Step()

  // TODO: ILLUMINACLIP:<fastaWithAdaptersEtc>:<seed mismatches>:<palindrome clip threshold>:<simple clip threshold>:<minAdapterLength>:<keepBothReads>
  // TODO: MAXINFO:<targetLength>:<strictness>
}
import TrimmomaticDSL._


/* Command constructor */
case class TrimmomaticCommand(
  trimmomatic: Trimmomatic
)(threads: Option[Int],
  phred:   Option[Phred],
  trimlog: Option[File]
) {

  def basicOpts: Seq[String] = Seq(
    threads.map { t => Seq("-threads", t.toString) },
      phred.map { p => Seq(s"-${p}") } ,
    trimlog.map { f => Seq("-trimlog", f.getCanonicalPath.toString) }
  ).flatten.flatten

  private def paths(files: File*) = files.map { _.getCanonicalPath }

  def singleEnd(
    input: File,
    output: File
  )(steps: Step*): Seq[String] =
    trimmomatic.withArgs(
      Seq("SE") ++
      basicOpts ++
      paths(input, output) ++
      steps.map(_.toString)
    )

  def pairedEnd(
    input1: File, input2: File
  )(pairedOutput1: File, unpairedOutput1: File
  )(pairedOutput2: File, unpairedOutput2: File
  )(steps: Step*): Seq[String] =
    trimmomatic.withArgs(
      Seq("PE") ++
      basicOpts ++
      paths(
        input1, input2,
        pairedOutput1, unpairedOutput1,
        pairedOutput2, unpairedOutput2
      ) ++
      steps.map(_.toString)
    )
}
