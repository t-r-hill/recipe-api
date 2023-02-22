package co.LabsProjects.recipeapi.model;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue
    private long id;

    @NotNull(message = "A review must have a username")
    private String username;

    @NotNull(message = "A review must have a description")
    private String description;

    @NotNull(message = "A review must have a rating")
    private int rating;

    public void validate() throws InvalidArgumentException{
        if (rating <= 0 || rating > 10) {
            throw new InvalidArgumentException("Rating must be between 0 and 10.");
        } else if (username == null || username.isBlank()) {
            throw new InvalidArgumentException("Username must have a value");
        } else if (description == null || description.isBlank()){
            throw new InvalidArgumentException("Description must have a value");
        }
    }
}
