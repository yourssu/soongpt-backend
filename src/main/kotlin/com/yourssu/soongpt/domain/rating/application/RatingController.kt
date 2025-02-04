package com.yourssu.soongpt.domain.rating.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.rating.application.dto.RatingCreatedRequest
import com.yourssu.soongpt.domain.rating.application.dto.RatingFoundRequest
import com.yourssu.soongpt.domain.rating.business.RatingService
import com.yourssu.soongpt.domain.rating.business.dto.RatingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ratings")
class RatingController(
    private val ratingService: RatingService,
) {
    @PostMapping
    fun save(request: RatingCreatedRequest): Response<RatingResponse> {
        val response = ratingService.save(request.toCommand())
        return Response(result = response)
    }

    @GetMapping
    fun findBy(request: RatingFoundRequest): Response<RatingResponse> {
        val response = ratingService.findBy(request.toCommand())
        return Response(result = response)
    }
}