-- Query track and its track_points
SELECT
    b.id AS track_id,
    b.trip_id,
    d.time
FROM track b
INNER JOIN track_segment c ON c.track_id = b.id
INNER JOIN track_point d ON d.track_segment_id = c.id

-- Query track and its earliest and latest track_points
SELECT
    b.id AS track_id,
    b.trip_id,
    MIN(d.time) AS earliest,
    b.`start`,
    MAX(d.time) AS latest,
    b.`end`
FROM track b
INNER JOIN track_segment c ON c.track_id = b.id
INNER JOIN track_point d ON d.track_segment_id = c.id
GROUP BY b.id

-- Update track end time
UPDATE track
SET `end` = (
    SELECT MAX(d.time)
    FROM track b
    INNER JOIN track_segment c ON c.track_id = b.id
    INNER JOIN track_point d ON d.track_segment_id = c.id
    WHERE b.id = track.id
    GROUP BY b.id
)
--WHERE EXISTS (
--    SELECT MAX(d.time)
--    FROM track b
--    INNER JOIN track_segment c ON c.track_id = b.id
--    INNER JOIN track_point d ON d.track_segment_id = c.id
--    WHERE b.id = track.id
--    GROUP BY b.id
--)

-- Validate
SELECT * FROM (
    SELECT
        b.id AS track_id,
        b.trip_id,
        MIN(d.time) AS earliest,
        b.`start`,
        MAX(d.time) AS latest,
        b.`end`
    FROM track b
    INNER JOIN track_segment c ON c.track_id = b.id
    INNER JOIN track_point d ON d.track_segment_id = c.id
    GROUP BY b.id
) WHERE `end` <> latest
