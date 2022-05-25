package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.model.client.dto.CreateClientRequest
import br.com.jamadeu.multicontas.model.client.dto.UpdateClientRequest
import br.com.jamadeu.multicontas.repository.ClientRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
internal class ClientControllerTest {
    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var database: DatabaseClient

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup() {
        val statements = listOf(//
            "DROP TABLE IF EXISTS public.clients;",
            "CREATE TABLE public.clients (\n" +
                    "\tid serial NOT NULL,\n" +
                    "\tname varchar NOT NULL,\n" +
                    "\tcpf varchar NOT NULL,\n" +
                    "\tcreated_at date NOT NULL,\n" +
                    "\tupdated_at date NOT NULL,\n" +
                    "\tCONSTRAINT clients_pk PRIMARY KEY (id),\n" +
                    "\tCONSTRAINT clients_un UNIQUE (cpf)\n" +
                    ");"
        )

        statements.forEach {
            database.sql(it)
                .fetch()
                .rowsUpdated()
                .`as`(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete()
        }
    }

    @Test
    fun `create returns a uri with the created client when successful`() {
        val client = client()
        val request = createClientRequest(client.name, client.cpf)

        val response = webTestClient
            .post()
            .uri("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isCreated
            .expectBody(String::class.java)
            .isEqualTo<BodySpec<String, *>>("\"/v1/clients/${client.id}\"")
            .returnResult()

        clientRepository
            .findById(client.id)
            .doOnNext { savedClient ->
                assertNotNull(savedClient)
                assertEquals(client.name, savedClient.name)
                assertEquals(client.cpf, savedClient.cpf)
                assertNotNull(savedClient.createdAt)
                assertNotNull(savedClient.updatedAt)
            }
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

        clientRepository
            .findAll()
            .hasElements()
            .doOnNext { hasElements -> assertFalse(hasElements) }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    fun `create returns bad request when name is null or empty`(name: String?) {
        val request = createClientRequest(name = name)

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

        clientRepository
            .findAll()
            .hasElements()
            .doOnNext { hasElements -> assertFalse(hasElements) }
    }

    @Test
    fun `create returns bad request when client already exists`() {
        val request = createClientRequest()
        clientRepository.save(request.toClient())
            .doOnNext {
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
            .subscribe()

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `findById returns a mono with client when it exists`() {
        clientRepository
            .save(client())
            .doOnNext {
                webTestClient
                    .get()
                    .uri("/v1/clients/${it.id}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Client::class.java)
                    .isEqualTo<BodySpec<Client, *>>(it)
            }
            .subscribe()

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `findById returns not found when client does not exists`() {
        webTestClient
            .get()
            .uri("/v1/clients/1")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `findByCpf returns a mono with client when it exists`() {
        clientRepository
            .save(client())
            .doOnNext {
                webTestClient
                    .get()
                    .uri("/v1/clients/cpf/${it.cpf}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Client::class.java)
                    .isEqualTo<BodySpec<Client, *>>(it)
            }
            .subscribe()

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `findByCpf returns not found when client does not exists`() {
        webTestClient
            .get()
            .uri("/v1/clients/cpf/844.781.250-23")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = ["111.111.111-11"])
    fun `findByCpf returns bad request when cpf is null, empty or invalid`(cpf: String?) {
        webTestClient
            .get()
            .uri("/v1/clients/cpf/${cpf}")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `update returns a client when successful`() {
        val client = client()
        val request = updateClientRequest()
        clientRepository
            .save(client)
            .doOnNext {
                webTestClient
                    .put()
                    .uri("/v1/clients/${client.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isNoContent
            }
            .subscribe()

        clientRepository
            .findById(client.id)
            .doOnNext { foundedClient ->
                assertEquals(request.name, foundedClient.name)
                assertEquals(request.cpf, foundedClient.cpf)
                assertTrue(foundedClient.updatedAt.isAfter(client.updatedAt))
            }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = ["111.111.111-11"])
    fun `update returns bad request when cpf is null, empty or invalid`(cpf: String?) {
        val request = updateClientRequest(cpf = cpf)

        webTestClient
            .put()
            .uri("/v1/clients/1")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    fun `update returns bad request when name is null or empty`(name: String?) {
        val request = updateClientRequest(name = name)

        webTestClient
            .put()
            .uri("/v1/clients/1")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `delete removes client when successful`() {
        val client = client()
        clientRepository
            .save(client)
            .doOnNext {
                webTestClient
                    .delete()
                    .uri("/v1/clients/${it.id}")
                    .exchange()
                    .expectStatus().isNoContent
            }
            .subscribe()

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `delete returns status code 204 when client does not exists`() {
        webTestClient
            .delete()
            .uri("/v1/clients/1")
            .exchange()
            .expectStatus().isNoContent

        clientRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    private fun updateClientRequest(
        name: String? = "UpdatedClient",
        cpf: String? = "891.097.570-90"
    ) = UpdateClientRequest(name, cpf)

    private fun createClientRequest(
        name: String? = "Client",
        cpf: String? = "844.781.250-23"
    ) = CreateClientRequest(name, cpf)

    private fun client(
        id: Long = 1L,
        name: String = "Client",
        cpf: String = "844.781.250-23",
        createdAt: LocalDate = LocalDate.now(),
        updatedAt: LocalDate = LocalDate.now()
    ) = Client(id, name, cpf, createdAt, updatedAt)
}