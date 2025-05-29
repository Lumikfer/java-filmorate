package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.service.ReviewsService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    @PostMapping
    public Reviews createRew(@Valid @RequestBody Reviews rew) {

        reviewsService.addRew(rew);
        return rew;

    }

    @PutMapping
    public Reviews updateRew(@Valid @RequestBody Reviews reviews) {
        reviewsService.updateRew(reviews);
        return reviews;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int rewid, @PathVariable int userid) {
        reviewsService.setLike(rewid, userid);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDis(@PathVariable int rewid, @PathVariable int userid) {
        reviewsService.delLike(rewid, userid);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLike(@PathVariable int rewid, @PathVariable int userid) {
        reviewsService.delLike(rewid, userid);
    }

    @GetMapping("/popular")
    public List<Reviews> getPopRew(@RequestParam(defaultValue = "10") int count, @RequestParam Integer filmId) {
        return reviewsService.getPop(count, filmId);
    }


}
