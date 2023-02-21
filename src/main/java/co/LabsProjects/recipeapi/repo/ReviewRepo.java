package co.LabsProjects.recipeapi.repo;

import co.LabsProjects.recipeapi.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    List<Review> findByUsername(String username);
}
