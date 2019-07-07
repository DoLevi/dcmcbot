package de.untenrechts.dev.dcmcbot.exceptions;

public class IllegalCommandException extends RuntimeException {

    public IllegalCommandException() {
        super();
    }
    public IllegalCommandException(String message) {
        super(message);
    }
    public IllegalCommandException(String message, Throwable cause) {
        super(message, cause);
    }
    public IllegalCommandException(Throwable cause) {
        super(cause);
    }
}
