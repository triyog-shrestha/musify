// AuthException.java
// Thrown by AuthService when login or registration fails.

package exception;

public class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }
}