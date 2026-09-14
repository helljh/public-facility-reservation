CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'reservation_no_overlap'
    ) THEN

ALTER TABLE reservation
    ADD CONSTRAINT reservation_no_overlap
    EXCLUDE USING gist (
            room_id WITH =,
            reservation_date WITH =,
            tsrange(
                reservation_date + start_time,
                reservation_date + end_time,
                '[)'
            ) WITH &&
        )
        WHERE (status = 'RESERVED');

END IF;
END
$$;