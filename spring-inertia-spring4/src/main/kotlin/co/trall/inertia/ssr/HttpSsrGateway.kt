package co.trall.inertia.ssr

import co.trall.inertia.InertiaPage
import co.trall.inertia.config.InertiaProperties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

/**
 * HTTP-based SSR gateway that communicates with a Node.js SSR service.
 *
 * The SSR service should expose:
 * - `POST /render` - Accepts InertiaPage JSON, returns SsrResponse JSON
 * - `GET /health` - Returns 200 if healthy
 *
 * Example SSR service setup (Node.js):
 * ```javascript
 * import { createServer } from 'node:http'
 * import { createInertiaApp } from '@inertiajs/vue3'
 * import { renderToString } from '@vue/server-renderer'
 *
 * const server = createServer(async (req, res) => {
 *   if (req.url === '/health') {
 *     res.writeHead(200)
 *     res.end()
 *     return
 *   }
 *
 *   if (req.url === '/render' && req.method === 'POST') {
 *     const page = await readBody(req)
 *     const { head, body } = await renderPage(page)
 *     res.writeHead(200, { 'Content-Type': 'application/json' })
 *     res.end(JSON.stringify({ head, body }))
 *   }
 * })
 *
 * server.listen(13714)
 * ```
 */
class HttpSsrGateway(
    private val properties: InertiaProperties,
    restClientBuilder: RestClient.Builder
) : SsrGateway {

    private val logger = LoggerFactory.getLogger(HttpSsrGateway::class.java)

    private val restClient = restClientBuilder
        .baseUrl(properties.ssr.url)
        .build()

    override fun render(page: InertiaPage): SsrResponse? {
        return try {
            restClient.post()
                .uri("/render")
                .contentType(MediaType.APPLICATION_JSON)
                .body(page)
                .retrieve()
                .body(SsrResponse::class.java)
        } catch (e: ResourceAccessException) {
            // Connection error (timeout, connection refused, etc.)
            logger.warn("SSR service unavailable: ${e.message}")
            null
        } catch (e: RestClientException) {
            // HTTP error or response parsing error
            logger.warn("SSR render failed, falling back to client-side rendering", e)
            null
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            restClient.get()
                .uri("/health")
                .retrieve()
                .toBodilessEntity()
            true
        } catch (e: ResourceAccessException) {
            // Connection error - service is not reachable
            logger.debug("SSR service not reachable: ${e.message}")
            false
        } catch (e: RestClientException) {
            // HTTP error - service returned non-2xx status
            logger.debug("SSR service health check failed", e)
            false
        }
    }
}
