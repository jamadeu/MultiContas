package br.com.jamadeu.multicontas.adapters.account

import br.com.jamadeu.multicontas.domain.account.Account
import br.com.jamadeu.multicontas.domain.account.AccountRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AccountRepositoryAdapter(private val repository: R2dbcAccountRepository) : AccountRepository {
    override fun save(account: Account): Mono<Account> = repository.save(account)

    override fun findById(id: Long): Mono<Account> = repository.findById(id)

    override fun existsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Boolean> =
        repository.existsByAccountNumberAndBranchNumber(accountNumber, branchNumber)

    override fun deleteById(id: Long): Mono<Void> = repository.deleteById(id)

    override fun findByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Account> =
        repository.findByAccountNumberAndBranchNumber(accountNumber, branchNumber)
}