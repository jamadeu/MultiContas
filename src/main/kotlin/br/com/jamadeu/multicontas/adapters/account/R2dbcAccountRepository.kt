package br.com.jamadeu.multicontas.adapters.account

import br.com.jamadeu.multicontas.domain.account.Account
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface R2dbcAccountRepository : ReactiveCrudRepository<Account, Long> {

    fun existsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Boolean>

    fun findByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Account>

    fun findByClientId(clientId: Long): Flux<Account>

}