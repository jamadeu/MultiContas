package br.com.jamadeu.multicontas.application.exception.handler

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException

@Component
class CustomAttributes: DefaultErrorAttributes() {
    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any> {
        val errorAttributes = super.getErrorAttributes(request, options)
        val exception = getError(request)
        if(exception is ResponseStatusException){
            errorAttributes["message"] = exception.message
            errorAttributes["developerMessage"] = "A ResponseStatusException Happened"
            return errorAttributes
        }
        return errorAttributes
    }
}