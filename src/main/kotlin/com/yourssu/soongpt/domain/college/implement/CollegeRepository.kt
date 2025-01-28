package com.yourssu.soongpt.domain.college.implement

interface CollegeRepository {
    fun save(college: College): College
    fun get(id: Long): College
}