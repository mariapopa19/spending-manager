-- Import hash now includes the person: date|amount|currency|description|source|person_id
-- Recompute existing hashes so re-importing old statements is still detected as DUPLICATE.
-- Must stay byte-for-byte identical to ImportHasher's canonical string.
UPDATE transaction
SET import_hash = encode(
        sha256(convert_to(
                concat_ws('|',
                          to_char(date, 'YYYY-MM-DD'),
                          amount::text,
                          currency,
                          description,
                          source,
                          person_id::text),
                'UTF8')),
        'hex')
WHERE import_hash IS NOT NULL;
