INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 3', 0, 10, 0, 25, 25);

create or replace view use_case_3 as
select * from execution_history where use_case = 'Use Case 3';