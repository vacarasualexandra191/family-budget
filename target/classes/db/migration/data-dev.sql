-- Roluri
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

-- Familia demo
INSERT INTO families (name, description, created_at)
VALUES ('Familia Demo', 'Familie pentru testare', NOW())
ON CONFLICT DO NOTHING;

-- User admin (parola: admin123 - BCrypt)
INSERT INTO users (username, password, full_name, email, enabled, created_at, family_id)
VALUES ('admin',
        '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFT4N7.WtlQA0vEOy',
        'Administrator', 'admin@familybudget.ro', true, NOW(),
        (SELECT id FROM families WHERE name = 'Familia Demo' LIMIT 1))
ON CONFLICT (username) DO NOTHING;

-- User obisnuit demo (parola: user123 - BCrypt)
INSERT INTO users (username, password, full_name, email, enabled, created_at, family_id)
VALUES ('user1',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXHFsOIXe4l8q1q1q1q1q1q1q1q',
        'Ion Popescu', 'ion@familybudget.ro', true, NOW(),
        (SELECT id FROM families WHERE name = 'Familia Demo' LIMIT 1))
ON CONFLICT (username) DO NOTHING;

-- Asignare roluri
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name IN ('ROLE_USER', 'ROLE_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'user1' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- Categorii de baza
INSERT INTO categories (name, type, icon) VALUES ('Salariu', 'INCOME', 'bi-wallet2') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Freelance', 'INCOME', 'bi-laptop') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Chiria', 'EXPENSE', 'bi-house') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Mancare', 'EXPENSE', 'bi-cart') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Transport', 'EXPENSE', 'bi-car-front') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Utilitati', 'EXPENSE', 'bi-lightning') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Sanatate', 'EXPENSE', 'bi-heart-pulse') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Divertisment', 'EXPENSE', 'bi-film') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Educatie', 'EXPENSE', 'bi-book') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, icon) VALUES ('Economii', 'INCOME', 'bi-piggy-bank') ON CONFLICT (name) DO NOTHING;

-- Taguri uzuale
INSERT INTO tags (name) VALUES ('urgent') ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name) VALUES ('vacanta') ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name) VALUES ('recurent') ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name) VALUES ('cadou') ON CONFLICT (name) DO NOTHING;
