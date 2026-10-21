-- U15__workflow_approval_engine.sql
-- Rollback script for Workflows & Approvals Engine

DROP TABLE IF EXISTS configuration_workflow_actions CASCADE;
DROP TABLE IF EXISTS configuration_workflow_instances CASCADE;
DROP TABLE IF EXISTS configuration_workflow_steps CASCADE;
DROP TABLE IF EXISTS configuration_workflows CASCADE;
