package co.LabsProjects.recipeapi.service;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import co.LabsProjects.recipeapi.exception.NoSuchRecipeException;
import co.LabsProjects.recipeapi.exception.NoSuchReviewException;
import co.LabsProjects.recipeapi.model.Recipe;
import co.LabsProjects.recipeapi.model.Review;
import co.LabsProjects.recipeapi.repo.ReviewRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    ReviewRepo reviewRepo;

    @Autowired
    RecipeService recipeService;

    @Cacheable(value = "reviews", key = "#id")
    public Review getReviewById(Long id) throws NoSuchReviewException {
        Optional<Review> review = reviewRepo.findById(id);

        if (review.isEmpty()) {
            throw new NoSuchReviewException("The review with ID " + id + " could not be found.");
        }
        return review.get();
    }


    public ArrayList<Review> getReviewByRecipeId(Long recipeId) throws NoSuchRecipeException, NoSuchReviewException {
        Recipe recipe = recipeService.getRecipeById(recipeId);

        ArrayList<Review> reviews = new ArrayList<>(recipe.getReviews());

        if (reviews.isEmpty()) {
            throw new NoSuchReviewException("There are no reviews for this recipe.");
        }
        return reviews;
    }

    public List<Review> getReviewByUsername(String username) throws NoSuchReviewException {
        List<Review> reviews = reviewRepo.findByUser_Username(username);

        if (reviews.isEmpty()) {
            throw new NoSuchReviewException("No reviews could be found for username " + username);
        }

        return reviews;
    }

    @CachePut(value = "reviews", key = "#review.id")
    public Recipe postNewReview(Review review, Long recipeId) throws NoSuchRecipeException, InvalidArgumentException {
        Recipe recipe = recipeService.getRecipeById(recipeId);

        if (review.getAuthor().equals(recipe.getAuthor())){
            throw new InvalidArgumentException("Stop trying to upvote your own recipe!");
        }
        review.validate();
        recipe.getReviews().add(review);
        recipe.setAverageReviewRating();
        recipeService.updateRecipe(recipe, false);
        return recipe;
    }

    @Transactional
    @CacheEvict(value = "reviews", key = "id")
    public Review deleteReviewById(Long id) throws NoSuchReviewException, NoSuchRecipeException, InvalidArgumentException {
        Review review = getReviewById(id);

        if (null == review) {
            throw new NoSuchReviewException("The review you are trying to delete does not exist.");
        }

        Recipe recipe = recipeService.getRecipeByReviewId(id);
        reviewRepo.deleteById(id);
        reviewRepo.flush();
        recipeService.updateRecipe(recipe, false);
        return review;
    }

    @CachePut(value = "reviews", key = "#reviewToUpdate.id")
    @Transactional
    public Review updateReviewById(Review reviewToUpdate) throws NoSuchReviewException, NoSuchRecipeException, InvalidArgumentException {
        try {
            Review review = getReviewById(reviewToUpdate.getId());
        } catch (NoSuchReviewException e) {
            throw new NoSuchReviewException("The review you are trying to update. Maybe you meant to create one? If not," +
                    "please double check the ID you passed in.");
        }
        reviewToUpdate.validate();
        Recipe recipe = recipeService.getRecipeByReviewId(reviewToUpdate.getId());
        reviewRepo.save(reviewToUpdate);
        recipeService.updateRecipe(recipe, false);
        return reviewToUpdate;
    }
}
