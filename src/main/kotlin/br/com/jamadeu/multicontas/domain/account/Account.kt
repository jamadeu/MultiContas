package br.com.jamadeu.multicontas.domain.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Table("accounts")
data class Account(
    @Id
    var id: Long = 0,

    @field:NotBlank(message = "accountNumber cannot be null")
    @Column("account_number")
    val accountNumber: String,

    @field:NotBlank(message = "branchNumber cannot be null")
    @Column("branch_number")
    val branchNumber: String,

    val balance: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "clientId cannot be null")
    @Column("client_id")
    val clientId: Long,

    @Column("created_at")
    val createdAt: LocalDate = LocalDate.now(),

    @Column("updated_at")
    var updatedAt: LocalDate = LocalDate.now()
) {

    fun deposit(amount: BigDecimal) {
        this.balance.add(amount)
        this.updatedAt = LocalDate.now()
    }
}