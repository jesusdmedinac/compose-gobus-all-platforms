import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactoryInfo

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Gobus") {
        App(
            maps = { modifier, currentLocationState, mapState ->
                SwingPanel(
                    factory = {
                        val mapViewer = JXMapViewer()

                        // Create a TileFactoryInfo for OpenStreetMap

                        // Create a TileFactoryInfo for OpenStreetMap
                        val info: TileFactoryInfo = OSMTileFactoryInfo()
                        val tileFactory = DefaultTileFactory(info)
                        mapViewer.tileFactory = tileFactory

                        // Use 8 threads in parallel to load the tiles

                        // Use 8 threads in parallel to load the tiles
                        tileFactory.setThreadPoolSize(8)

                        // Set the focus

                        // Set the focus
                        val frankfurt = GeoPosition(50.11, 8.68)

                        mapViewer.zoom = 7
                        mapViewer.addressLocation = frankfurt

                        mapViewer
                    },
                )
            },
        )
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App(
        maps = { modifier, currentLocationState, mapState -> },
        onHomeDisplayed = {},
    )
}
