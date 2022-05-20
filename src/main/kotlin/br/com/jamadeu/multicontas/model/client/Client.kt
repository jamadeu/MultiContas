package br.com.jamadeu.multicontas.model.client

import org.hibernate.validator.constraints.br.CPF
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import javax.validation.constraints.NotEmpty

@Table
data class Client(
    @Id
    var id: Long = 0,

    @field:NotEmpty(message = "Name cannot be empty")
    val name: String,

    @field:NotEmpty
    @field:CPF
    val cpf: String,

    @Column("created_at")
    val createdAt: LocalDate = LocalDate.now(),

    @Column("updated_at")
    val updatedAt: LocalDate = LocalDate.now()
) {

}