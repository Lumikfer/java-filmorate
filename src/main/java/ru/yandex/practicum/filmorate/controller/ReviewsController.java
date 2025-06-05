package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.service.ReviewsService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reviews createReview(@Valid @RequestBody Reviews review) {
        return reviewsService.createReview(review);
    }

    @PutMapping
    public Reviews updateReview(@Valid @RequestBody Reviews review) {
        return reviewsService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Integer id) {
        Reviews review = reviewsService.getReviewById(id);
        reviewsService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Reviews getReview(@PathVariable Integer id) {
        return reviewsService.getReviewById(id);

    }

    @GetMapping
    public List<Reviews> getPopularReviews(@RequestParam(required = false) Integer filmId,
                                           @RequestParam(defaultValue = "10") Integer count) {
        return reviewsService.getPopularReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Integer reviewId,
                        @PathVariable int userId) {
        reviewsService.addLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") Integer reviewId,
                           @PathVariable Integer userId) {
        reviewsService.removeLikeAndDislike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Integer reviewId,
                           @PathVariable Integer userId) {
        reviewsService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") Integer reviewId,
                              @PathVariable Integer userId) {
        reviewsService.removeLikeAndDislike(reviewId, userId);
    }
}

