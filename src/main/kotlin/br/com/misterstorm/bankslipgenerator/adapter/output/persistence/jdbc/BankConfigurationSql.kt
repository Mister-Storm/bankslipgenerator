package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

object BankConfigurationSql {
    const val INSERT = """
        INSERT INTO bank_configurations (
            id, bank_code, bank_name, layout_configuration, 
            cnab_configuration, validation_rules, is_active,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?)
    """

    const val UPDATE = """
        UPDATE bank_configurations SET
            bank_name = ?,
            layout_configuration = ?::jsonb,
            cnab_configuration = ?::jsonb,
            validation_rules = ?::jsonb,
            is_active = ?,
            updated_at = ?
        WHERE id = ?
    """

    const val FIND_BY_ID = """
        SELECT id, bank_code, bank_name, layout_configuration, 
               cnab_configuration, validation_rules, is_active,
               created_at, updated_at
        FROM bank_configurations
        WHERE id = ?
    """

    const val FIND_BY_BANK_CODE = """
        SELECT id, bank_code, bank_name, layout_configuration, 
               cnab_configuration, validation_rules, is_active,
               created_at, updated_at
        FROM bank_configurations
        WHERE bank_code = ?
    """

    const val FIND_ALL = """
        SELECT id, bank_code, bank_name, layout_configuration, 
               cnab_configuration, validation_rules, is_active,
               created_at, updated_at
        FROM bank_configurations
        ORDER BY bank_name
    """

    const val FIND_ALL_ACTIVE = """
        SELECT id, bank_code, bank_name, layout_configuration, 
               cnab_configuration, validation_rules, is_active,
               created_at, updated_at
        FROM bank_configurations
        WHERE is_active = true
        ORDER BY bank_name
    """

    const val SOFT_DELETE = """
        UPDATE bank_configurations 
        SET is_active = false, updated_at = ?
        WHERE id = ?
    """

    const val EXISTS_BY_ID = """
        SELECT COUNT(*) > 0 
        FROM bank_configurations 
        WHERE id = ?
    """

    const val EXISTS_BY_BANK_CODE = """
        SELECT COUNT(*) > 0 
        FROM bank_configurations 
        WHERE bank_code = ?
    """
}

