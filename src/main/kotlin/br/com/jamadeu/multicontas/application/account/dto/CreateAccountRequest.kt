package br.com.jamadeu.multicontas.application.account.dto

import br.com.jamadeu.multicontas.domain.account.Account
import java.math.BigDecimal
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateAccountRequest(
    @field:NotBlank(message = "accountNumber cannot be null")
    val accountNumber: String?,

    @field:NotBlank(message = "branchNumber cannot be null")
    val branchNumber: String?,

    val balance: BigDecimal?,

    @field:NotNull(message = "clientId cannot be null")
    val clientId: Long?
) {
    fun toAccount() = with(balance) {
        if (this == null) {
            Account(
                accountNumber = accountNumber ?: throw RuntimeException("Account number cannot be null"),
                branchNumber = branchNumber ?: throw RuntimeException("BranchNumber cannot be null"),
                clientId = clientId ?: throw RuntimeException("ClientId cannot be null")
            )
        } else {
            Account(
                accountNumber = accountNumber ?: throw RuntimeException("Account number cannot be null"),
                branchNumber = branchNumber ?: throw RuntimeException("Account branchNumber cannot be null"),
                clientId = clientId ?: throw RuntimeException("ClientId cannot be null"),
                balance = this
            )
        }
    }
}
