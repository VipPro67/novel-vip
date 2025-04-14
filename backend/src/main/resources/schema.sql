-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    total_novels INTEGER NOT NULL DEFAULT 0
);

-- Novels table
CREATE TABLE IF NOT EXISTS novels (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    title_nomalized VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    author VARCHAR(255) NOT NULL,
    cover_image VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_chapters INTEGER NOT NULL DEFAULT 0,
    views INTEGER NOT NULL DEFAULT 0,
    rating INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Novel-Categories join table
CREATE TABLE IF NOT EXISTS novel_categories (
    novel_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (novel_id, category_id),
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_novel_slug ON novels(slug);
CREATE INDEX IF NOT EXISTS idx_category_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_novel_categories ON novel_categories(novel_id, category_id);