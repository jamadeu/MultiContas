package br.com.jamadeu.multicontas.repository

import br.com.jamadeu.multicontas.model.client.Client
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono


interface ClientRepository : ReactiveCrudRepository<Client, Long> {

    fun findByCpf(cpf: String): Mono<Client>
}
