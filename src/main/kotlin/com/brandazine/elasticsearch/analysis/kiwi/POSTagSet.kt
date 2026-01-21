package com.brandazine.elasticsearch.analysis.kiwi

/**
 * Korean Part-of-Speech (POS) tag constants based on the Kiwi morphological analyzer.
 *
 * Kiwi uses a modified Sejong tagset for Korean POS tagging.
 * Tags are grouped by category for easier filtering and reference.
 */
object POSTagSet {

    // ============================================================
    // 체언 (Substantives) - Nouns and related
    // ============================================================

    /** 일반 명사 (General Noun) */
    const val NNG = "NNG"

    /** 고유 명사 (Proper Noun) */
    const val NNP = "NNP"

    /** 의존 명사 (Dependent Noun) */
    const val NNB = "NNB"

    /** 수사 (Numeral) */
    const val NR = "NR"

    /** 대명사 (Pronoun) */
    const val NP = "NP"

    // ============================================================
    // 용언 (Predicates) - Verbs and Adjectives
    // ============================================================

    /** 동사 (Verb) */
    const val VV = "VV"

    /** 형용사 (Adjective) */
    const val VA = "VA"

    /** 보조 용언 (Auxiliary Predicate) */
    const val VX = "VX"

    /** 긍정 지정사 (Positive Copula - 이다) */
    const val VCP = "VCP"

    /** 부정 지정사 (Negative Copula - 아니다) */
    const val VCN = "VCN"

    // ============================================================
    // 수식언 (Modifiers)
    // ============================================================

    /** 관형사 (Determiner/Adnominal) */
    const val MM = "MM"

    /** 일반 부사 (General Adverb) */
    const val MAG = "MAG"

    /** 접속 부사 (Conjunctive Adverb) */
    const val MAJ = "MAJ"

    // ============================================================
    // 독립언 (Independents)
    // ============================================================

    /** 감탄사 (Interjection) */
    const val IC = "IC"

    // ============================================================
    // 관계언 (Relational) - Particles/Postpositions
    // ============================================================

    /** 주격 조사 (Nominative Particle) */
    const val JKS = "JKS"

    /** 보격 조사 (Complementizer Particle) */
    const val JKC = "JKC"

    /** 관형격 조사 (Genitive Particle) */
    const val JKG = "JKG"

    /** 목적격 조사 (Accusative Particle) */
    const val JKO = "JKO"

    /** 부사격 조사 (Adverbial Particle) */
    const val JKB = "JKB"

    /** 호격 조사 (Vocative Particle) */
    const val JKV = "JKV"

    /** 인용격 조사 (Quotative Particle) */
    const val JKQ = "JKQ"

    /** 보조사 (Auxiliary Particle) */
    const val JX = "JX"

    /** 접속 조사 (Conjunctive Particle) */
    const val JC = "JC"

    // ============================================================
    // 의존형태 (Bound Morphemes) - Endings
    // ============================================================

    /** 선어말 어미 (Prefinal Ending) */
    const val EP = "EP"

    /** 종결 어미 (Final Ending) */
    const val EF = "EF"

    /** 연결 어미 (Connective Ending) */
    const val EC = "EC"

    /** 명사형 전성 어미 (Nominalizing Ending) */
    const val ETN = "ETN"

    /** 관형형 전성 어미 (Adnominalizing Ending) */
    const val ETM = "ETM"

    // ============================================================
    // 접사 (Affixes)
    // ============================================================

    /** 체언 접두사 (Noun Prefix) */
    const val XPN = "XPN"

    /** 명사 파생 접미사 (Noun-deriving Suffix) */
    const val XSN = "XSN"

    /** 동사 파생 접미사 (Verb-deriving Suffix) */
    const val XSV = "XSV"

    /** 형용사 파생 접미사 (Adjective-deriving Suffix) */
    const val XSA = "XSA"

    /** 어근 (Root) */
    const val XR = "XR"

    // ============================================================
    // 기호 (Symbols)
    // ============================================================

    /** 마침표, 물음표, 느낌표 (Sentence-final Punctuation) */
    const val SF = "SF"

    /** 쉼표, 가운뎃점, 콜론, 빗금 (General Punctuation) */
    const val SP = "SP"

    /** 따옴표, 괄호, 줄표 (Quotation/Bracket) */
    const val SS = "SS"

    /** 줄임표 (Ellipsis) */
    const val SE = "SE"

    /** 붙임표 (Hyphen) */
    const val SO = "SO"

    /** 기타 기호 (Other Symbols) */
    const val SW = "SW"

    // ============================================================
    // 기타 (Others)
    // ============================================================

    /** 외국어 (Foreign Word) */
    const val SL = "SL"

    /** 한자 (Chinese Character) */
    const val SH = "SH"

    /** 숫자 (Number) */
    const val SN = "SN"

    /** 분석 불능 (Unknown) */
    const val UN = "UN"

    /** W 태그들 - 웹/특수 (Web/Special) */
    const val W_URL = "W_URL"
    const val W_EMAIL = "W_EMAIL"
    const val W_HASHTAG = "W_HASHTAG"
    const val W_MENTION = "W_MENTION"

    // ============================================================
    // Tag Groups for Filtering
    // ============================================================

    /** All noun tags (체언) */
    val NOUNS = setOf(NNG, NNP, NNB, NR, NP)

    /** All verb/adjective tags (용언) */
    val PREDICATES = setOf(VV, VA, VX, VCP, VCN)

    /** All modifier tags (수식언) */
    val MODIFIERS = setOf(MM, MAG, MAJ)

    /** All particle tags (조사) */
    val PARTICLES = setOf(JKS, JKC, JKG, JKO, JKB, JKV, JKQ, JX, JC)

    /** All ending tags (어미) */
    val ENDINGS = setOf(EP, EF, EC, ETN, ETM)

    /** All affix tags (접사) */
    val AFFIXES = setOf(XPN, XSN, XSV, XSA, XR)

    /** All punctuation/symbol tags (기호) */
    val PUNCTUATION = setOf(SF, SP, SS, SE, SO, SW)

    /** Default stop tags - commonly filtered out for search */
    val DEFAULT_STOP_TAGS = PARTICLES + ENDINGS + PUNCTUATION + setOf(EP, EF, EC)

    /** Content word tags - typically kept for search indexing */
    val CONTENT_WORDS = NOUNS + PREDICATES + MODIFIERS + setOf(IC, SL, SH, SN)

    /**
     * Check if a tag represents punctuation.
     */
    fun isPunctuation(tag: String): Boolean = tag in PUNCTUATION

    /**
     * Check if a tag represents a content word (noun, verb, adjective, etc.).
     */
    fun isContentWord(tag: String): Boolean = tag in CONTENT_WORDS

    /**
     * Check if a tag is typically filtered out for search.
     */
    fun isStopTag(tag: String): Boolean = tag in DEFAULT_STOP_TAGS
}
