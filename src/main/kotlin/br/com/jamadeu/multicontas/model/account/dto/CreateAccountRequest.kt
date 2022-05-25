package br.com.jamadeu.multicontas.model.account.dto

import br.com.jamadeu.multicontas.model.account.Account
import java.math.BigDecimal
import javax.validation.constraints.NotBlank

data class CreateAccountRequest(
    @field:NotBlank(message = "accountNumber cannot be null")
    val accountNumber: String?,

    @field:NotBlank(message = "branchNumber cannot be null")
    val branchNumber: String?,

    val balance: BigDecimal?,
) {
    fun toAccount() =
        Account(
            accountNumber = accountNumber ?: throw RuntimeException("accountNumber cannot be null"),
            branchNumber = branchNumber ?: throw RuntimeException("branchNumber cannot be null"),
        )
}
