package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

object CnabFileSql {
    const val INSERT = """
        INSERT INTO cnab_files (
            id, bank_code, file_type, version, file_name, file_content,
            file_url, status, total_records, processed_records,
            error_records, errors, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
    """

    const val UPDATE = """
        UPDATE cnab_files SET
            status = ?,
            processed_records = ?,
            error_records = ?,
            errors = ?::jsonb,
            processed_at = ?
        WHERE id = ?
    """

    const val FIND_BY_ID = """
        SELECT id, bank_code, file_type, version, file_name, file_content,
               file_url, status, total_records, processed_records,
               error_records, errors, created_at, processed_at
        FROM cnab_files
        WHERE id = ?
    """

    const val FIND_BY_BANK_CODE = """
        SELECT id, bank_code, file_type, version, file_name, file_content,
               file_url, status, total_records, processed_records,
               error_records, errors, created_at, processed_at
        FROM cnab_files
        WHERE bank_code = ?
        ORDER BY created_at DESC
    """

    const val FIND_BY_FILE_TYPE = """
        SELECT id, bank_code, file_type, version, file_name, file_content,
               file_url, status, total_records, processed_records,
               error_records, errors, created_at, processed_at
        FROM cnab_files
        WHERE file_type = ?
        ORDER BY created_at DESC
    """

    const val FIND_BY_STATUS = """
        SELECT id, bank_code, file_type, version, file_name, file_content,
               file_url, status, total_records, processed_records,
               error_records, errors, created_at, processed_at
        FROM cnab_files
        WHERE status = ?
        ORDER BY created_at DESC
    """

    const val FIND_ALL = """
        SELECT id, bank_code, file_type, version, file_name, file_content,
               file_url, status, total_records, processed_records,
               error_records, errors, created_at, processed_at
        FROM cnab_files
        ORDER BY created_at DESC
    """

    const val MARK_PROCESSED = """
        UPDATE cnab_files
        SET status = 'PROCESSED', processed_at = ?
        WHERE id = ?
    """
}

