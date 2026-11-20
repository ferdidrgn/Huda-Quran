package org.ferdidrgn.hudaquran.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DuaContentRepositoryTest {
    private val repository = DuaContentRepository()

    @Test
    fun categoriesAndSearchUseTheCuratedCatalog() {
        val categories = repository.getCategories()
        assertTrue(categories.isNotEmpty())
        assertTrue(repository.getAll().size >= 20)

        val firstCategory = categories.first()
        assertTrue(repository.getAll().filter { it.dua.category == firstCategory }.isNotEmpty())
        assertEquals(
            repository.getAll().size,
            repository.search(" ").size,
        )
    }

    @Test
    fun searchMatchesTitleMeaningAndTags() {
        val item = repository.getAll().first()
        assertTrue(repository.search(item.dua.title).any { it.dua.id == item.dua.id })
        assertTrue(repository.search(item.dua.category).any { it.dua.id == item.dua.id })
        assertTrue(repository.sourceBadge().providerName.isNotBlank())
    }
}
