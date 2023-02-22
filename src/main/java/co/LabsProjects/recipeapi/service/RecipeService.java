package co.LabsProjects.recipeapi.service;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import co.LabsProjects.recipeapi.exception.NoSuchRecipeException;
import co.LabsProjects.recipeapi.model.Recipe;
import co.LabsProjects.recipeapi.repo.RecipeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    @Autowired
    RecipeRepo recipeRepo;

    @Transactional
    public Recipe createNewRecipe(Recipe recipe) throws InvalidArgumentException {
        recipe.validate();
        recipe = recipeRepo.save(recipe);
        recipe.generateLocationURI();
        return recipe;
    }

    public Recipe getRecipeById(Long id) throws NoSuchRecipeException {
        Optional<Recipe> recipeOptional = recipeRepo.findById(id);

        if (recipeOptional.isEmpty()) {
            throw new NoSuchRecipeException("No recipe with ID " + id + " could be found.");
        }

        Recipe recipe = recipeOptional.get();
        recipe.generateLocationURI();
        return recipe;
    }

    public Recipe getRecipeByReviewId(Long reviewId){
        Recipe recipe = recipeRepo.findByReviews_Id(reviewId);
        recipe.generateLocationURI();
        return recipe;
    }

    public List<Recipe> getRecipesByName(String name) throws NoSuchRecipeException {
        List<Recipe> matchingRecipes = recipeRepo.findByNameContainingIgnoreCase(name);

        if (matchingRecipes.isEmpty()) {
            throw new NoSuchRecipeException("No recipes could be found with that name.");
        }

        return matchingRecipes;
    }

    public List<Recipe> getRecipesByNameWithMaxDifficulty(String name, int difficultyRating) throws NoSuchRecipeException {
        List<Recipe> recipes = recipeRepo.getByNameContainingIgnoreCaseAndDifficultyRatingLessThanEqual(name, difficultyRating);

        if (recipes.isEmpty()){
            throw new NoSuchRecipeException("No recipes could be found matching that criteria");
        }

        return recipes;
    }

    public List<Recipe> getAllRecipes() throws NoSuchRecipeException {
        List<Recipe> recipes = recipeRepo.findAll();

        if (recipes.isEmpty()) {
            throw new NoSuchRecipeException("There are no recipes yet :( feel free to add one though");
        }
        return recipes;
    }

    public List<Recipe> getRecipesByRatingGreaterThan(Double minimumRating) throws NoSuchRecipeException {
        List<Recipe> recipes = recipeRepo.getByAverageReviewRatingGreaterThan(minimumRating);

        if (recipes.isEmpty()){
            throw new NoSuchRecipeException("No recipes could be found with a rating greater than " + minimumRating);
        }

        return recipes;
    }

    public List<Recipe> getRecipesByUsername(String username) throws NoSuchRecipeException {
        List<Recipe> recipes = recipeRepo.getByUsername(username);

        if (recipes.isEmpty()){
            throw new NoSuchRecipeException("No recipes can be found for the user: " + username);
        }

        return recipes;
    }

    @Transactional
    public Recipe deleteRecipeById(Long id) throws NoSuchRecipeException {
        try {
            Recipe recipe = getRecipeById(id);
            recipeRepo.deleteById(id);
            return recipe;
        } catch (NoSuchRecipeException e) {
            throw new NoSuchRecipeException(e.getMessage() + " Could not delete.");
        }
    }

    @Transactional
    public Recipe updateRecipe(Recipe recipe, boolean forceIdCheck) throws NoSuchRecipeException, InvalidArgumentException {
        try {
            if (forceIdCheck) {
                getRecipeById(recipe.getId());
            }
            recipe.validate();
            recipe.setAverageReviewRating();
            Recipe savedRecipe = recipeRepo.save(recipe);
            savedRecipe.generateLocationURI();
            return savedRecipe;
        } catch (NoSuchRecipeException e) {
            throw new NoSuchRecipeException("The recipe you passed in did not have an ID found in the database." +
                    " Double check that it is correct. Or maybe you meant to POST a recipe not PATCH one.");
        }
    }
}
