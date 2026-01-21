package co.trall.inertia.http

import co.trall.inertia.InertiaResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(InertiaResponseHandler::class.java)

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        val supports = InertiaResponse::class.java.isAssignableFrom(returnType.parameterType)
        logger.debug("supportsReturnType called for {}: {}", returnType.parameterType, supports)
        return supports
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        logger.debug("handleReturnValue called with returnValue: {}", returnValue?.javaClass?.name)

        if (returnValue == null) {
            mavContainer.isRequestHandled = true
            return
        }

        val inertiaResponse = returnValue as InertiaResponse
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("No HttpServletRequest available")

        val result = inertiaResponse.toView(request)
        logger.debug("toView returned type: {}", result.javaClass.name)

        when (result) {
            is ModelAndView -> {
                val viewName = result.viewName
                logger.debug("Setting ModelAndView with viewName: '{}'", viewName)
                if (viewName.isNullOrBlank()) {
                    throw IllegalStateException("Inertia rootView is blank. Check your inertia.root-view configuration.")
                }
                // Use setViewName explicitly to ensure String type is preserved
                mavContainer.viewName = viewName
                mavContainer.model.putAll(result.model)
                result.status?.let { mavContainer.status = it }
                logger.debug("mavContainer.viewName after setting: '{}'", mavContainer.viewName)
            }
            is org.springframework.http.ResponseEntity<*> -> {
                logger.debug("Returning ResponseEntity with status: {}", result.statusCode)
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
            else -> {
                throw IllegalStateException("Unexpected return type from InertiaResponse.toView(): ${result.javaClass.name}")
            }
        }
    }
}
