-- Query track_segments which are part of multiple track_segments belonging to the same track
SELECT id, track_id, number
FROM track_segment
WHERE track_id IN (
	SELECT track_id
	FROM track_segment
	GROUP BY track_id
	HAVING COUNT(*) > 1
)

-- Update track_segment number
UPDATE track_segment
SET number = 1

-- Validate
SELECT DISTINCT number
FROM track_segment
