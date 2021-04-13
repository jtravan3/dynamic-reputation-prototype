CREATE TABLE use_case_metrics
(
    id                          serial primary key not null,
    name                        varchar(100)       not null,
    total_transactions_executed integer            not null,
    recalculation_percentage    integer            not null,
    total_affected_transactions integer            not null,
    conflicting_percentage      integer            not null,
    abort_percentage            integer            not null,
    time_executed               TIMESTAMP          NOT NULL DEFAULT now()
);

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 1', 23230, 50, 637, 10, 5);

INSERT INTO use_case_metrics (name, total_transactions_executed, recalculation_percentage, total_affected_transactions,
                              conflicting_percentage, abort_percentage)
VALUES ('Use Case 2', 0, 10, 0, 25, 25);