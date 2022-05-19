package br.com.jamadeu.multicontas.service

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.repository.ClientRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ClientService(
    val clientRepository: ClientRepository
) {

    fun create(client: Client): Mono<Client> =
        clientRepository.save(client)
}
