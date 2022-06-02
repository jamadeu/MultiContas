package br.com.jamadeu.multicontas.domain.client

import br.com.jamadeu.multicontas.application.client.dto.UpdateClientRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class ClientService(
    private val clientRepository: ClientRepository
) {

    fun create(request: Client): Mono<Client> =
        clientRepository.findByCpf(request.cpf)
            .hasElement()
            .map { clientExists ->
                if (clientExists)
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Client already exists")
            }
            .flatMap { clientRepository.save(request) }

    fun findById(id: Long): Mono<Client> =
        clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found")))

    fun findByCpf(cpf: String): Mono<Client> =
        clientRepository.findByCpf(cpf)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found")))

    fun update(id: Long, request: UpdateClientRequest): Mono<Void> =
        findById(id)
            .flatMap { client -> request.toClient(client).toMono() }
            .flatMap { updatedClient -> clientRepository.save(updatedClient) }
            .then()

    fun delete(id: Long): Mono<Void> =
        clientRepository.deleteById(id)
}
