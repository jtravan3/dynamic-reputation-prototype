INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 9', 0, 30, 0, 25, 10);

create or replace view use_case_9 as
select * from execution_history where use_case = 'Use Case 9';