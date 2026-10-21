-- ============================================================================
-- V16__recruiting_candidate_management.sql
-- Enterprise AI-Powered Recruiting & Candidate Management (RCM) Schema
-- Description:
--   Complete relational data model for MAX ERP Recruiting Suite:
--   - Configurable Talent Pipeline Stages & Ordering (rcm_pipeline_stages)
--   - Position-Linked Job Requisitions & Multi-Slot Headcount (rcm_job_requisitions, rcm_requisition_position_slots)
--   - Candidate Self-Service Accounts & Authentication (rcm_candidate_accounts)
--   - Candidate Ingestion, OFCCP Tracking & Knockout Screening (rcm_candidates)
--   - Interview Scheduling, Outlook Sync & MS Teams Integration (rcm_scheduled_interviews)
--   - Structured Competency Scorecards & Ratings (rcm_interview_scorecards)
--   - Offer Letter Templates & Digital Legal E-Signatures (rcm_offer_templates, rcm_candidate_offers)
--   - Recruiting Automation Rules Engine (rcm_recruiting_rules)
--   - Dynamic Application Form Builder & Questions (rcm_application_form_configs)
--   - Immutable Audit Ledger for Compliance (rcm_candidate_audit_logs)
-- ============================================================================

-- 1. CONFIGURABLE PIPELINE STAGES
CREATE TABLE IF NOT EXISTS rcm_pipeline_stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    stage_key VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    stage_order INT NOT NULL,
    stage_category VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, TERMINAL_SUCCESS, TERMINAL_REJECT
    sla_days INT DEFAULT 5,
    is_visible_to_candidate BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rcm_pipeline_stage_key UNIQUE (tenant_id, stage_key),
    CONSTRAINT uq_rcm_pipeline_stage_order UNIQUE (tenant_id, stage_order),
    CONSTRAINT chk_rcm_pipeline_stage_cat CHECK (stage_category IN ('ACTIVE', 'TERMINAL_SUCCESS', 'TERMINAL_REJECT', 'ON_HOLD'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_stages_tenant_order ON rcm_pipeline_stages(tenant_id, stage_order);

-- 2. JOB REQUISITIONS (POSITION-LINKED)
CREATE TABLE IF NOT EXISTS rcm_job_requisitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    req_number VARCHAR(50) NOT NULL,
    position_id UUID,
    title VARCHAR(200) NOT NULL,
    department VARCHAR(100) NOT NULL,
    job_level VARCHAR(50) NOT NULL,
    employment_type VARCHAR(50) NOT NULL DEFAULT 'FULL_TIME',
    location VARCHAR(100) NOT NULL,
    work_arrangement VARCHAR(50) NOT NULL DEFAULT 'HYBRID',
    target_headcount INT NOT NULL DEFAULT 1,
    filled_headcount INT NOT NULL DEFAULT 0,
    salary_range_min NUMERIC(12, 2),
    salary_range_max NUMERIC(12, 2),
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    hiring_manager_id UUID REFERENCES users(id) ON DELETE SET NULL,
    hiring_manager_name VARCHAR(150),
    recruiter_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recruiter_name VARCHAR(150),
    job_description TEXT,
    required_skills JSONB NOT NULL DEFAULT '[]'::jsonb,
    preferred_skills JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    approval_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    current_approval_step INT DEFAULT 1,
    referral_bonus_amount NUMERIC(10, 2) DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rcm_req_number UNIQUE (tenant_id, req_number),
    CONSTRAINT chk_rcm_req_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'OPEN', 'INTERVIEWING', 'OFFER_OUT', 'FILLED', 'ON_HOLD', 'CANCELLED')),
    CONSTRAINT chk_rcm_req_work_arr CHECK (work_arrangement IN ('REMOTE', 'HYBRID', 'ONSITE'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_req_tenant_status ON rcm_job_requisitions(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rcm_req_dept ON rcm_job_requisitions(tenant_id, department);

-- 3. POSITION HEADCOUNT SLOTS
CREATE TABLE IF NOT EXISTS rcm_requisition_position_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    requisition_id UUID NOT NULL REFERENCES rcm_job_requisitions(id) ON DELETE CASCADE,
    slot_code VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNFILLED',
    assigned_candidate_id UUID,
    filled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rcm_slot_code UNIQUE (tenant_id, requisition_id, slot_code),
    CONSTRAINT chk_rcm_slot_status CHECK (status IN ('UNFILLED', 'INTERVIEWING', 'OFFER_OUT', 'FILLED'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_slots_req ON rcm_requisition_position_slots(tenant_id, requisition_id);

-- 4. CANDIDATE ACCOUNTS (EXTERNAL AUTH)
CREATE TABLE IF NOT EXISTS rcm_candidate_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    email VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(50),
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    verification_token VARCHAR(100),
    last_login_at TIMESTAMPTZ,
    resume_storage_key VARCHAR(255),
    parsed_profile JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rcm_cand_account_email UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_rcm_cand_acc_email ON rcm_candidate_accounts(tenant_id, email);

-- 5. CANDIDATE APPLICATIONS (ONE EMAIL PER JR POLICY ENFORCED)
CREATE TABLE IF NOT EXISTS rcm_candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    candidate_account_id UUID REFERENCES rcm_candidate_accounts(id) ON DELETE SET NULL,
    requisition_id UUID NOT NULL REFERENCES rcm_job_requisitions(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(200) NOT NULL,
    phone VARCHAR(50),
    portfolio_url VARCHAR(255),
    stage_id UUID REFERENCES rcm_pipeline_stages(id) ON DELETE SET NULL,
    stage_key VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    source VARCHAR(50) NOT NULL DEFAULT 'CAREERS_PORTAL',
    is_referred BOOLEAN NOT NULL DEFAULT FALSE,
    referrer_name VARCHAR(150),
    referrer_email VARCHAR(200),
    internal_employee_id VARCHAR(50),
    has_work_authorization BOOLEAN NOT NULL DEFAULT TRUE,
    meets_prerequisites BOOLEAN NOT NULL DEFAULT TRUE,
    resume_summary TEXT,
    resume_document_id VARCHAR(255),
    ai_match_score NUMERIC(5, 2) DEFAULT 0.00,
    ai_scorecard JSONB DEFAULT '{}'::jsonb,
    interview_score NUMERIC(3, 2) DEFAULT 0.00,
    assigned_position_slot_id UUID REFERENCES rcm_requisition_position_slots(id) ON DELETE SET NULL,
    is_auto_knockout BOOLEAN NOT NULL DEFAULT FALSE,
    knockout_reason TEXT,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rcm_cand_req_email UNIQUE (tenant_id, requisition_id, email)
);

CREATE INDEX IF NOT EXISTS idx_rcm_candidates_req ON rcm_candidates(tenant_id, requisition_id);
CREATE INDEX IF NOT EXISTS idx_rcm_candidates_stage ON rcm_candidates(tenant_id, stage_key);
CREATE INDEX IF NOT EXISTS idx_rcm_candidates_email ON rcm_candidates(tenant_id, email);

-- 6. INTERVIEW SCHEDULING & TEAMS / OUTLOOK INTEGRATION
CREATE TABLE IF NOT EXISTS rcm_scheduled_interviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES rcm_candidates(id) ON DELETE CASCADE,
    requisition_id UUID NOT NULL REFERENCES rcm_job_requisitions(id) ON DELETE CASCADE,
    round_type VARCHAR(50) NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    scheduled_start TIMESTAMPTZ NOT NULL,
    scheduled_end TIMESTAMPTZ NOT NULL,
    platform VARCHAR(50) NOT NULL DEFAULT 'MICROSOFT_TEAMS',
    meeting_link VARCHAR(500),
    outlook_event_id VARCHAR(255),
    interviewer_user_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    interviewer_names JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    meeting_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rcm_interview_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED')),
    CONSTRAINT chk_rcm_interview_plat CHECK (platform IN ('MICROSOFT_TEAMS', 'OUTLOOK_INVITE', 'IN_PERSON', 'PHONE'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_interview_cand ON rcm_scheduled_interviews(tenant_id, candidate_id);
CREATE INDEX IF NOT EXISTS idx_rcm_interview_status ON rcm_scheduled_interviews(tenant_id, status);

-- 7. STRUCTURED INTERVIEW SCORECARDS
CREATE TABLE IF NOT EXISTS rcm_interview_scorecards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    interview_id UUID REFERENCES rcm_scheduled_interviews(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES rcm_candidates(id) ON DELETE CASCADE,
    interviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    interviewer_name VARCHAR(150) NOT NULL,
    round_type VARCHAR(50) NOT NULL,
    overall_rating INT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    recommendation VARCHAR(30) NOT NULL,
    competency_ratings JSONB NOT NULL DEFAULT '{}'::jsonb,
    written_feedback TEXT NOT NULL,
    key_strengths TEXT,
    areas_for_growth TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rcm_scorecard_rec CHECK (recommendation IN ('STRONG_HIRE', 'HIRE', 'HOLD', 'NO_HIRE'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_scorecards_cand ON rcm_interview_scorecards(tenant_id, candidate_id);

-- 8. OFFER LETTER TEMPLATES & FORMAL OFFERS
CREATE TABLE IF NOT EXISTS rcm_offer_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    template_name VARCHAR(150) NOT NULL,
    department VARCHAR(100),
    legal_entity VARCHAR(100),
    country VARCHAR(10),
    subject VARCHAR(200) NOT NULL,
    content_html TEXT NOT NULL,
    required_approval_roles JSONB DEFAULT '[]'::jsonb,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rcm_candidate_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES rcm_candidates(id) ON DELETE CASCADE,
    template_id UUID REFERENCES rcm_offer_templates(id) ON DELETE SET NULL,
    base_salary NUMERIC(12, 2) NOT NULL,
    signing_bonus NUMERIC(12, 2) DEFAULT 0.00,
    equity_options VARCHAR(100),
    target_start_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EXTENDED',
    offer_letter_html TEXT,
    is_signed BOOLEAN NOT NULL DEFAULT FALSE,
    signed_name VARCHAR(150),
    signed_at TIMESTAMPTZ,
    signed_ip_address VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rcm_offer_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'EXTENDED', 'ACCEPTED', 'DECLINED', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_offers_cand ON rcm_candidate_offers(tenant_id, candidate_id);

-- 9. RECRUITING AUTOMATION RULES ENGINE
CREATE TABLE IF NOT EXISTS rcm_recruiting_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    rule_name VARCHAR(150) NOT NULL,
    trigger_event VARCHAR(50) NOT NULL,
    execution_order INT NOT NULL DEFAULT 1,
    conditions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    actions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rcm_rule_trigger CHECK (trigger_event IN ('ON_APPLICATION_SUBMIT', 'ON_STAGE_CHANGE', 'ON_FEEDBACK_SUBMIT', 'ON_OFFER_EXTEND'))
);

CREATE INDEX IF NOT EXISTS idx_rcm_rules_tenant_trigger ON rcm_recruiting_rules(tenant_id, trigger_event, is_active);

-- 10. DYNAMIC APPLICATION FORM CONFIGURATIONS
CREATE TABLE IF NOT EXISTS rcm_application_form_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    requisition_id UUID REFERENCES rcm_job_requisitions(id) ON DELETE CASCADE,
    form_title VARCHAR(150) NOT NULL DEFAULT 'Standard Candidate Application',
    sections_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    screening_questions JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 11. AUDIT COMPLIANCE LOGS (OFCCP / EEOC)
CREATE TABLE IF NOT EXISTS rcm_candidate_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES configuration_tenants(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES rcm_candidates(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    performed_by_id UUID,
    performed_by_name VARCHAR(150) NOT NULL,
    previous_state JSONB,
    new_state JSONB,
    audit_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rcm_audit_cand_time ON rcm_candidate_audit_logs(tenant_id, candidate_id, audit_timestamp);
