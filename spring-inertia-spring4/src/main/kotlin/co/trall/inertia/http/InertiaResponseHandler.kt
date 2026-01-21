package co.trall.inertia.http

import co.trall.inertia.InertiaResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.ModelAndView
import tools.jackson.databind.ObjectMapper

/**
 * Handles InertiaResponse return values from controller methods.
 *
 * This handler converts InertiaResponse objects to the appropriate format
 * based on the request type (HTML for initial loads, JSON for Inertia requests).
 */
class InertiaResponseHandler(
    private val objectMapper: ObjectMapper
) : HandlerMethodReturnValueHandler {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return InertiaResponse::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        if (returnValue == null) {
            mavContainer.isRequestHandled = true
            return
        }

        val inertiaResponse = returnValue as InertiaResponse
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("No HttpServletRequest available")

        when (val result = inertiaResponse.toView(request)) {
            is ModelAndView -> {
                mavContainer.view = result.view
                mavContainer.model.putAll(result.model)
            }
            is org.springframework.http.ResponseEntity<*> -> {
                mavContainer.isRequestHandled = true
                val response = webRequest.getNativeResponse(jakarta.servlet.http.HttpServletResponse::class.java)
                    ?: throw IllegalStateException("No HttpServletResponse available")

                response.status = result.statusCode.value()
                result.headers.forEach { headerName, values ->
                    values.forEach { value -> response.addHeader(headerName, value) }
                }

                val body = result.body
                if (body != null) {
                    response.contentType = "application/json"
                    response.writer.write(objectMapper.writeValueAsString(body))
                }
            }
        }
    }
}
