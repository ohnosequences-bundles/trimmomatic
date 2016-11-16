/* # Trimmomatic DSL */
package ohnosequencesBundles.statika

import java.io.File

/* ## DSL for Trimmomatic options

Note, that the comments here are copied from the [Trimmomatic Manual v0.32](http://www.usadellab.org/cms/uploads/supplementary/Trimmomatic/TrimmomaticManual_V0.32.pdf). For More information refer to it and to the [Trimmomatic website](http://www.usadellab.org/cms/?page=trimmomatic).
*/
case object TrimmomaticDSL {

  /* Options that are accepted by any mode (single/paired end) */
  sealed trait BasicOption { val parts: Seq[String] }

  /* Base quality encoding */
  sealed trait Phred extends BasicOption { lazy val parts = Seq(s"-${this}") }
  case object phred33 extends Phred
  case object phred64 extends Phred
  /* If no quality encoding is specified, it will be determined automatically (since version 0.32) */

  case class threads(t: Int)  extends BasicOption { lazy val parts = Seq("-threads", t.toString) }
  case class trimlog(f: File) extends BasicOption { lazy val parts = Seq("-trimlog", f.getCanonicalPath.toString) }


  /* ### Trimming Steps

  The different processing steps occur in the order in which the steps are specified on the command line. It is recommended in most cases that adapter clipping, if required, is done as early as possible, since correctly identifying adapters using partial matches is more difficult.
  */
  sealed class TrimmingStep(args: String*) { step: Product =>
    override def toString = (step.productPrefix +: args).mkString(":")
  }


  /* #### ILLUMINACLIP

  This step is used to find and remove Illumina adapters.
  */
  case class ILLUMINACLIP(
    /* - Specifies the path to a fasta file containing all the adapters, PCR sequences etc */
    fastaWithAdaptersEtc: File,
    /* - Specifies the maximum mismatch count which will still allow a full match to be performed */
    seedMismatches: Int,
    /* - Specifies how accurate the match between the two 'adapter ligated' reads must be for PE palindrome read alignment */
    palindromeClipThreshold: Int,
    /* - Specifies how accurate the match between any adapter etc. sequence must be against a read */
    simpleClipThreshold: Int,

    /* Two optional parameters for palindrome mode only: */

    /* - In addition to the alignment score, palindrome mode can verify that a minimum length of adapter has been detected. If unspecified, this defaults to 8 bases, for historical reasons. However, since palindrome mode has a very low false positive rate, this can be safely reduced, even down to 1, to allow shorter adapter fragments to be removed */
    minAdapterLength: Int = 8,
    /* - After read-though has been detected by palindrome mode, and the adapter sequence removed, the reverse read contains the same sequence information as the forward read, albeit in reverse complement. For this reason, the default behaviour is to entirely drop the reverse read. By specifying „true‟ for this parameter, the reverse read will also be retained, which may be useful e.g. if the downstream tools cannot handle a combination of paired and unpaired reads */
    keepBothReads: Boolean = false
  ) extends TrimmingStep(
    fastaWithAdaptersEtc.getCanonicalPath.toString,
    seedMismatches.toString,
    palindromeClipThreshold.toString,
    simpleClipThreshold.toString,
    minAdapterLength.toString,
    keepBothReads.toString
  )


  /* #### SLIDINGWINDOW

  Perform a sliding window trimming, cutting once the average quality within the window falls below a threshold. By considering multiple bases, a single poor quality base will not cause the removal of high quality data later in the read.
  */
  case class SLIDINGWINDOW(
    /* - Specifies the number of bases to average across */
    windowSize: Int,
    /* - Specifies the average quality required */
    requiredQuality: Int
  ) extends TrimmingStep(
    windowSize.toString,
    requiredQuality.toString
  )


  /* #### MAXINFO

  Performs an adaptive quality trim, balancing the benefits of retaining longer reads against the costs of retaining bases with errors.
  */
  case class MAXINFO(
    /* - Specifies the read length which is likely to allow the location of the read within the target sequence to be determined */
    targetLength: Int,
    /* - This value, which should be set between 0 and 1, specifies the balance between preserving as much read length as possible vs. removal of incorrect bases. A low value of this parameter (<0.2) favours longer reads, while a high value (>0.8) favours read correctness */
    strictness: Float
  ) extends TrimmingStep(
    targetLength.toString,
    strictness.toString
  )


  /* #### LEADING

  Remove low quality bases from the beginning. As long as a base has a value below this threshold the base is removed and the next base will be investigated.
  */
  case class LEADING(
    /* - Specifies the minimum quality required to keep a base */
    quality: Int
  ) extends TrimmingStep(quality.toString)


  /* #### TRAILING

  Remove low quality bases from the end. As long as a base has a value below this threshold the base is removed and the next base (which as trimmomatic is starting from the 3‟ prime end would be base preceding the just removed base) will be investigated.
  */
  case class TRAILING(
    /* - Specifies the minimum quality required to keep a base */
    quality: Int
  ) extends TrimmingStep(quality.toString)


  /* #### CROP

  Removes bases regardless of quality from the end of the read, so that the read has maximally the specified length after this step has been performed. Steps performed after CROP might of course further shorten the read.
  */
  case class CROP(
    /* - The number of bases to keep, from the end of the read */
    length: Int
  ) extends TrimmingStep(length.toString)


  /* #### HEADCROP

  Removes the specified number of bases, regardless of quality, from the beginning of the read.
  */
  case class HEADCROP(
    /* - The number of bases to remove from the start of the read */
    length: Int
  ) extends TrimmingStep(length.toString)

  /* #### MINLEN

  This module removes reads that fall below the specified minimal length. If required, it should normally be after all other processing steps.
  */
  case class MINLEN(
    /* - Specifies the minimum length of reads to be kept */
    length: Int
  ) extends TrimmingStep(length.toString)


  /* #### TOPHRED33

  This (re)encodes the quality part of the FASTQ file to base 33.
  */
  case object TOPHRED33 extends TrimmingStep()


  /* #### TOPHRED64

  This (re)encodes the quality part of the FASTQ file to base 64.
  */
  case object TOPHRED64 extends TrimmingStep()

}

