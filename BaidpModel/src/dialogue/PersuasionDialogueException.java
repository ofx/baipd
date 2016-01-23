package dialogue;

public class PersuasionDialogueException extends Exception {

    private static final long serialVersionUID = 1L;

    public PersuasionDialogueException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return this.getMessage();
    }

}
