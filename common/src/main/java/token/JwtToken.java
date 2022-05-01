package token;

import VO.UserInfo;
import exception.TokenParseErrorException;

public class JwtToken implements Token {


    private UserInfo user;

    private String tokenStr;

    private String refreshTokenStr;

    public JwtToken(String tokenStr) throws TokenParseErrorException {
        this.tokenStr = tokenStr;
        if (!verify(tokenStr))
            throw new TokenParseErrorException();
    }


    public Integer getUserId() {
        return user.getUserId();
    }

    public String getUserName() {
        return user.getUserName();
    }

    public String getRole() {
        return user.getRole();
    }

    public boolean verify(String tokenStr) {
        // Todo 验证 token 逻辑
        return true;
    }

    public String generateToken(Integer userId, String userName) {
        // Todo 生成 token
        return "token";
    }

    public void refreshToken() {
        // Todo 刷新 token
    }

    public String getTokenString() {
        return tokenStr;
    }

    public String getRefreshToken() {
        return refreshTokenStr;
    }
}
