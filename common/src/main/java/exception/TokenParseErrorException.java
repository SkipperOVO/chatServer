package exception;

public class TokenParseErrorException extends Exception {
    @Override
    public String toString() {
        return "Token 非法或者已过期！";
    }
}
