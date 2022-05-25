package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.model.account.Account
import br.com.jamadeu.multicontas.model.account.dto.CreateAccountRequest
import br.com.jamadeu.multicontas.repository.AccountRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.NullSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
internal class AccountControllerTest {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var database: DatabaseClient

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup() {
        val statements = listOf(//
            "DROP TABLE IF EXISTS public.accounts;",
            "CREATE TABLE public.accounts (\n" +
                    "\tid serial NOT NULL,\n" +
                    "\taccount_number varchar NOT NULL,\n" +
                    "\tbranch_number varchar NOT NULL,\n" +
                    "\tbalance decimal NOT NULL,\n" +
                    "\tcreated_at date NOT NULL,\n" +
                    "\tupdated_at date NOT NULL,\n" +
                    "\tCONSTRAINT clients_pk PRIMARY KEY (id),\n" +
                    "\tCONSTRAINT clients_un UNIQUE (account_number, branch_number)\n" +
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
    fun `create returns a uri with the created account id when successful`() {
        val account = account()
        val request = createAccountRequest(account.accountNumber, account.branchNumber, account.balance)

        webTestClient
            .post()
            .uri("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isCreated
            .expectBody(String::class.java)
            .isEqualTo<WebTestClient.BodySpec<String, *>>("\"/v1/accounts/${account.id}\"")
            .returnResult()

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                Assertions.assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(account.balance, savedAccount.balance)
                Assertions.assertNotNull(savedAccount.createdAt)
                Assertions.assertNotNull(savedAccount.updatedAt)
            }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    fun `create returns bad request when accountNumber is null, empty`(accountNumber: String?) {
        val request = createAccountRequest(accountNumber)

        webTestClient
            .post()
            .uri("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .hasElements()
            .doOnNext { hasElements -> Assertions.assertFalse(hasElements) }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    fun `create returns bad request when branchNumber is null, empty`(branchNumber: String?) {
        val request = createAccountRequest(branchNumber)

        webTestClient
            .post()
            .uri("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .hasElements()
            .doOnNext { hasElements -> Assertions.assertFalse(hasElements) }
    }

    @Test
    fun `create returns bad request when account already exists`() {
        val account = account()
        val request = createAccountRequest(accountNumber = account.accountNumber, branchNumber = account.branchNumber)

        accountRepository
            .save(account)
            .doOnNext {
                webTestClient
                    .post()
                    .uri("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
            }
            .subscribe()

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    private fun account(
        id: Long = 1L,
        accountNumber: String = "1234",
        branchNumber: String = "5678",
        balance: BigDecimal = BigDecimal.valueOf(1000),
        createdAt: LocalDate = LocalDate.now(),
        updatedAt: LocalDate = LocalDate.now()
    ) = Account(id, accountNumber, branchNumber, balance, createdAt, updatedAt)

    private fun createAccountRequest(
        accountNumber: String? = "1234",
        branchNumber: String? = "5678",
        balance: BigDecimal = BigDecimal.valueOf(1000),
    ) = CreateAccountRequest(accountNumber, branchNumber, balance)
}