package com.yourssu.soongpt.domain.target.implement

enum class ScopeType(val code: Int) {
    UNIVERSITY(0),  // 전교생 대상
    COLLEGE(1),     // 단과대학 범위
    DEPARTMENT(2)   // 학과 범위
}
