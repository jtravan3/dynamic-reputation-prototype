ALTER TABLE execution_history ADD COLUMN use_case VARCHAR (100);

UPDATE execution_history SET use_case = 'Use Case 1' WHERE id <= 23634;
UPDATE execution_history SET use_case = 'Use Case 2' WHERE id > 23634;