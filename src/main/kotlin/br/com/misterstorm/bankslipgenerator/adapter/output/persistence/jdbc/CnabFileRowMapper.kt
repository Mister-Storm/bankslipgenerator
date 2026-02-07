package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.CnabFileEntity
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*

@Component
class CnabFileRowMapper : RowMapper<CnabFileEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): CnabFileEntity {
        return CnabFileEntity(
            id = UUID.fromString(rs.getString("id")),
            bankCode = rs.getString("bank_code"),
            fileType = CnabFileType.valueOf(rs.getString("file_type")),
            version = CnabVersion.valueOf(rs.getString("version")),
            fileName = rs.getString("file_name"),
            fileContent = rs.getString("file_content"),
            fileUrl = rs.getString("file_url"),
            status = CnabFileStatus.valueOf(rs.getString("status")),
            totalRecords = rs.getInt("total_records"),
            processedRecords = rs.getInt("processed_records"),
            errorRecords = rs.getInt("error_records"),
            errors = rs.getString("errors"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            processedAt = rs.getTimestamp("processed_at")?.toLocalDateTime()
        )
    }
}

