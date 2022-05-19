package br.com.jamadeu.multicontas.model.client

import org.hibernate.validator.constraints.br.CPF
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Document
data class Client(
    @Id
    @field:NotNull(message = "Id cannot be null")
    val id: UUID,

    @field:NotEmpty(message = "Name cannot be empty")
    val name: String,

    @field:NotEmpty
    @field:CPF
    val cpf: String
) {
}