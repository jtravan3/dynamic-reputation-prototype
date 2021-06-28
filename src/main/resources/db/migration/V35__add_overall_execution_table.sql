CREATE TABLE overall_execution_history (
    id  serial primary key not null,
    overall_execution_id varchar (100) not null,
    overall_execution_time decimal not null,
    scheduler_type VARCHAR (50) NOT NULL DEFAULT 'DRP',
    time_executed TIMESTAMP NOT NULL DEFAULT now()
);