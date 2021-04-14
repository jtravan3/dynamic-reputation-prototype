ALTER TABLE recalculation_metrics ADD COLUMN use_case VARCHAR (100);

update recalculation_metrics set use_case = 'Use Case 2';