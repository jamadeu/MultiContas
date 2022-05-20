package br.com.jamadeu.multicontas.repository

import br.com.jamadeu.multicontas.model.client.Client
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ClientRepository : ReactiveCrudRepository<Client, Long> {
}
