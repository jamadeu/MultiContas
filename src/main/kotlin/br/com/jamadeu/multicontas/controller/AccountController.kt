package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.model.account.Account
import br.com.jamadeu.multicontas.model.account.dto.CreateAccountRequest
import br.com.jamadeu.multicontas.model.account.dto.UpdateAccountRequest
import br.com.jamadeu.multicontas.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import javax.validation.Valid

@RestController
@RequestMapping("/v1/accounts")
class AccountController(
    val accountService: AccountService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateAccountRequest, uriBuilder: UriComponentsBuilder): Mono<URI> =
        accountService
            .create(request.toAccount())
            .flatMap { account ->
                uriBuilder.path("/v1/accounts/{id}").buildAndExpand(account.id).toUri().toMono()
            }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable("id") id: Long): Mono<Account> =
        accountService.findById(id)

    @GetMapping("/account-number/{accountNumber}/branch-number/{branchNumber}")
    fun findByAccountAndBranch(
        @PathVariable("accountNumber") accountNumber: String,
        @PathVariable("branchNumber") branchNumber: String
    ): Mono<Account> = accountService.findByAccountAndBranch(accountNumber, branchNumber)

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable("id") id: Long, @Valid @RequestBody request: UpdateAccountRequest): Mono<Void> =
        accountService.update(id, request)
}