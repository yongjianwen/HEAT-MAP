package yong.jianwen.heatmap

import yong.jianwen.heatmap.data.entity.TripWithTracks

class GPXGenerator {
    companion object {
        fun generate(tripWithTracks: TripWithTracks): String {
            var gpxString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.0">
                	<name>Trip ID: ${tripWithTracks.tracks[0].track.id}</name>
            """.trimIndent()
            for (track in tripWithTracks.tracks) {
                gpxString += """
                    <trk>
                        <name>${track.track.name}</name>
                        <number>${track.track.number}</number>
                """.trimIndent()
                for (trackSegment in track.trackSegments) {
                    gpxString += """
                        <trkseg>
                    """.trimIndent()
                    for (trackPoint in trackSegment.trackPoints) {
                        gpxString += """
                            <trkpt lat="${trackPoint.latitude}" lon="${trackPoint.longitude}">
                                <ele>${trackPoint.elevation}</ele>
                                <time>2007-10-14T10:09:57Z</time>
                            </trkpt>
                        """.trimIndent()
                    }
                    gpxString += """
                        </trkseg>
                    """.trimIndent()
                }
                gpxString += """
                    </trk>
                """.trimIndent()
            }
            gpxString += "</gpx>"
            return gpxString
        }
    }
}
