package br.com.jamadeu.multicontas.repository

import br.com.jamadeu.multicontas.model.account.Account
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface AccountRepository : ReactiveCrudRepository<Account, Long> {

    fun existsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Boolean>

    fun findByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Account>

}