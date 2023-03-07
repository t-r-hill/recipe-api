package co.LabsProjects.recipeapi.model;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
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

    @ManyToOne(optional = false)
    @JoinColumn
    @JsonIgnore
    private CustomUserDetails user;

    @NotNull(message = "A review must have a description")
    private String description;

    @NotNull(message = "A review must have a rating")
    private int rating;

    @JsonIgnore
    public String getAuthor() {
        return user.getUsername();
    }

    public void validate() throws InvalidArgumentException{
        if (rating <= 0 || rating > 10) {
            throw new InvalidArgumentException("Rating must be between 0 and 10.");
        } else if (user == null) {
            throw new InvalidArgumentException("Username must have a value");
        } else if (description == null || description.isBlank()){
            throw new InvalidArgumentException("Description must have a value");
        }
    }
}
