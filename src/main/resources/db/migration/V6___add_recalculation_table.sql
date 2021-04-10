CREATE TABLE recalculation_metrics (
    id  serial primary key not null,
    num_of_execution_history integer not null,
    num_of_users integer not null,
    num_of_transactions integer not null,
    time_to_recalculate decimal not null,
    time_executed TIMESTAMP NOT NULL DEFAULT now()
);