package com.yourssu.soongpt.domain.target.implement

enum class StudentType(val code: Int) {
    GENERAL(0),       // 일반 학생
    FOREIGNER(1),     // 외국인 학생
    MILITARY(2),      // 군위탁 학생
    TEACHING_CERT(3)  // 교직이수자
}
