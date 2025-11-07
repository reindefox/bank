-- Insert sample accounts
INSERT INTO accounts (id, account_number, balance, currency, created_at) VALUES
('acc1', 'ACC-001', 1000.00, 'USD', CURRENT_TIMESTAMP),
('acc2', 'ACC-002', 500.50, 'EUR', CURRENT_TIMESTAMP),
('acc3', 'ACC-003', 2500.75, 'RUB', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Insert sample transactions
INSERT INTO transactions (id, account_id, amount, currency, description, transaction_type, created_at) VALUES
('t1', 'acc1', 100.00, 'USD', 'Salary payment', 'INCOME', CURRENT_TIMESTAMP - INTERVAL '10 days'),
('t2', 'acc1', -25.50, 'USD', 'Groceries', 'EXPENSE', CURRENT_TIMESTAMP - INTERVAL '8 days'),
('t3', 'acc1', -12.00, 'USD', 'Coffee', 'EXPENSE', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('t4', 'acc1', 200.00, 'USD', 'Freelance work', 'INCOME', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('t5', 'acc1', -8.40, 'USD', 'Snacks', 'EXPENSE', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('t6', 'acc2', 150.00, 'EUR', 'Payment received', 'INCOME', CURRENT_TIMESTAMP - INTERVAL '7 days'),
('t7', 'acc2', -30.00, 'EUR', 'Shopping', 'EXPENSE', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('t8', 'acc3', 500.00, 'RUB', 'Deposit', 'INCOME', CURRENT_TIMESTAMP - INTERVAL '6 days'),
('t9', 'acc3', -100.00, 'RUB', 'Withdrawal', 'EXPENSE', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

