package br.com.jamadeu.multicontas.adapters.client

import br.com.jamadeu.multicontas.domain.client.Client
import br.com.jamadeu.multicontas.domain.client.ClientRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ClientRepositoryAdapter(
    private val repository: R2dbcClientRepository
): ClientRepository
{
    override fun findByCpf(cpf: String): Mono<Client> = repository.findByCpf(cpf)

    override fun save(client: Client): Mono<Client> = repository.save(client)

    override fun findById(id: Long): Mono<Client> = repository.findById(id)

    override fun deleteById(id: Long): Mono<Void> = repository.deleteById(id)
}