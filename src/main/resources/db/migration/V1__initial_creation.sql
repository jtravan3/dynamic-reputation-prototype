CREATE TABLE users (
    userid varchar (50) primary key not null ,
    user_ranking decimal
);

CREATE TABLE transactions (
    transaction_id varchar (50) primary key not null ,
    transaction_commit_ranking decimal not null,
    transaction_system_ranking decimal not null,
    transaction_eff_ranking decimal not null,
    transaction_num_of_operations integer not null
);

CREATE TABLE execution_history (
    id  serial primary key not null,
    userid varchar (50) not null,
    user_ranking decimal not null,
    transaction_id varchar(50) not null,
    transaction_commit_ranking decimal not null,
    transaction_system_ranking decimal not null,
    transaction_eff_ranking decimal not null,
    transaction_num_of_operations integer not null,
    reputation_score decimal not null,
    action_taken varchar(20) not null,
    transaction_execution_time decimal not null,
    percentage_affected decimal not null,
    recalculation_needed boolean not null default 'n',
    time_executed TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_user
        FOREIGN KEY(userid)
            REFERENCES users(userid),
    CONSTRAINT fk_transaction
        FOREIGN KEY(transaction_id)
            REFERENCES transactions(transaction_id)
);

