/**
 * Custom exception for authentication and authorization errors.
 * Thrown by AuthService when login, registration, password changes, or profile updates fail.
 * 
 * The exception message is user-friendly and can be displayed directly in UI error labels.
 */
package exception;

public class AuthException extends Exception {
    
    /**
     * Creates a new authentication exception with a user-friendly error message.
     * 
     * @param message Error description to display to the user
     */
    public AuthException(String message) {
        super(message);
    }
}