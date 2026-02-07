package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFileType
import java.util.*

/**
 * Repository port for CNAB file persistence
 */
interface CnabFileRepository {
    suspend fun save(cnabFile: CnabFile): Either<DomainError, CnabFile>
    suspend fun findById(id: UUID): Either<DomainError, CnabFile>
    suspend fun findByBankCode(bankCode: String, page: Int, size: Int): Either<DomainError, List<CnabFile>>
    suspend fun findByFileType(fileType: CnabFileType, page: Int, size: Int): Either<DomainError, List<CnabFile>>
    suspend fun update(cnabFile: CnabFile): Either<DomainError, CnabFile>
}

