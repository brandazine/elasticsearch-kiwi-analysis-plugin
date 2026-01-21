package com.brandazine.elasticsearch.analysis.kiwi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for POSTagSet-based settings defaults.
 *
 * Note: The actual settings interfaces (KiwiTokenizerSettings, etc.) are
 * annotated with @AnalysisSettings and are instantiated by Elasticsearch
 * via dynamic proxy. These tests verify the POSTagSet defaults used
 * when settings are empty.
 */
class KiwiSettingsDefaultsTest {

    @Test
    fun `default stop tags include particles`() {
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.PARTICLES))
    }

    @Test
    fun `default stop tags include endings`() {
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.ENDINGS))
    }

    @Test
    fun `default stop tags include punctuation`() {
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.PUNCTUATION))
    }

    @Test
    fun `default stop tags do not include nouns`() {
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.none { it in POSTagSet.NOUNS })
    }

    @Test
    fun `default stop tags do not include verbs`() {
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.none { it in POSTagSet.PREDICATES })
    }

    @Test
    fun `content words set includes nouns and predicates`() {
        assertTrue(POSTagSet.CONTENT_WORDS.containsAll(POSTagSet.NOUNS))
        assertTrue(POSTagSet.CONTENT_WORDS.containsAll(POSTagSet.PREDICATES))
    }
}
