create or replace view use_case_1 as
select * from execution_history where use_case = 'Use Case 1';

create or replace view use_case_2 as
select * from execution_history where use_case = 'Use Case 2';