package co.LabsProjects.recipeapi.repo;

import co.LabsProjects.recipeapi.model.CustomUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<CustomUserDetails, Long> {

    CustomUserDetails findByUsername(String username);
}