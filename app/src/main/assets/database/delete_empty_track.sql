-- Query empty track segments
SELECT *
FROM track_segment c
LEFT JOIN track_point d ON d.track_segment_id = c.id
WHERE d.id IS NULL

-- Delete track segments with no track points
DELETE FROM track_segment
WHERE id IN (
	SELECT c.id
	FROM track_segment c
	LEFT JOIN track_point d ON d.track_segment_id = c.id
	WHERE d.id IS NULL
)

-- Query empty tracks
SELECT *
FROM track b
LEFT JOIN track_segment c ON c.track_id = b.id
WHERE c.id IS NULL

-- Delete tracks with no track segments
DELETE FROM track
WHERE id IN (
	SELECT b.id
	FROM track b
	LEFT JOIN track_segment c ON c.track_id = b.id
	WHERE c.id IS NULL
)

-- Query empty trips
SELECT *
FROM trip a
LEFT JOIN track b ON b.trip_id = a.id
WHERE b.id IS NULL

-- Delete trips with no tracks
DELETE FROM trip
WHERE id IN (
	SELECT a.id
	FROM trip a
	LEFT JOIN track b ON b.trip_id = a.id
	WHERE b.id IS NULL
)
