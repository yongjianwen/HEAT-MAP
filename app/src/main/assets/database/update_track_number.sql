-- Query tracks which are part of multiple tracks belonging to the same trip
SELECT id, trip_id, number
FROM track
WHERE trip_id IN (
	SELECT trip_id
	FROM track
	GROUP BY trip_id
	HAVING COUNT(*) > 1
)

-- Query rank (actual track number)
SELECT
	id,
	trip_id,
	number,
	(
		SELECT COUNT() + 1
		FROM (
			SELECT DISTINCT id
			FROM track t
			WHERE id < track.id
			-- AND t.trip_id IN (
			-- 	SELECT trip_id
			-- 	FROM track
			-- 	GROUP BY trip_id
			-- 	HAVING COUNT(*) > 1
			-- )
			AND trip_id = track.trip_id
		)
	) AS rank
FROM track
WHERE trip_id IN (
	SELECT trip_id
	FROM track
	GROUP BY trip_id
	HAVING COUNT(*) > 1
)

-- Update track number
UPDATE track
SET number = (
	SELECT COUNT() + 1
	FROM (
		SELECT DISTINCT id
		FROM track t
		WHERE id < track.id
		AND trip_id = track.trip_id
	)
)
WHERE trip_id IN (
	SELECT trip_id
	FROM track
	GROUP BY trip_id
	HAVING COUNT(*) > 1
)
