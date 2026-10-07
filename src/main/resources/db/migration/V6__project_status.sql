ALTER TABLE projects ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS description_text TEXT;
CREATE INDEX idx_projects_status ON projects(status);