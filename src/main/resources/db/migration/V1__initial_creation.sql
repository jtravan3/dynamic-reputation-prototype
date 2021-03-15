CREATE TABLE execution_history (
    id  serial primary key not null,
    userid varchar (50),
    user_ranking decimal,
    transaction_commit_ranking decimal,
    transaction_system_ranking decimal,
    transaction_eff_ranking decimal,
    transaction_num_of_operations integer,
    action_taken varchar(20),
    reputation_score decimal,
    transaction_execution_time decimal not null,
    percentage_affected decimal not null,
    recalculation_needed boolean not null default 'n',
    time_executed TIMESTAMP NOT NULL DEFAULT now()
);

