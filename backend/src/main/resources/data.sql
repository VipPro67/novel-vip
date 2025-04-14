-- Insert sample categories
INSERT INTO categories (id, name, slug, description, total_novels) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Action', 'action', 'Novels with intense physical activity, fights, and thrilling sequences', 0),
('550e8400-e29b-41d4-a716-446655440001', 'Romance', 'romance', 'Love stories and romantic relationships', 0),
('550e8400-e29b-41d4-a716-446655440002', 'Fantasy', 'fantasy', 'Stories involving magic, mythical creatures, and supernatural elements', 0),
('550e8400-e29b-41d4-a716-446655440003', 'Martial Arts', 'martial-arts', 'Stories focused on martial arts cultivation and combat', 0),
('550e8400-e29b-41d4-a716-446655440004', 'Adventure', 'adventure', 'Exciting journeys and explorations', 0),
('550e8400-e29b-41d4-a716-446655440005', 'Mystery', 'mystery', 'Intriguing stories with suspense and detective elements', 0);

-- Insert sample novels
INSERT INTO novels (id, title, title_nomalized, slug, description, author, cover_image, status, total_chapters, views, rating, created_at, updated_at) VALUES
(
    '660e8400-e29b-41d4-a716-446655440000',
    'The Dragon Warrior''s Path',
    'THE DRAGON WARRIORS PATH',
    'the-dragon-warriors-path-660e8400',
    'A young warrior discovers his hidden dragon bloodline and must master both martial arts and magic to save his realm from ancient evil.',
    'Chen Wei Ming',
    'https://res.cloudinary.com/sample/image/upload/v1/novel/covers/dragon-warrior.jpg',
    'ongoing',
    100,
    5000,
    4,
    NOW(),
    NOW()
),
(
    '660e8400-e29b-41d4-a716-446655440001',
    'Eternal Love Curse',
    'ETERNAL LOVE CURSE',
    'eternal-love-curse-660e8400',
    'A immortal cultivator and a mortal healer find their fates intertwined through multiple lifetimes.',
    'Lin Mei Hua',
    'https://res.cloudinary.com/sample/image/upload/v1/novel/covers/eternal-love.jpg',
    'completed',
    200,
    8000,
    5,
    NOW(),
    NOW()
),
(
    '660e8400-e29b-41d4-a716-446655440002',
    'Mystery of the Ancient Scrolls',
    'MYSTERY OF THE ANCIENT SCROLLS',
    'mystery-of-the-ancient-scrolls-660e8400',
    'An archaeologist discovers ancient scrolls that contain secrets that could change the world.',
    'Sarah Johnson',
    'https://res.cloudinary.com/sample/image/upload/v1/novel/covers/ancient-scrolls.jpg',
    'ongoing',
    50,
    3000,
    4,
    NOW(),
    NOW()
);

-- Insert novel-category relationships
INSERT INTO novel_categories (novel_id, category_id) VALUES
('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000'), -- Dragon Warrior - Action
('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440003'), -- Dragon Warrior - Martial Arts
('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002'), -- Dragon Warrior - Fantasy
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001'), -- Eternal Love - Romance
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002'), -- Eternal Love - Fantasy
('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'), -- Ancient Scrolls - Mystery
('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'); -- Ancient Scrolls - Adventure

-- Insert sample chapters for the first novel
INSERT INTO chapters (id, title, chapter_number, content, views, json_url, created_at, updated_at, novel_id) VALUES
(
    '770e8400-e29b-41d4-a716-446655440000',
    'The Awakening',
    1,
    '<p>In the misty peaks of Mount Tianlong, young Lei Wei felt something stirring within his blood. The ancient dragons were calling...</p>',
    100,
    'https://res.cloudinary.com/sample/raw/upload/v1/novel/chapters/dragon-warrior-chap-1.json',
    NOW(),
    NOW(),
    '660e8400-e29b-41d4-a716-446655440000'
),
(
    '770e8400-e29b-41d4-a716-446655440001',
    'Dragon Blood Legacy',
    2,
    '<p>Master Chen looked at Lei Wei with newfound respect. "You carry the blood of the ancient dragon clans. It is time you learned the truth about your heritage."</p>',
    80,
    'https://res.cloudinary.com/sample/raw/upload/v1/novel/chapters/dragon-warrior-chap-2.json',
    NOW(),
    NOW(),
    '660e8400-e29b-41d4-a716-446655440000'
);

-- Insert sample chapters for the second novel
INSERT INTO chapters (id, title, chapter_number, content, views, json_url, created_at, updated_at, novel_id) VALUES
(
    '770e8400-e29b-41d4-a716-446655440002',
    'First Life',
    1,
    '<p>The scent of herbs filled the small medical hall as Yue Hua carefully ground the medicines. She had no idea that her ordinary life was about to change forever...</p>',
    150,
    'https://res.cloudinary.com/sample/raw/upload/v1/novel/chapters/eternal-love-chap-1.json',
    NOW(),
    NOW(),
    '660e8400-e29b-41d4-a716-446655440001'
),
(
    '770e8400-e29b-41d4-a716-446655440003',
    'The Immortal',
    2,
    '<p>He appeared like a god descending from the heavens, his white robes untouched by the rain. Yue Hua felt her heart skip a beat...</p>',
    120,
    'https://res.cloudinary.com/sample/raw/upload/v1/novel/chapters/eternal-love-chap-2.json',
    NOW(),
    NOW(),
    '660e8400-e29b-41d4-a716-446655440001'
);