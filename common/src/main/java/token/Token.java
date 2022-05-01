package token;

public interface Token {

    public Integer getUserId();

    public String getUserName();

    public String getRole();

    public boolean verify(String token);

    public String generateToken(Integer userId, String userName);

    public void refreshToken();

    public String getRefreshToken();

    public String getTokenString();
}
