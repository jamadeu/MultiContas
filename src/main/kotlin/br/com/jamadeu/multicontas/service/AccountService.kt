package br.com.jamadeu.multicontas.service

import br.com.jamadeu.multicontas.model.account.Account
import br.com.jamadeu.multicontas.repository.AccountRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class AccountService(
    val accountRepository: AccountRepository
) {
    fun create(account: Account): Mono<Account> =
        accountRepository
            .existsByAccountNumberAndBranchNumber(account.accountNumber, account.branchNumber)
            .map { accountExists ->
                if (accountExists)
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already exists")
            }.flatMap { accountRepository.save(account) }

    fun findById(id: Long): Mono<Account> =
        accountRepository
            .findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))

    fun findByAccountAndBranch(accountNumber: String, branchNumber: String): Mono<Account> =
        accountRepository
            .findByAccountNumberAndBranchNumber(accountNumber = accountNumber, branchNumber = branchNumber)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
}