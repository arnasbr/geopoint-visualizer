import Models._
import play.api.libs.json.{JsValue, Json}
import io.circe.parser._
import scala.sys.process._
import java.awt.Desktop
import java.net.URI

object Utils {
  def parseInputCoordinates(
      input: String,
      swap: Boolean
  ): Option[CoordinatesList] = {
    val json = parse(input)

    json
      .flatMap(_.as[CoordinatesList].map { coordinatesList =>
        if (swap) {
          val swappedCoordinates =
            coordinatesList.points.map(point => Point(point.lng, point.lat))
          CoordinatesList(swappedCoordinates)
        } else {
          coordinatesList
        }
      })
      .toOption
  }

  def createFeatureCollection(
      inputCoordinatesOneOpt: Option[CoordinatesList],
      colorOne: Color,
      inputCoordinatesTwoOpt: Option[CoordinatesList],
      colorTwo: Color,
      inputCoordinatesThreeOpt: Option[CoordinatesList],
      colorThree: Color,
      inputCoordinatesFourOpt: Option[CoordinatesList],
      colorFour: Color,
      inputCoordinatesFiveOpt: Option[CoordinatesList],
      colorFive: Color
  ): JsValue = {
    val features = Seq(
      createFeatures(inputCoordinatesOneOpt, colorOne),
      createFeatures(inputCoordinatesTwoOpt, colorTwo),
      createFeatures(inputCoordinatesThreeOpt, colorThree),
      createFeatures(inputCoordinatesFourOpt, colorFour),
      createFeatures(inputCoordinatesFiveOpt, colorFive)
    ).flatten

    Json.obj(
      "type" -> "FeatureCollection",
      "features" -> features
    )
  }

  private def createFeatures(
      inputCoordinatesOpt: Option[CoordinatesList],
      color: Color
  ): List[JsValue] = {
    for {
      inputCoordinates <- inputCoordinatesOpt.toList
      coordinates <- inputCoordinates.points
    } yield {
      Json.obj(
        "type" -> "Feature",
        "properties" -> Json.obj(
          "marker-color" -> color.hexValue,
          "marker-size" -> "large",
          "marker-symbol" -> "circle"
        ),
        "geometry" -> Json.obj(
          "type" -> "Point",
          "coordinates" -> List(coordinates.lat, coordinates.lng)
        )
      )
    }
  }

  def openInBrowser(url: String): Unit = {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(url))
    }
  }

  def executeImageGeneration(
      downloadFlag: Boolean,
      browserFlag: Boolean,
      downloadCmd: String,
      openInBrowser: Unit
  ): Any = {
    (downloadFlag, browserFlag) match {
      case (false, false) =>
        downloadCmd.! //If neither flag is picked, the default is to download the image
      case (true, false) => downloadCmd.!
      case (false, true) => openInBrowser
      case (true, true) =>
        downloadCmd.!
        openInBrowser
    }
  }
}
