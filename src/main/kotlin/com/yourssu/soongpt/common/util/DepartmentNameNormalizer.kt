package com.yourssu.soongpt.common.util

object DepartmentNameNormalizer {
    @Volatile
    private var canonicalByNormalized: Map<String, String> = emptyMap()

    @Volatile
    private var aliasesByNormalized: Map<String, String> = emptyMap()

    @Synchronized
    fun initialize(
        canonicalDepartments: Set<String>,
        aliases: Map<String, String>,
    ) {
        val canonicalMap = canonicalDepartments
            .map { normalizeFormat(it) to it.trim() }
            .toMap()

        val aliasMap = mutableMapOf<String, String>()
        for ((rawAlias, rawTarget) in aliases) {
            val normalizedAlias = normalizeFormat(rawAlias)
            if (normalizedAlias.isBlank()) {
                continue
            }

            val normalizedTarget = normalizeFormat(rawTarget)
            val canonicalTarget = canonicalMap[normalizedTarget]
                ?: throw IllegalStateException(
                    "department-normalization alias target not found in ssu-data.colleges: '$rawTarget'"
                )

            aliasMap[normalizedAlias] = canonicalTarget
        }

        canonicalByNormalized = canonicalMap
        aliasesByNormalized = aliasMap
    }

    fun normalize(input: String): String {
        val normalizedInput = normalizeFormat(input)
        if (normalizedInput.isBlank()) return normalizedInput

        canonicalByNormalized[normalizedInput]?.let { return it }
        aliasesByNormalized[normalizedInput]?.let { return it }

        return normalizedInput
    }

    fun normalizeNullable(input: String?): String? {
        return input?.let { normalize(it) }
    }

    @Synchronized
    fun resetForTest() {
        canonicalByNormalized = emptyMap()
        aliasesByNormalized = emptyMap()
    }

    private fun normalizeFormat(value: String): String {
        var normalized = value
            .replace('（', '(')
            .replace('）', ')')
            .trim()
            .replace(Regex("\\s+"), " ")

        // "전자정보공학부 (IT융합전공)" -> "전자정보공학부(IT융합전공)"
        normalized = normalized
            .replace(Regex("\\s*\\(\\s*"), "(")
            .replace(Regex("\\s*\\)\\s*"), ")")
            .replace(Regex("\\s+"), " ")
            .trim()

        // "전자정보공학부(IT융합전공)" -> "전자정보공학부 IT융합전공"
        val trailingParenMatch = Regex("^(.+)\\((.+)\\)$").matchEntire(normalized)
        if (trailingParenMatch != null) {
            val left = trailingParenMatch.groupValues[1].trim()
            val right = trailingParenMatch.groupValues[2].trim()
            normalized = "$left $right"
        }

        return normalized
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
