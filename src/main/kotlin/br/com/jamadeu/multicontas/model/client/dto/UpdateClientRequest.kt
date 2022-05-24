package br.com.jamadeu.multicontas.model.client.dto

import br.com.jamadeu.multicontas.model.client.Client
import org.hibernate.validator.constraints.br.CPF
import java.time.LocalDate
import javax.validation.constraints.NotEmpty

data class UpdateClientRequest(

    @field:NotEmpty(message = "Name cannot be empty")
    val name: String?,

    @field:NotEmpty
    @field:CPF
    val cpf: String?
) {
    fun toClient(client: Client): Client =
        Client(
            id = client.id,
            name = this.name ?: throw RuntimeException("Name cannot be null"),
            cpf = this.cpf ?: throw RuntimeException("CPF cannot be null"),
            createdAt = client.createdAt,
            updatedAt = LocalDate.now()
        )
}

