-- V15__workflow_approval_engine.sql
-- Workflows & Approvals Engine Schema for MAX ERP

CREATE TABLE IF NOT EXISTS configuration_workflows (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    business_object_id UUID NOT NULL REFERENCES configuration_business_objects(id) ON DELETE CASCADE,
    workflow_key VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    trigger_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL_SUBMISSION',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_configuration_workflows_key UNIQUE (tenant_id, workflow_key),
    CONSTRAINT chk_configuration_workflows_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_configuration_workflows_trigger CHECK (trigger_type IN ('MANUAL_SUBMISSION', 'RECORD_CREATION', 'RECORD_UPDATE', 'FIELD_CHANGE'))
);

CREATE INDEX IF NOT EXISTS idx_configuration_workflows_tenant ON configuration_workflows(tenant_id);
CREATE INDEX IF NOT EXISTS idx_configuration_workflows_bo ON configuration_workflows(business_object_id);
CREATE INDEX IF NOT EXISTS idx_configuration_workflows_status ON configuration_workflows(tenant_id, status);

CREATE TABLE IF NOT EXISTS configuration_workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL REFERENCES configuration_workflows(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    step_type VARCHAR(50) NOT NULL DEFAULT 'APPROVAL',
    approver_role_id UUID REFERENCES security_roles(id) ON DELETE SET NULL,
    approver_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    condition_expression TEXT,
    status_on_approval VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
    status_on_rejection VARCHAR(50) NOT NULL DEFAULT 'REJECTED',
    timeout_hours INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workflow_step_order UNIQUE (workflow_id, step_order),
    CONSTRAINT chk_workflow_step_type CHECK (step_type IN ('APPROVAL', 'NOTIFICATION', 'AUTO_ACTION'))
);

CREATE INDEX IF NOT EXISTS idx_workflow_steps_workflow ON configuration_workflow_steps(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_order ON configuration_workflow_steps(workflow_id, step_order);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_approver_role ON configuration_workflow_steps(approver_role_id);

CREATE TABLE IF NOT EXISTS configuration_workflow_instances (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    workflow_id UUID NOT NULL REFERENCES configuration_workflows(id) ON DELETE RESTRICT,
    business_object_id UUID NOT NULL REFERENCES configuration_business_objects(id) ON DELETE RESTRICT,
    record_id UUID NOT NULL REFERENCES configuration_business_object_records(id) ON DELETE CASCADE,
    current_step_id UUID REFERENCES configuration_workflow_steps(id) ON DELETE SET NULL,
    current_step_order INT NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    initiated_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    initiated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    context_data JSONB DEFAULT '{}'::jsonb,
    CONSTRAINT chk_workflow_instance_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_workflow_instances_tenant ON configuration_workflow_instances(tenant_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_record ON configuration_workflow_instances(record_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_status ON configuration_workflow_instances(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_step ON configuration_workflow_instances(current_step_id);

CREATE TABLE IF NOT EXISTS configuration_workflow_actions (
    id UUID PRIMARY KEY,
    instance_id UUID NOT NULL REFERENCES configuration_workflow_instances(id) ON DELETE CASCADE,
    step_id UUID NOT NULL REFERENCES configuration_workflow_steps(id) ON DELETE RESTRICT,
    actor_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action VARCHAR(30) NOT NULL,
    comments TEXT NOT NULL,
    action_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_workflow_action_type CHECK (action IN ('APPROVED', 'REJECTED', 'DELEGATED'))
);

CREATE INDEX IF NOT EXISTS idx_workflow_actions_instance ON configuration_workflow_actions(instance_id);
CREATE INDEX IF NOT EXISTS idx_workflow_actions_actor ON configuration_workflow_actions(actor_user_id);
