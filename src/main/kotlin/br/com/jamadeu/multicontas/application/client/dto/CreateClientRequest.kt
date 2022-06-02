package br.com.jamadeu.multicontas.application.client.dto


import br.com.jamadeu.multicontas.domain.client.Client
import org.hibernate.validator.constraints.br.CPF
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
            name = name ?: throw RuntimeException("Name cannot be null"),
            cpf = cpf ?: throw RuntimeException("CPF cannot be null")
        )
}
