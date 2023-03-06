package co.LabsProjects.recipeapi.model;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(generator = "recipe_generator")
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn
    @JsonIgnore
    private CustomUserDetails user;

    @Column(nullable = false)
    private int minutesToMake;

    @Column(nullable = false)
    private int difficultyRating;

    private double averageReviewRating;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipeId", nullable = false, foreignKey = @ForeignKey)
    private Collection<Ingredient> ingredients;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipeId", nullable = false, foreignKey = @ForeignKey)
    private Collection<Step> steps;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipeId", nullable = false, foreignKey = @ForeignKey)
    private Collection<Review> reviews;

    @Transient
    @JsonIgnore
    private URI locationURI;

    public String getAuthor() {
        return user.getUsername();
    }

    public void validate() throws InvalidArgumentException {
        if (ingredients == null || ingredients.size() == 0) {
            throw new InvalidArgumentException("You have to have at least one ingredient for your recipe!");
        } else if (steps == null || steps.size() == 0) {
            throw new InvalidArgumentException("You have to include at least one step for your recipe!");
        } else if (name == null || name.isBlank()){
            throw new InvalidArgumentException("Name must have a value");
//        } else if (user == null) {
//            throw new InvalidArgumentException("Username must have a value");
        } else if (difficultyRating < 0 || difficultyRating > 10) {
            throw new InvalidArgumentException("Difficulty rating must have a value between 1 and 10");
        } else if (minutesToMake < 0) {
            throw new InvalidArgumentException("A recipe must take at least 0 minutes to make");
        }
    }

    public void generateLocationURI() {
        try {
            locationURI = new URI(
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/recipes/")
                            .path(String.valueOf(id))
                            .toUriString());
        } catch (URISyntaxException e) {
            //Exception should stop here.
        }
    }

    public void setAverageReviewRating(){
        this.averageReviewRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
