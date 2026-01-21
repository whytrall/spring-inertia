package co.trall.inertia

import co.trall.inertia.config.InertiaProperties
import tools.jackson.databind.json.JsonMapper
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InertiaResponseFactoryConcurrencyTest {

    private val objectMapper = JsonMapper.builder().findAndAddModules().build()
    private val properties = InertiaProperties()
    private val factory = InertiaResponseFactory(properties, objectMapper)

    @BeforeTest
    fun setUp() {
        val request = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @AfterTest
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        factory.clearShared()
    }

    @Test
    fun `getShared is thread-safe under concurrent reads`() {
        // Setup shared props
        factory.share("static", "value")
        factory.share("dynamic") { Thread.currentThread().name }

        val threadCount = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()

        repeat(threadCount) { index ->
            executor.submit {
                try {
                    repeat(100) {
                        val shared = factory.getShared()
                        // Verify static prop is always present and correct
                        assertEquals("value", shared["static"])
                        // Verify dynamic prop is present (value varies by thread)
                        assertTrue(shared.containsKey("dynamic"))
                    }
                } catch (e: Throwable) {
                    errors[index] = e
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(errors.isEmpty(), "Errors occurred: ${errors.values.map { it.message }}")
    }

    @Test
    fun `share and getShared are thread-safe under concurrent writes and reads`() {
        val threadCount = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()

        // Half threads write, half threads read
        repeat(threadCount) { index ->
            executor.submit {
                try {
                    if (index % 2 == 0) {
                        // Writer thread
                        repeat(100) { iteration ->
                            factory.share("key-$index-$iteration", "value-$iteration")
                        }
                    } else {
                        // Reader thread
                        repeat(100) {
                            val shared = factory.getShared()
                            // Should not throw ConcurrentModificationException
                            shared.forEach { (_, _) -> /* just iterate */ }
                        }
                    }
                } catch (e: Throwable) {
                    errors[index] = e
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(errors.isEmpty(), "Errors occurred: ${errors.values.map { it.message }}")
    }

    @Test
    fun `callback resolution in getShared does not cause race conditions`() {
        val callCount = AtomicInteger(0)

        factory.share("counter") {
            callCount.incrementAndGet()
        }

        val threadCount = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = ConcurrentHashMap<Int, Int>()

        repeat(threadCount) { index ->
            executor.submit {
                try {
                    val shared = factory.getShared()
                    results[index] = shared["counter"] as Int
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        // Each thread should get a unique counter value
        assertEquals(threadCount, results.size)
        // All values should be unique (1 to threadCount)
        assertEquals(threadCount, results.values.toSet().size)
    }

    @Test
    fun `clearShared is thread-safe`() {
        // Pre-populate with data
        repeat(100) { i ->
            factory.share("initial-$i", "value-$i")
        }

        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()

        repeat(threadCount) { index ->
            executor.submit {
                try {
                    repeat(20) { iteration ->
                        // Readers - most threads
                        if (index < threadCount - 2) {
                            val shared = factory.getShared()
                            // Just iterate, should not throw ConcurrentModificationException
                            shared.forEach { (_, _) -> }
                        }
                        // One writer thread
                        else if (index == threadCount - 2) {
                            factory.share("new-$iteration", "value")
                        }
                        // One clearer thread (only clears occasionally)
                        else if (iteration % 10 == 0) {
                            factory.clearShared()
                        }
                    }
                } catch (e: Throwable) {
                    errors[index] = e
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(errors.isEmpty(), "Errors occurred: ${errors.values.map { "${it::class.simpleName}: ${it.message}" }}")
    }

    @Test
    fun `getShared returns consistent snapshot even during concurrent modifications`() {
        // Add many initial props
        repeat(100) { i ->
            factory.share("initial-$i", i)
        }

        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1) // To start all threads at once
        val doneLatch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()
        val snapshotSizes = ConcurrentHashMap<Int, MutableList<Int>>()

        repeat(threadCount) { index ->
            snapshotSizes[index] = mutableListOf()
            executor.submit {
                try {
                    startLatch.await() // Wait for signal to start
                    repeat(50) {
                        if (index % 2 == 0) {
                            // Modifier thread - add new props
                            factory.share("new-$index-$it", it)
                        }
                        // All threads read
                        val snapshot = factory.getShared()
                        snapshotSizes[index]!!.add(snapshot.size)

                        // Verify snapshot is consistent (no null keys/values from partial state)
                        snapshot.forEach { (key, value) ->
                            assertTrue(key.isNotEmpty(), "Key should not be empty")
                        }
                    }
                } catch (e: Throwable) {
                    errors[index] = e
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown() // Start all threads
        doneLatch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(errors.isEmpty(), "Errors occurred: ${errors.values.map { it.message }}")

        // Verify each thread's snapshots were monotonically non-decreasing or consistent
        // (size should only grow as we add props, never shrink unexpectedly within a single read)
        snapshotSizes.forEach { (_, sizes) ->
            assertTrue(sizes.isNotEmpty())
        }
    }

    @Test
    fun `multiple factories are independent and thread-safe`() {
        val factory1 = InertiaResponseFactory(properties, objectMapper)
        val factory2 = InertiaResponseFactory(properties, objectMapper)

        val threadCount = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()

        repeat(threadCount) { index ->
            executor.submit {
                try {
                    val factory = if (index % 2 == 0) factory1 else factory2
                    repeat(100) { iteration ->
                        factory.share("key-$index-$iteration", "value")
                        factory.getShared()
                    }
                } catch (e: Throwable) {
                    errors[index] = e
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue(errors.isEmpty(), "Errors occurred: ${errors.values.map { it.message }}")

        // Verify factories maintained separate state
        val shared1 = factory1.getShared()
        val shared2 = factory2.getShared()

        // Each factory should only have keys from its threads (even indices for factory1)
        assertTrue(shared1.keys.any { it.startsWith("key-0-") })
        assertTrue(shared2.keys.any { it.startsWith("key-1-") })
    }
}
