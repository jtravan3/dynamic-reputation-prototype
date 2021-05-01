INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 6', 0, 12, 0, 45, -1);

create or replace view use_case_6 as
select * from execution_history where use_case = 'Use Case 6';