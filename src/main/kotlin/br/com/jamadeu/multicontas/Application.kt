package br.com.jamadeu.multicontas

import kotlinx.coroutines.debug.CoroutinesBlockHoundIntegration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.blockhound.BlockHound

@SpringBootApplication
class Application

fun main(args: Array<String>) {
	BlockHound
		.builder()
		.allowBlockingCallsInside("java.util.UUID", "randomUUID")
		.with(CoroutinesBlockHoundIntegration())
		.install()
	runApplication<Application>(*args)
}
