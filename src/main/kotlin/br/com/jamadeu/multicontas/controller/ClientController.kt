package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.model.client.dto.CreateClientRequest
import br.com.jamadeu.multicontas.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import javax.validation.Valid

@RestController
@RequestMapping("/v1/clients")
class ClientController(
    val clientService: ClientService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateClientRequest, uriBuilder: UriComponentsBuilder): Mono<URI> {
        return clientService.create(request.toClient())
            .flatMap { client ->
                uriBuilder.path("/v1/clients/{id}").buildAndExpand(client.id).toUri().toMono()
            }
//        return clientService.create(request.toClient())
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable("id") id: Long): Mono<Client> {
        return clientService.findById(id)
    }
}