ALTER TABLE execution_history DROP CONSTRAINT fk_transaction;
ALTER TABLE execution_history DROP CONSTRAINT fk_user;

ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE users ADD CONSTRAINT unique_user_id UNIQUE (userid);

ALTER TABLE transactions DROP CONSTRAINT transactions_pkey;
ALTER TABLE transactions ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE transactions ADD CONSTRAINT unique_transaction_id UNIQUE (transaction_id);

ALTER TABLE execution_history
    ADD CONSTRAINT fk_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id);
ALTER TABLE execution_history
    ADD CONSTRAINT fk_user FOREIGN KEY (userid) REFERENCES users (userid);

