package co.LabsProjects.recipeapi.repo;

import co.LabsProjects.recipeapi.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepo extends JpaRepository<Recipe, Long> {

    List<Recipe> findByNameContainingIgnoreCase(String name);

    Recipe findByReviews_Id(long id);

    List<Recipe> getByAverageReviewRatingGreaterThan(Double minimumRating);

    List<Recipe> getByNameContainingIgnoreCaseAndDifficultyRatingLessThanEqual(String name, int difficultyRating);

    List<Recipe> getByUser_Username(String username);
}
