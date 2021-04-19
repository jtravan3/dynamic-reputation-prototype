INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 5', 0, 30, 0, 45, 45);

create or replace view use_case_5 as
select * from execution_history where use_case = 'Use Case 5';