package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.infrastructure.exception.FileNotFoundException
import org.springframework.core.io.ClassPathResource
import java.io.File


object FileReader {
    fun getFile(folderPath: String, patterns: List<String>, prefix: String = "", postFix: String = ""): File {
        try {
            val resource = ClassPathResource(folderPath)
            if (!resource.exists() || !resource.file.isDirectory) {
                throw FileNotFoundException()
            }
            val folder = resource.file
            val majorFiles = folder.listFiles { file -> file.name.startsWith(prefix) &&
                        file.name.endsWith(postFix) }
                ?: throw FileNotFoundException()
            return majorFiles.find { file -> containsAllPatterns(fileName = file.name, patterns = patterns) }
            ?: throw FileNotFoundException("파일을 찾을 수 없습니다: $folderPath")
        } catch (_: Exception) {
            throw FileNotFoundException("파일을 찾을 수 없습니다: $folderPath")
        }
    }

    private fun containsAllPatterns(
        fileName: String,
        patterns: List<String>
    ): Boolean {
        return patterns.all { fileName.contains(it) }
    }
}
