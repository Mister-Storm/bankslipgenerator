package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion

/**
 * Service port for CNAB file generation and processing
 */
interface CnabService {
    suspend fun generateRemittanceFile(
        bankSlips: List<BankSlip>,
        bankCode: String,
        version: CnabVersion
    ): Either<DomainError, CnabFile>

    suspend fun processReturnFile(
        fileContent: String,
        bankCode: String,
        version: CnabVersion
    ): Either<DomainError, CnabFile>
}
