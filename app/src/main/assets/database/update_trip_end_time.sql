-- Query trip and its tracks
SELECT
    a.id AS trip_id,
    b.id AS track_id,
    b.`start`,
    b.`end`
FROM trip a
INNER JOIN track b ON b.trip_id = a.id

-- Query trip and its earliest and latest tracks
SELECT
    a.id AS trip_id,
    MIN(b.`start`) AS earliest_track_start_time,
    a.`start`,
    MAX(b.`end`) AS latest_track_end_time,
    a.`end`
FROM trip a
INNER JOIN track b ON b.trip_id = a.id
GROUP BY a.id

-- Update trip end time
UPDATE trip
SET `end` = (
    SELECT MAX(b.`end`)
   FROM trip a
   INNER JOIN track b ON b.trip_id = a.id
   WHERE a.id = trip.id
   GROUP BY a.id
)

-- Validate
SELECT * FROM (
    SELECT
        a.id AS trip_id,
        MIN(b.`start`) AS earliest_track_start_time,
        a.`start`,
        MAX(b.`end`) AS latest_track_end_time,
        a.`end`
    FROM trip a
    INNER JOIN track b ON b.trip_id = a.id
    GROUP BY a.id
) WHERE `end` <> latest_track_end_time
