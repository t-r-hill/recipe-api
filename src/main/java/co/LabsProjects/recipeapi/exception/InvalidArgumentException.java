package co.LabsProjects.recipeapi.exception;

public class InvalidArgumentException extends Exception {

    public InvalidArgumentException(String message){
        super(message);
    }

    public InvalidArgumentException(){}
}
