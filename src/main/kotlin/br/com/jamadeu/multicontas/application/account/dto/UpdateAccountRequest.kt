package br.com.jamadeu.multicontas.application.account.dto

import br.com.jamadeu.multicontas.domain.account.Account
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class UpdateAccountRequest(
    @field:NotBlank(message = "accountNumber cannot be null")
    val accountNumber: String?,

    @field:NotBlank(message = "branchNumber cannot be null")
    val branchNumber: String?,

    @field:NotNull(message = "branchNumber cannot be null")
    val balance: BigDecimal?,
) {
    fun toAccount(account: Account) =
        Account(
            id = account.id,
            accountNumber = accountNumber ?: throw RuntimeException("accountNumber cannot be null"),
            branchNumber = branchNumber ?: throw RuntimeException("branchNumber cannot be null"),
            balance = balance ?: throw RuntimeException("branchNumber cannot be null"),
            updatedAt = LocalDate.now(),
            createdAt = account.createdAt
        )
}
