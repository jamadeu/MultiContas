package br.com.jamadeu.multicontas.domain.account

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AccountRepository {

    fun save(account: Account): Mono<Account>

    fun findById(id: Long): Mono<Account>

    fun existsByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Boolean>

    fun deleteById(id: Long): Mono<Void>

    fun findByAccountNumberAndBranchNumber(accountNumber: String, branchNumber: String): Mono<Account>

    fun findByClientId(clientId: Long): Flux<Account>
}