package br.com.jamadeu.multicontas.controller

import br.com.jamadeu.multicontas.model.client.Client
import br.com.jamadeu.multicontas.model.client.dto.CreateClientRequest
import br.com.jamadeu.multicontas.model.client.dto.UpdateClientRequest
import br.com.jamadeu.multicontas.service.ClientService
import org.hibernate.validator.constraints.br.CPF
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/clients")
@Validated
class ClientController(
    val clientService: ClientService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateClientRequest, uriBuilder: UriComponentsBuilder): Mono<URI> =
        clientService
            .create(request.toClient())
            .flatMap { client ->
                uriBuilder.path("/v1/clients/{id}").buildAndExpand(client.id).toUri().toMono()
            }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable("id") id: Long): Mono<Client> =
        clientService.findById(id)


    @GetMapping("/cpf/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    fun findByCpf(@PathVariable("cpf") @Valid @CPF @NotBlank cpf: String): Mono<Client> =
        clientService.findByCpf(cpf)


    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable("id") id: Long, @Valid @RequestBody request: UpdateClientRequest): Mono<Void> =
        clientService.update(id, request)


    @ExceptionHandler(ConstraintViolationException::class)
    private fun constraintViolationExceptionHandler(exception: ConstraintViolationException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message)
    }
}