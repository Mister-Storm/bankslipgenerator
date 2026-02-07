package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

object BankslipSql {
    const val INSERT = """
        INSERT INTO bankslips (
            id, bank_code, document_number, barcode, digitable_line, amount, due_date, issue_date, status,
            payer_name, payer_document_number, payer_street, payer_number, payer_complement, 
            payer_neighborhood, payer_city, payer_state, payer_zip_code,
            beneficiary_name, beneficiary_document_number, beneficiary_agency_number, 
            beneficiary_account_number, beneficiary_account_digit,
            beneficiary_street, beneficiary_number, beneficiary_complement,
            beneficiary_neighborhood, beneficiary_city, beneficiary_state, beneficiary_zip_code,
            payment_date, paid_amount,
            discount_type, discount_value, discount_limit_date,
            fine_type, fine_value, fine_start_date,
            interest_type, interest_value,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    const val UPDATE = """
        UPDATE bankslips SET
            status = ?,
            payment_date = ?,
            paid_amount = ?,
            updated_at = ?
        WHERE id = ?
    """

    const val FIND_BY_ID = """
        SELECT * FROM bankslips WHERE id = ? AND deleted_at IS NULL
    """

    const val FIND_BY_DOCUMENT_NUMBER = """
        SELECT * FROM bankslips WHERE document_number = ? AND deleted_at IS NULL
    """

    const val FIND_BY_BARCODE = """
        SELECT * FROM bankslips WHERE barcode = ? AND deleted_at IS NULL
    """

    const val FIND_BY_STATUS = """
        SELECT * FROM bankslips WHERE status = ? AND deleted_at IS NULL ORDER BY created_at DESC
    """

    const val FIND_BY_DUE_DATE_BETWEEN = """
        SELECT * FROM bankslips WHERE due_date BETWEEN ? AND ? AND deleted_at IS NULL ORDER BY due_date
    """

    const val FIND_BY_PAYER_DOCUMENT = """
        SELECT * FROM bankslips WHERE payer_document_number = ? AND deleted_at IS NULL ORDER BY created_at DESC
    """

    const val SOFT_DELETE = """
        UPDATE bankslips SET deleted_at = ? WHERE id = ?
    """
}

