package com.brandazine.elasticsearch.analysis.kiwi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for POSTagSet constants and utility methods.
 */
class POSTagSetTest {

    @Test
    fun `noun tags are correctly defined`() {
        assertEquals("NNG", POSTagSet.NNG)
        assertEquals("NNP", POSTagSet.NNP)
        assertEquals("NNB", POSTagSet.NNB)
        assertEquals("NR", POSTagSet.NR)
        assertEquals("NP", POSTagSet.NP)
    }

    @Test
    fun `predicate tags are correctly defined`() {
        assertEquals("VV", POSTagSet.VV)
        assertEquals("VA", POSTagSet.VA)
        assertEquals("VX", POSTagSet.VX)
        assertEquals("VCP", POSTagSet.VCP)
        assertEquals("VCN", POSTagSet.VCN)
    }

    @Test
    fun `particle tags are correctly defined`() {
        assertTrue(POSTagSet.PARTICLES.contains("JKS"))
        assertTrue(POSTagSet.PARTICLES.contains("JKO"))
        assertTrue(POSTagSet.PARTICLES.contains("JX"))
        assertEquals(9, POSTagSet.PARTICLES.size)
    }

    @Test
    fun `punctuation tags are correctly defined`() {
        assertTrue(POSTagSet.PUNCTUATION.contains("SF"))
        assertTrue(POSTagSet.PUNCTUATION.contains("SP"))
        assertTrue(POSTagSet.PUNCTUATION.contains("SS"))
        assertEquals(6, POSTagSet.PUNCTUATION.size)
    }

    @Test
    fun `isPunctuation returns true for punctuation tags`() {
        assertTrue(POSTagSet.isPunctuation("SF"))
        assertTrue(POSTagSet.isPunctuation("SP"))
        assertTrue(POSTagSet.isPunctuation("SS"))
        assertTrue(POSTagSet.isPunctuation("SE"))
        assertTrue(POSTagSet.isPunctuation("SO"))
        assertTrue(POSTagSet.isPunctuation("SW"))
    }

    @Test
    fun `isPunctuation returns false for non-punctuation tags`() {
        assertFalse(POSTagSet.isPunctuation("NNG"))
        assertFalse(POSTagSet.isPunctuation("VV"))
        assertFalse(POSTagSet.isPunctuation("JKS"))
    }

    @Test
    fun `isContentWord returns true for content word tags`() {
        assertTrue(POSTagSet.isContentWord("NNG"))
        assertTrue(POSTagSet.isContentWord("NNP"))
        assertTrue(POSTagSet.isContentWord("VV"))
        assertTrue(POSTagSet.isContentWord("VA"))
        assertTrue(POSTagSet.isContentWord("MAG"))
    }

    @Test
    fun `isContentWord returns false for non-content tags`() {
        assertFalse(POSTagSet.isContentWord("JKS"))
        assertFalse(POSTagSet.isContentWord("EP"))
        assertFalse(POSTagSet.isContentWord("SF"))
    }

    @Test
    fun `isStopTag returns true for default stop tags`() {
        assertTrue(POSTagSet.isStopTag("JKS"))
        assertTrue(POSTagSet.isStopTag("EP"))
        assertTrue(POSTagSet.isStopTag("SF"))
    }

    @Test
    fun `isStopTag returns false for content words`() {
        assertFalse(POSTagSet.isStopTag("NNG"))
        assertFalse(POSTagSet.isStopTag("VV"))
    }

    @Test
    fun `NOUNS set contains all noun tags`() {
        val expected = setOf("NNG", "NNP", "NNB", "NR", "NP")
        assertEquals(expected, POSTagSet.NOUNS)
    }

    @Test
    fun `PREDICATES set contains all verb and adjective tags`() {
        val expected = setOf("VV", "VA", "VX", "VCP", "VCN")
        assertEquals(expected, POSTagSet.PREDICATES)
    }

    @Test
    fun `DEFAULT_STOP_TAGS includes particles endings and punctuation`() {
        // Should include particles
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.PARTICLES))

        // Should include endings
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.ENDINGS))

        // Should include punctuation
        assertTrue(POSTagSet.DEFAULT_STOP_TAGS.containsAll(POSTagSet.PUNCTUATION))
    }
}
