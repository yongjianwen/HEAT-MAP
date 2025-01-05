package yong.jianwen.heatmap.local

import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.TrackPoint
import yong.jianwen.heatmap.data.entity.TrackSegment
import yong.jianwen.heatmap.data.entity.TrackSegmentWithTrackPoints
import yong.jianwen.heatmap.data.entity.TrackWithTrackSegments
import yong.jianwen.heatmap.data.entity.Trip
import yong.jianwen.heatmap.data.entity.TripWithTracks
import yong.jianwen.heatmap.data.helper.CarAndModeForTrip

class DataSource {

    companion object {
        private val mockCars = listOf(
            Car(
                id = 1,
                registrationNumber = "MV 7097",
                manufacturer = "Honda",
                model = "Accord"
            ),
            Car(
                id = 2,
                registrationNumber = "MAS 1080",
                manufacturer = "Proton",
                model = "Wira"
            ),
            Car(
                id = 3,
                registrationNumber = "MBQ 1080",
                manufacturer = "Proton",
                model = "Saga"
            ),
            Car(
                id = 4,
                registrationNumber = "MAS 1080",
                manufacturer = "Toyota",
                model = "Hilux"
            ),
            Car(
                id = 5,
                registrationNumber = "MDV 108",
                manufacturer = "Tesla",
                model = "Model Y"
            ),
        )
        private val mockTrip = Trip(
            id = 1,
            name = "Bahau ABCDEFGH IJKLMNOPQ RSTUVWXYZ Trip",
            start = "2024-12-01T13:05:23",
            end = "2024-12-01T15:00:13"
        )
        private val mockTrack1 = Track(
            id = 1,
            tripId = 1,
            type = "Driving",
            name = "Morning driving track from Melaka to Genting Highlands via NS Highway",
            number = 1,
            start = "2024-12-01T08:05:23",
            end = "2024-12-01T08:35:51",
            carId = 1
        )
        private val mockTrack2 = Track(
            id = 2,
            tripId = 1,
            type = "Driving",
            name = "Morning seated track",
            number = 2,
            start = "2024-12-01T08:36:55",
            end = "2024-12-01T10:00:13",
            carId = 1
        )
        private val mockTrackSegment1 = TrackSegment(
            id = 1,
            trackId = 1,
            number = 1
        )
        private val mockTrackSegment2 = TrackSegment(
            id = 2,
            trackId = 1,
            number = 2
        )
        private val mockTrackSegment3 = TrackSegment(
            id = 3,
            trackId = 2,
            number = 1
        )
        private val mockTrackSegment4 = TrackSegment(
            id = 4,
            trackId = 2,
            number = 2
        )
        private val mockTrackPoint1 = TrackPoint(
            id = 1,
            trackSegmentId = 1,
            latitude = 102.0,
            longitude = 2.0,
            elevation = 10,
            time = "2024-12-01T08:05:23"
        )
        private val mockTrackPoint2 = TrackPoint(
            id = 2,
            trackSegmentId = 1,
            latitude = 102.1,
            longitude = 2.3,
            elevation = 11,
            time = "2024-12-01T08:15:23"
        )
        private val mockTrackPoint3 = TrackPoint(
            id = 3,
            trackSegmentId = 2,
            latitude = 102.15,
            longitude = 2.35,
            elevation = 10,
            time = "2024-12-01T08:25:23"
        )
        private val mockTrackPoint4 = TrackPoint(
            id = 4,
            trackSegmentId = 2,
            latitude = 102.35,
            longitude = 2.15,
            elevation = 15,
            time = "2024-12-01T08:35:51"
        )
        private val mockTrackPoint5 = TrackPoint(
            id = 5,
            trackSegmentId = 3,
            latitude = 102.35,
            longitude = 2.15,
            elevation = 15,
            time = "2024-12-01T08:36:55"
        )
        private val mockTrackPoint6 = TrackPoint(
            id = 6,
            trackSegmentId = 3,
            latitude = 102.45,
            longitude = 2.05,
            elevation = 15,
            time = "2024-12-01T08:59:55"
        )
        private val mockTrackPoint7 = TrackPoint(
            id = 7,
            trackSegmentId = 4,
            latitude = 102.46,
            longitude = 2.08,
            elevation = 15,
            time = "2024-12-01T09:35:55"
        )
        private val mockTrackPoint8 = TrackPoint(
            id = 8,
            trackSegmentId = 4,
            latitude = 102.51233235245466555,
            longitude = 2.090923409230794555,
            elevation = 15,
            time = "2024-12-01T10:00:13"
        )
        private val mockTrackSegmentWithTrackPoints1 = TrackSegmentWithTrackPoints(
            trackSegment = mockTrackSegment1,
            trackPoints = listOf(mockTrackPoint1, mockTrackPoint2)
        )
        private val mockTrackSegmentWithTrackPoints2 = TrackSegmentWithTrackPoints(
            trackSegment = mockTrackSegment2,
            trackPoints = listOf(mockTrackPoint3, mockTrackPoint4)
        )
        private val mockTrackSegmentWithTrackPoints3 = TrackSegmentWithTrackPoints(
            trackSegment = mockTrackSegment3,
            trackPoints = listOf(mockTrackPoint5, mockTrackPoint6)
        )
        private val mockTrackSegmentWithTrackPoints4 = TrackSegmentWithTrackPoints(
            trackSegment = mockTrackSegment4,
            trackPoints = listOf(mockTrackPoint7, mockTrackPoint8)
        )
        private val mockTrackWithTrackSegments1 = TrackWithTrackSegments(
            track = mockTrack1,
            trackSegments = listOf(
                mockTrackSegmentWithTrackPoints1,
                mockTrackSegmentWithTrackPoints2
            )
        )
        private val mockTrackWithTrackSegments2 = TrackWithTrackSegments(
            track = mockTrack2,
            trackSegments = listOf(
                mockTrackSegmentWithTrackPoints3,
                mockTrackSegmentWithTrackPoints4
            )
        )
        private val mockTripWithTracks = TripWithTracks(
            trip = mockTrip,
            tracks = listOf(mockTrackWithTrackSegments1, mockTrackWithTrackSegments2)
        )

        private val mockTrips = listOf(
            mockTrip,
            Trip(
                id = 2,
                name = "Genting Highlands Trip",
                start = "2024-12-02T08:05:23",
                end = "2024-12-03T10:00:13"
            ),
            Trip(
                id = 3,
                name = "Afternoon driving",
                start = "2024-12-05T08:05:23",
                end = "2024-12-05T10:00:13"
            ),
            Trip(
                id = 4,
                name = "Genting Highlands Trip",
                start = "2024-12-02T08:05:23",
                end = "2024-12-03T10:00:13"
            ),
            Trip(
                id = 5,
                name = "Afternoon driving",
                start = "2024-12-05T08:05:23",
                end = "2024-12-05T10:00:13"
            )
        )

        private val chips = listOf(
            CarAndModeForTrip(1, "Driving", mockCars[2])
        )

        fun getMockCars() = mockCars
        fun getMockTrip() = mockTrip
        fun getMockTrips() = mockTrips
        fun getMockTripWithTracks() = mockTripWithTracks
        fun getChips() = chips
    }
}
