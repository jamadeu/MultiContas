package br.com.jamadeu.multicontas.domain.account

import br.com.jamadeu.multicontas.application.account.dto.UpdateAccountRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class AccountService(
    private val accountRepository: AccountRepository
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
        findById(id)
            .map { account ->
                if (account.accountNumber != request.accountNumber ||
                    account.branchNumber != request.branchNumber
                ) {
                    checkIdAccountExistsByAccountNumberAndBranchNumber(
                        accountNumber = request.accountNumber
                            ?: throw RuntimeException("AccountNumber cannot be null"),
                        branchNumber = request.branchNumber
                            ?: throw RuntimeException("branchNumber cannot be null")
                    )
                }
                return@map account
            }
            .flatMap { account -> accountRepository.save(request.toAccount(account)) }
            .then()

    fun delete(id: Long): Mono<Void> = accountRepository.deleteById(id)

    private fun checkIdAccountExistsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String) {
        accountRepository
            .existsByAccountNumberAndBranchNumber(accountNumber, branchNumber)
            .map { accountExists ->
                if (accountExists)
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already exists")
            }
    }

}