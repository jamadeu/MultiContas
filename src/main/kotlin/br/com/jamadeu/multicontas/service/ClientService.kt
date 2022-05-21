package br.com.jamadeu.multicontas.service

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.repository.ClientRepository
import org.hibernate.validator.constraints.br.CPF
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Service
class ClientService(
    val clientRepository: ClientRepository
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

}
