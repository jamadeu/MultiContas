package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.exception.handler.CustomAttributes
import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.model.client.dto.CreateClientRequest
import br.com.jamadeu.multicontas.repository.ClientRepository
import br.com.jamadeu.multicontas.service.ClientService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito
import org.mockito.BDDMockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.WebProperties.Resources
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest
@Import(ClientService::class, Resources::class, CustomAttributes::class)
internal class ClientControllerTest {

    @MockBean
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `create returns a client when successful`() {
        val client = client()
        val request = createClientRequest(client.name, client.cpf)
        `when`(clientRepository.findByCpf(request.cpf!!))
            .thenReturn(Mono.empty())
        `when`(clientRepository.save(any(Client::class.java)))
            .thenReturn(Mono.just(client))

        webTestClient
            .post()
            .uri("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isCreated
            .expectBody(Client::class.java)
            .isEqualTo<BodySpec<Client, *>>(client)
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = ["111.111.111-11"])
    fun `create returns bad request when cpf is null, empty or invalid`(cpf: String?) {
        val request = createClientRequest(cpf = cpf)

        webTestClient
            .post()
            .uri("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

    @Test
    fun `create returns bad request when client already exists`() {
        val client = client()
        val request = createClientRequest(client.name, client.cpf)
        `when`(clientRepository.findByCpf(request.cpf!!))
            .thenReturn(Mono.just(client))

        webTestClient
            .post()
            .uri("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

    @Test
    fun `findById returns a mono with client when it exists`(){
        val client = client()
        `when`(clientRepository.findById(client.id)).thenReturn(Mono.just(client))

        webTestClient
            .get()
            .uri("/v1/clients/${client.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(Client::class.java)
            .isEqualTo<BodySpec<Client, *>>(client)
    }

    @Test
    fun `findById returns not found when client does not exists`(){
        `when`(clientRepository.findById(anyLong())).thenReturn(Mono.empty())

        webTestClient
            .get()
            .uri("/v1/clients/1")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

    private fun createClientRequest(
        name: String? = "Client",
        cpf: String? = "844.781.250-23"
    ) = CreateClientRequest(name, cpf)

    private fun client(
        id: Long = 1L,
        name: String = "Client",
        cpf: String = "844.781.250-23"
    ) = Client(id, name, cpf)
}