CREATE TABLE execution_history (
    userid varchar (50),
    user_ranking varchar(20),
    transaction_commit_ranking varchar(20),
    transaction_system_ranking varchar(20),
    transaction_eff_ranking varchar(20),
    transaction_num_of_operations varchar(20),
    action_taken varchar(20),
    reputation_score varchar(20),
    transaction_execution_time varchar (50) not null,
    percentage_affected varchar (10) not null,
    recalculation_needed boolean not null default 'n',
    time_executed TIMESTAMP NOT NULL
);

