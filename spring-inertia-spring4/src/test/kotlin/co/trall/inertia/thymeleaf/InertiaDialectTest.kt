package co.trall.inertia.thymeleaf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InertiaDialectTest {

    @Test
    fun `dialect has correct name`() {
        val dialect = InertiaDialect()

        assertEquals("Inertia", dialect.name)
    }

    @Test
    fun `dialect has correct prefix`() {
        val dialect = InertiaDialect()

        assertEquals("inertia", dialect.prefix)
    }

    @Test
    fun `dialect registers page and head processors`() {
        val dialect = InertiaDialect()
        val processors = dialect.getProcessors("inertia")

        assertEquals(2, processors.size)
        assertTrue(processors.any { it is InertiaPageElementProcessor })
        assertTrue(processors.any { it is InertiaHeadElementProcessor })
    }
}
