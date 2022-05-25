package br.com.jamadeu.multicontas.service

import br.com.jamadeu.multicontas.model.account.Account
import br.com.jamadeu.multicontas.model.account.dto.UpdateAccountRequest
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
        Mono.just(account)
            .doFirst {
                checkIdAccountExistsByAccountNumberAndBranchNumber(
                    accountNumber = account.accountNumber,
                    branchNumber = account.branchNumber
                )
            }
            .flatMap { accountToSave -> accountRepository.save(accountToSave) }

    fun findById(id: Long): Mono<Account> =
        accountRepository
            .findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))

    fun findByAccountAndBranch(accountNumber: String, branchNumber: String): Mono<Account> =
        accountRepository
            .findByAccountNumberAndBranchNumber(accountNumber = accountNumber, branchNumber = branchNumber)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))

    fun update(id: Long, request: UpdateAccountRequest): Mono<Void> =
        Mono.just(request)
            .doOnNext { updateAccountRequest ->
                findById(id)
                    .doOnNext { account ->
                        if (account.accountNumber != updateAccountRequest.accountNumber ||
                            account.branchNumber != updateAccountRequest.branchNumber
                        ) {
                            checkIdAccountExistsByAccountNumberAndBranchNumber(
                                accountNumber = updateAccountRequest.accountNumber
                                    ?: throw RuntimeException("AccountNumber cannot be null"),
                                branchNumber = updateAccountRequest.branchNumber
                                    ?: throw RuntimeException("branchNumber cannot be null")
                            )
                        }
                    }
                    .flatMap { account -> accountRepository.save(request.toAccount(account)) }
            }
            .then()

    private fun checkIdAccountExistsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String) {
        accountRepository
            .existsByAccountNumberAndBranchNumber(accountNumber, branchNumber)
            .map { accountExists ->
                if (accountExists)
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already exists")
            }
    }
}