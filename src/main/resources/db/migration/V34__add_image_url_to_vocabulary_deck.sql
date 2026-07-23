ALTER TABLE vocabulary_deck
    ADD COLUMN image_url TEXT;

UPDATE vocabulary_deck
SET image_url = 'https://media.zim.vn/681b1982d04ca1b008193a34/1000-tu-vung-tieng-anh-theo-chu-de.jpg',
    updated_at = NOW()
WHERE slug = '1000-tu-tieng-anh-thong-dung';

COMMENT ON COLUMN vocabulary_deck.image_url IS 'Cover image URL for the vocabulary deck';
