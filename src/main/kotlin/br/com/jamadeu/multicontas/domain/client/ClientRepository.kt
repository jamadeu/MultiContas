package br.com.jamadeu.multicontas.domain.client

import reactor.core.publisher.Mono

interface ClientRepository {

    fun findByCpf(cpf: String): Mono<Client>

    fun save(client: Client): Mono<Client>

    fun findById(id: Long): Mono<Client>

    fun deleteById(id: Long): Mono<Void>
}