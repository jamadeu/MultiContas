package br.com.jamadeu.multicontas.model.client.dto

import br.com.jamadeu.multicontas.model.client.Client
import org.hibernate.validator.constraints.br.CPF
import java.util.UUID
import javax.validation.constraints.NotEmpty

data class CreateClientRequest(
    @field:NotEmpty(message = "Name cannot be empty")
    val name: String?,

    @field:NotEmpty
    @field:CPF
    val cpf: String?
) {

    fun toClient(): Client =
        Client(
            id = UUID.randomUUID(),
            name ?: throw RuntimeException("Name cannot be null"),
            cpf ?: throw RuntimeException("CPF cannot be null")
        )
}
