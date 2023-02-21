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
public class Step {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    private int stepNumber;

    @NotNull
    private String description;
}
