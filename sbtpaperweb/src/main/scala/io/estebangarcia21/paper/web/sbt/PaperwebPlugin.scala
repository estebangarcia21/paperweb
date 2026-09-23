package io.estebangarcia21.paper.web.sbt

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import spray.revolver.RevolverPlugin
import spray.revolver.RevolverCorePlugin.autoImport._
import spray.revolver.RevolverPlugin.autoImport._

import java.nio.file.Paths

/** Adds the matching Paperweb runtime and testkit and registers the `paperweb` command. */
object PaperwebPlugin extends AutoPlugin {

  object autoImport {

    val paperwebConfigFile =
      settingKey[File]("Project-local Paperweb configuration file.")

    val paperwebAddLibrary =
      settingKey[Boolean]("Whether Paperweb adds its matching runtime and testkit dependencies.")

    val paperwebHotCompile =
      taskKey[Unit]("Compile sources and publish a browser reload version when classes change.")

  }

  import autoImport._

  override def requires: Plugins = JvmPlugin && RevolverPlugin

  override def trigger: PluginTrigger = noTrigger

  override def globalSettings: Seq[Def.Setting[_]] =
    Seq(commands += paperwebCommand)

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      paperwebConfigFile := baseDirectory.value / "paperweb.conf",
      paperwebAddLibrary := true,
      libraryDependencies ++= {
        if (paperwebAddLibrary.value)
          Seq(
            "io.estebangarcia21.paper" %% "paperweb" % PaperwebVersion.current,
            "io.estebangarcia21.paper" %% "paperwebtestkit" % PaperwebVersion.current % Test
          )
        else Nil
      },
      reStart / aggregate := false,
      reStart / fullClasspath ++= {
        // The agent reads this from the child classpath to watch sbt's freshly compiled classes.
        val configDirectory = target.value / "paperweb-hot-config"
        IO.write(configDirectory / "hotswap-agent.properties", "autoHotswap=true\n")
        IO.write(
          configDirectory / "reload.stamp",
          PaperwebTooling.classTreeVersion((Compile / classDirectory).value.toPath)
        )

        Seq(Attributed.blank(configDirectory))
      },
      paperwebHotCompile := {
        (Compile / compile).value

        val configDirectory = target.value / "paperweb-hot-config"
        val version = PaperwebTooling.classTreeVersion((Compile / classDirectory).value.toPath)

        IO.write(configDirectory / "reload.stamp", version)
      },
      reStart / envVars ++= {
        val loaded = loadConfig(baseDirectory.value, paperwebConfigFile.value)

        Map(
          "PAPERWEB_DEVELOPMENT" -> "true",
          "PAPERWEB_ASSET_DIRECTORY" -> loaded.assetsDirectory.getAbsolutePath
        )
      },
      reStart / javaOptions ++= {
        val loaded = loadConfig(baseDirectory.value, paperwebConfigFile.value)

        Seq(
          "-Dpaperweb.development=true",
          s"-Dpaperweb.assetDirectory=${loaded.assetsDirectory.getAbsolutePath}",
          s"-Dpaperweb.reloadStampFile=${(target.value / "paperweb-hot-config" / "reload.stamp").getAbsolutePath}"
        )
      }
    )

  private val paperwebCommand =
    Command.args("paperweb", "<command>") { (state, arguments) =>
      val extracted = Project.extract(state)
      val current = extracted.currentRef
      val base = extracted.get(current / baseDirectory)
      val configFile = extracted.get(current / paperwebConfigFile)

      PaperwebConfig.load(base, configFile) match {
        case Left(error) =>
          state.log.error(error)
          state.fail
        case Right(config) if arguments == Seq("dev") =>
          PaperwebTooling.stopDevelopmentPort(config.developmentPort) match {
            case Left(error) =>
              state.log.error(error)
              state.fail
            case Right(messages) =>
              messages.foreach(message => state.log.info(message))
              Command.process("~reStart", state, error => state.log.error(error))
          }
        case Right(config) if arguments == Seq("dev", "hot") =>
          PaperwebTooling.hotSwapJvmOptions(Paths.get(sys.props("java.home"))) match {
            case Left(error) =>
              state.log.error(error)
              state.fail
            case Right(options) =>
              PaperwebTooling.stopDevelopmentPort(config.developmentPort) match {
                case Left(error) =>
                  state.log.error(error)
                  state.fail
                case Right(messages) =>
                  messages.foreach(message => state.log.info(message))
                  val started = Command.process(
                    s"reStart --- ${options.mkString(" ")}",
                    state,
                    error => state.log.error(error)
                  )

                  Command.process("~paperwebHotCompile", started, error => started.log.error(error))
              }
          }
        case Right(config) =>
          PaperwebTooling.run(arguments, config) match {
            case Left(error) =>
              state.log.error(error)
              state.fail
            case Right(messages) =>
              messages.foreach(message => state.log.info(message))
              state
          }
      }
    }

  private def loadConfig(base: File, configFile: File): PaperwebConfig =
    PaperwebConfig.load(base, configFile).fold(sys.error, identity)

}
