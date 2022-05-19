package token;

public interface Token {

    public Integer getUserId();

    public String getUserName();

    public String getRole();

    public boolean verify(String token);

    public String getTokenString();
}
