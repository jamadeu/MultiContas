package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.adapters.account.R2dbcAccountRepository
import br.com.jamadeu.multicontas.application.account.dto.CreateAccountRequest
import br.com.jamadeu.multicontas.application.account.dto.UpdateAccountRequest
import br.com.jamadeu.multicontas.domain.account.Account
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.test.web.reactive.server.WebTestClient.BodySpec
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
    //TODO check exception
    @Autowired
    lateinit var accountRepository: R2dbcAccountRepository

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
                    "\tclient_id int NOT NULL,\n" +
                    "\tcreated_at date NOT NULL,\n" +
                    "\tupdated_at date NOT NULL,\n" +
                    "\tCONSTRAINT accounts_pk PRIMARY KEY (id),\n" +
                    "\tCONSTRAINT accounts_un UNIQUE (account_number, branch_number, client_id)\n" +
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
            .isEqualTo<BodySpec<String, *>>("\"/v1/accounts/${account.id}\"")

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(account.balance, savedAccount.balance)
                assertNotNull(savedAccount.createdAt)
                assertNotNull(savedAccount.updatedAt)
            }
    }

    @Test
    fun `create if balance null it must be zero`() {
        val account = account()
        val request = createAccountRequest(account.accountNumber, account.branchNumber, balance = null)

        val result = webTestClient
            .post()
            .uri("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isCreated
            .expectBody(String::class.java)
            .returnResult()

        val responseBody = result.responseBody ?: Assertions.fail("Response body null")
        assertTrue(responseBody.contains("/v1/accounts/"))
        val accountId = responseBody[14].code.toLong()

        accountRepository
            .findById(accountId)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(account.balance, savedAccount.balance)
                assertNotNull(savedAccount.createdAt)
                assertNotNull(savedAccount.updatedAt)
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

    @Test
    fun `findById returns not found when account does not exists`() {
        webTestClient
            .get()
            .uri("/v1/accounts/1")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `findById returns a mono with account when it exists`() {
        accountRepository
            .save(account())
            .doOnNext { account ->
                webTestClient
                    .post()
                    .uri("/v1/accounts/${account.id}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Account::class.java)
                    .isEqualTo<BodySpec<String, *>>(account)
            }
            .subscribe()

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `findByAccountAndBranch returns not found when account does not exists`() {
        webTestClient
            .get()
            .uri("/v1/accounts/account-number/1234/branch-number/5678")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `findByAccountAndBranch returns a mono with account when it exists`() {
        accountRepository
            .save(account())
            .doOnNext { account ->
                webTestClient
                    .post()
                    .uri("/v1/accounts/account-number/${account.accountNumber}/branch-number/${account.branchNumber}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Account::class.java)
                    .isEqualTo<BodySpec<String, *>>(account)
            }
            .subscribe()

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `findByClientId returns not found when account does not exists`() {
        webTestClient
            .get()
            .uri("/v1/accounts/clientId/1")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `findByClientId returns a flux with account when it exists`() {
        accountRepository
            .save(account())
            .doOnNext { account ->
                webTestClient
                    .post()
                    .uri("/v1/accounts/clientId/${account.clientId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Account::class.java)
                    .isEqualTo<BodySpec<String, *>>(account)
            }
            .subscribe()

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(1, count) }
    }

    @Test
    fun `update returns no content when successful`() {
        val account = account()
        val request = updateAccountRequest()
        accountRepository
            .save(account)
            .doOnNext { savedAccount ->
                webTestClient
                    .put()
                    .uri("/v1/accounts/${savedAccount.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isNoContent
            }
            .subscribe()

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(request.accountNumber, savedAccount.accountNumber)
                assertEquals(request.branchNumber, savedAccount.branchNumber)
                assertEquals(request.balance, savedAccount.balance)
                assertEquals(account.createdAt, savedAccount.createdAt)
                assertTrue(savedAccount.updatedAt.isAfter(savedAccount.createdAt))
            }
    }

    @Test
    fun `update returns not found when account does not exists`() {
        val request = updateAccountRequest()

        webTestClient
            .put()
            .uri("/v1/accounts/10")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `update returns bad request when account already exists`() {
        val account = account()
        val accountAlreadyExists = account(
            accountNumber = "9876",
            branchNumber = "9876"
        )
        val request = updateAccountRequest(
            accountNumber = "9876",
            branchNumber = "9876"
        )

        accountRepository.save(accountAlreadyExists).subscribe()
        accountRepository.save(account)
            .doOnNext { savedAccount ->
                webTestClient
                    .put()
                    .uri("/v1/accounts/${savedAccount.id}")
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
            .doOnNext { count -> assertEquals(2, count) }

        accountRepository
            .findById(accountAlreadyExists.id)
            .doOnNext { foundedAccount ->
                assertNotNull(foundedAccount)
                assertEquals(accountAlreadyExists.accountNumber, foundedAccount.accountNumber)
                assertEquals(accountAlreadyExists.branchNumber, foundedAccount.branchNumber)
                assertEquals(accountAlreadyExists.balance, foundedAccount.balance)
                assertEquals(accountAlreadyExists.createdAt, foundedAccount.updatedAt)
                assertNotNull(foundedAccount.createdAt)
                assertNotNull(foundedAccount.updatedAt)
            }

        accountRepository
            .findById(account.id)
            .doOnNext { foundedAccount ->
                assertNotNull(foundedAccount)
                assertEquals(account.accountNumber, foundedAccount.accountNumber)
                assertEquals(account.branchNumber, foundedAccount.branchNumber)
                assertEquals(account.balance, foundedAccount.balance)
                assertEquals(account.createdAt, foundedAccount.updatedAt)
                assertNotNull(foundedAccount.createdAt)
                assertNotNull(foundedAccount.updatedAt)
            }
    }

    @Test
    fun `delete removes account when successful`() {
        accountRepository
            .save(account())
            .doOnNext { savedAccount ->
                webTestClient
                    .delete()
                    .uri("/v1/accounts/${savedAccount.id}")
                    .exchange()
                    .expectStatus().isNoContent
            }
            .subscribe()

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `delete returns no content when account does not exists`() {
        webTestClient
            .delete()
            .uri("/v1/accounts/1")
            .exchange()
            .expectStatus().isNoContent

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `deposit returns no content when successful`() {
        val account = account()
        val amount = BigDecimal.valueOf(1000)
        accountRepository
            .save(account)
            .doOnNext { savedAccount ->
                webTestClient
                    .put()
                    .uri("/v1/accounts/deposit/account/${savedAccount.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(amount))
                    .exchange()
                    .expectStatus().isNoContent
            }
            .subscribe()

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(amount, savedAccount.balance)
                assertEquals(account.createdAt, savedAccount.createdAt)
                assertTrue(savedAccount.updatedAt.isAfter(savedAccount.createdAt))
            }
    }

    @Test
    fun `deposit returns not found when account does not exists`() {
        webTestClient
            .put()
            .uri("/v1/accounts/deposit/account/10")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(BigDecimal.ONE))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

        accountRepository
            .findAll()
            .count()
            .doOnNext { count -> assertEquals(0, count) }
    }

    @Test
    fun `deposit returns bad request when amount is null`() {
        val account = account()
        accountRepository
            .save(account)
            .doOnNext { savedAccount ->
                webTestClient
                    .put()
                    .uri("/v1/accounts/deposit/account/${savedAccount.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
            }
            .subscribe()

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(account.balance, savedAccount.balance)
                assertEquals(account.createdAt, savedAccount.createdAt)
                assertEquals(account.updatedAt, savedAccount.updatedAt)
            }
    }

    @Test
    fun `deposit returns bad request when amount is negative`() {
        val account = account()
        accountRepository
            .save(account)
            .doOnNext { savedAccount ->
                webTestClient
                    .put()
                    .uri("/v1/accounts/deposit/account/${savedAccount.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(-1))
                    .exchange()
                    .expectStatus().isBadRequest
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
            }
            .subscribe()

        accountRepository
            .findById(account.id)
            .doOnNext { savedAccount ->
                assertNotNull(savedAccount)
                assertEquals(account.accountNumber, savedAccount.accountNumber)
                assertEquals(account.branchNumber, savedAccount.branchNumber)
                assertEquals(account.balance, savedAccount.balance)
                assertEquals(account.createdAt, savedAccount.createdAt)
                assertEquals(account.updatedAt, savedAccount.updatedAt)
            }
    }

    private fun account(
        id: Long = 0,
        accountNumber: String = "1234",
        branchNumber: String = "5678",
        balance: BigDecimal = BigDecimal.ZERO,
        clientId: Long = 1L,
        createdAt: LocalDate = LocalDate.now(),
        updatedAt: LocalDate = LocalDate.now()
    ) = Account(id, accountNumber, branchNumber, balance, clientId, createdAt, updatedAt)

    private fun createAccountRequest(
        accountNumber: String? = "1234",
        branchNumber: String? = "5678",
        balance: BigDecimal? = BigDecimal.valueOf(1000),
        clientId: Long = 1L
    ) = CreateAccountRequest(accountNumber, branchNumber, balance, clientId)

    private fun updateAccountRequest(
        accountNumber: String? = "5678",
        branchNumber: String? = "1234",
        balance: BigDecimal? = BigDecimal.valueOf(5000),
        clientId: Long = 1L
    ) = UpdateAccountRequest(accountNumber, branchNumber, balance, clientId)
}