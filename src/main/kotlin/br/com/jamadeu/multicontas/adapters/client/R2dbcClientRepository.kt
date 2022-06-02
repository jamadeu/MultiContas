package br.com.jamadeu.multicontas.adapters.client

import br.com.jamadeu.multicontas.domain.client.Client
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface R2dbcClientRepository : ReactiveCrudRepository<Client, Long> {

    fun findByCpf(cpf: String): Mono<Client>
}
