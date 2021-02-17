package scala.build

import sbt._, Keys._
import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.plugin.MimaPlugin, MimaPlugin.autoImport._

object MimaFilters extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val mimaReferenceVersion = settingKey[Option[String]]("Scala version number to run MiMa against")
  }
  import autoImport._

  override val globalSettings = Seq(
    mimaReferenceVersion := Some("2.12.13"),
  )

  val mimaFilters: Seq[ProblemFilter] = Seq[ProblemFilter](
    // KEEP: scala.reflect.internal isn't public API
    ProblemFilters.exclude[Problem]("scala.reflect.internal.*"),

    // #9314 introduced private[this] object
    ProblemFilters.exclude[MissingClassProblem]("scala.collection.immutable.TreeSet$unitsIterator$"),

    // #9314 #9315 #9507 NewRedBlackTree is private[collection]
    ProblemFilters.exclude[Problem]("scala.collection.immutable.NewRedBlackTree*"),

    // Wrappers is private[collection]
    ProblemFilters.exclude[DirectMissingMethodProblem]("scala.collection.convert.Wrappers#IteratorWrapper.asIterator")
  )

  override val buildSettings = Seq(
    mimaFailOnNoPrevious := false, // we opt everything out, knowing we only check library/reflect
  )

  val mimaSettings: Seq[Setting[_]] = Def.settings(
    mimaPreviousArtifacts       := mimaReferenceVersion.value.map(organization.value % name.value % _).toSet,
    mimaCheckDirection          := "both",
    mimaBinaryIssueFilters     ++= mimaFilters,
//  mimaReportSignatureProblems := true, // TODO: enable
  )
}
