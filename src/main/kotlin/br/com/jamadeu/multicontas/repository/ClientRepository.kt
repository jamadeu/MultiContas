package br.com.jamadeu.multicontas.repository

import br.com.jamadeu.multicontas.model.client.Client
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import java.util.UUID

interface ClientRepository : ReactiveMongoRepository<Client, UUID> {
}
