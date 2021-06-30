INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 19', 0, 30, 0, 0, 10);

create or replace view use_case_19 as
select * from execution_history where use_case = 'Use Case 19';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 20', 0, 30, 0, 50, 10);

create or replace view use_case_20 as
select * from execution_history where use_case = 'Use Case 20';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 21', 0, 30, 0, 25, 10);

create or replace view use_case_21 as
select * from execution_history where use_case = 'Use Case 21';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 22', 0, 30, 0, 20, 10);

create or replace view use_case_22 as
select * from execution_history where use_case = 'Use Case 22';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 23', 0, 30, 0, 22, 10);

create or replace view use_case_23 as
select * from execution_history where use_case = 'Use Case 23';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 24', 0, 30, 0, 75, 10);

create or replace view use_case_24 as
select * from execution_history where use_case = 'Use Case 24';

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 25', 0, 30, 0, 100, 10);

create or replace view use_case_25 as
select * from execution_history where use_case = 'Use Case 25';