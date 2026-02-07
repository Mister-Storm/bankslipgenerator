-- Create bankslips table
CREATE TABLE bankslips (
    id UUID PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(100) NOT NULL UNIQUE,
    digitable_line VARCHAR(100) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    due_date DATE NOT NULL,
    issue_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,

    -- Payer information
    payer_name VARCHAR(255) NOT NULL,
    payer_document_number VARCHAR(20) NOT NULL,
    payer_street VARCHAR(255) NOT NULL,
    payer_number VARCHAR(20) NOT NULL,
    payer_complement VARCHAR(100),
    payer_neighborhood VARCHAR(100) NOT NULL,
    payer_city VARCHAR(100) NOT NULL,
    payer_state VARCHAR(2) NOT NULL,
    payer_zip_code VARCHAR(10) NOT NULL,

    -- Beneficiary information
    beneficiary_name VARCHAR(255) NOT NULL,
    beneficiary_document_number VARCHAR(20) NOT NULL,
    beneficiary_agency_number VARCHAR(10) NOT NULL,
    beneficiary_account_number VARCHAR(20) NOT NULL,
    beneficiary_account_digit VARCHAR(2) NOT NULL,
    beneficiary_street VARCHAR(255) NOT NULL,
    beneficiary_number VARCHAR(20) NOT NULL,
    beneficiary_complement VARCHAR(100),
    beneficiary_neighborhood VARCHAR(100) NOT NULL,
    beneficiary_city VARCHAR(100) NOT NULL,
    beneficiary_state VARCHAR(2) NOT NULL,
    beneficiary_zip_code VARCHAR(10) NOT NULL,

    -- Payment information
    payment_date TIMESTAMP,
    paid_amount DECIMAL(19,2),

    -- Discount information
    discount_type VARCHAR(20),
    discount_value DECIMAL(19,2),
    discount_limit_date DATE,

    -- Fine information
    fine_type VARCHAR(20),
    fine_value DECIMAL(19,2),
    fine_start_date DATE,

    -- Interest information
    interest_type VARCHAR(20),
    interest_value DECIMAL(19,2),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_bankslips_bank_code ON bankslips(bank_code);
CREATE INDEX idx_bankslips_status ON bankslips(status);
CREATE INDEX idx_bankslips_due_date ON bankslips(due_date);
CREATE INDEX idx_bankslips_payer_document ON bankslips(payer_document_number);
CREATE INDEX idx_bankslips_deleted_at ON bankslips(deleted_at);

-- Create instructions table
CREATE TABLE bankslip_instructions (
    bankslip_id UUID NOT NULL,
    instruction VARCHAR(255) NOT NULL,
    FOREIGN KEY (bankslip_id) REFERENCES bankslips(id) ON DELETE CASCADE
);

CREATE INDEX idx_bankslip_instructions_bankslip_id ON bankslip_instructions(bankslip_id);

