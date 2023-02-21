package co.LabsProjects.recipeapi.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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

    @NotNull
    private String username;

    @NotNull
    private String description;

    private long rating;

    public void setRating(int rating){
        if (rating <= 0 || rating > 10) {
            throw new IllegalStateException("Rating must be between 0 and 10.");
        } else {
            this.rating = rating;
        }
    }
}
