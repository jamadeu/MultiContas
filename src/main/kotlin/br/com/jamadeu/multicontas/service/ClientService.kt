package br.com.jamadeu.multicontas.service

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.repository.ClientRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class ClientService(
    val clientRepository: ClientRepository
) {

    fun create(client: Client): Mono<Client> {
        clientRepository.findByCpf(client.cpf)
            .hasElement()
            .doOnNext { hasElement ->
                if (hasElement) {Mono.error<ResponseStatusException>(
                    ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Client already exists"
                    )
                )}
            }
        return clientRepository.save(client)
    }
//        Mono.just(request)
//            .doOnNext { client ->
//                findByCpf(client.cpf)
//                    .hasElement()
//                    .doOnNext { hasElement ->
//                        if (hasElement) Mono.error<ResponseStatusException>(
//                            ResponseStatusException(
//                                HttpStatus.BAD_REQUEST,
//                                "Client already exists"
//                            )
//                        )
//                    }
//            }
//            .flatMap { client -> clientRepository.save(client) }
}
