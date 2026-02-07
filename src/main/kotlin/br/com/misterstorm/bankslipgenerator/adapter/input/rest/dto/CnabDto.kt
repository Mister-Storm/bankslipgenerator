package br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto

import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import java.time.LocalDate

/**
 * Request DTO for generating CNAB remittance file
 */
data class GenerateRemittanceRequest(
    val bankCode: String,
    val version: CnabVersion,
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Request DTO for processing CNAB return file
 */
data class ProcessReturnRequest(
    val bankCode: String,
    val version: CnabVersion,
    val fileContent: String
)

