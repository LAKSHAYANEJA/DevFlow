CREATE TABLE labels (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_project_label_name UNIQUE (project_id, name)
);

CREATE TABLE task_labels (
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    label_id BIGINT NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, label_id)
);

CREATE INDEX idx_labels_project_id ON labels(project_id);
CREATE INDEX idx_task_labels_label_id ON task_labels(label_id);