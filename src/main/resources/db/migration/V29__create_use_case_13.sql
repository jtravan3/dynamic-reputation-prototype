INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 13', 0, 30, 0, 0, 10);

create or replace view use_case_13 as
select * from execution_history where use_case = 'Use Case 13';