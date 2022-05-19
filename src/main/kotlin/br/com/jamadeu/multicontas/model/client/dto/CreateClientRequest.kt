package br.com.jamadeu.multicontas.model.client.dto

import br.com.jamadeu.multicontas.model.client.Client
import com.fasterxml.jackson.core.util.ByteArrayBuilder
import org.hibernate.validator.constraints.br.CPF
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.NotEmpty
import kotlin.random.Random

data class CreateClientRequest(
    @field:NotEmpty(message = "Name cannot be empty")
    val name: String,

    @field:NotEmpty
    @field:CPF
    val cpf: String
) {

    fun toClient(): Client =
        Client(
            id = UUID.randomUUID(),
            name,
            cpf
        )
}
