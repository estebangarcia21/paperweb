package io.estebangarcia21.paper.web.sbt

import java.io.InputStream
import java.util.Properties

private[sbt] object PaperwebVersion {

  lazy val current: String = {
    val properties = new Properties()
    val stream: InputStream = Option(getClass.getResourceAsStream("/paperweb-version.properties"))
      .getOrElse(sys.error("Paperweb version resource is missing"))

    try properties.load(stream)
    finally stream.close()

    Option(properties.getProperty("version"))
      .filter(_.nonEmpty)
      .getOrElse(sys.error("Paperweb version resource contains no version"))
  }

}