/* ## DSL for Trimmomatic command

This trait provides methods for constructing complete Trimmomatic commands (represented as `Seq[String]`) ready to be ran.
*/
trait TrimmomaticCommand {
  import TrimmomaticDSL._

  val jar: File

  def withArgs(args: Seq[String]): Seq[String] = Seq("java", "-jar", jar.getCanonicalPath) ++ args

  private def paths(files: File*) = files.map { _.getCanonicalPath.toString }

  /* For single-ended data, one input and one output file are specified. The required processing steps (trimming, cropping, adapter clipping etc.) are specified as additional arguments after the input/output files. */
  def singleEnd(basicOpts: BasicOption*)(steps: TrimmingStep*)(
    input: File,
    output: File
  ): Seq[String] = this.withArgs(
    Seq("SE") ++
    basicOpts.flatMap(_.parts) ++
    paths(input, output) ++
    steps.map(_.toString)
  )

  /* For paired-end data, two input files, and 4 output files are specified, 2 for the 'paired' output where both reads survived the processing, and 2 for corresponding 'unpaired' output where a read survived, but the partner read did not. */
  def pairedEnd(basicOpts: BasicOption*)(steps: TrimmingStep*)(
    input1: File, input2: File,
    pairedOutput1: File, unpairedOutput1: File,
    pairedOutput2: File, unpairedOutput2: File
  ): Seq[String] = this.withArgs(
    Seq("PE") ++
    basicOpts.flatMap(_.parts) ++
    paths(
      input1, input2,
      pairedOutput1, unpairedOutput1,
      pairedOutput2, unpairedOutput2
    ) ++
    steps.map(_.toString)
  )

  // TODO: basenames for input/output and a way to get full names
}
