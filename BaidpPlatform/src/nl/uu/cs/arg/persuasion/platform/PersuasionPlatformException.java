package nl.uu.cs.arg.persuasion.platform;

public class PersuasionPlatformException extends Exception {

    private static final long serialVersionUID = 1L;

    boolean isCritical;

    public PersuasionPlatformException(String errorMessage, boolean isCritical) {
        super(errorMessage);
        this.isCritical = isCritical;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

}
