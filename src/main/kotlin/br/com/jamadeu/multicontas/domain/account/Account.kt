package br.com.jamadeu.multicontas.domain.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank

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

    @Column("created_at")
    val createdAt: LocalDate = LocalDate.now(),

    @Column("updated_at")
    val updatedAt: LocalDate = LocalDate.now()
) {
}