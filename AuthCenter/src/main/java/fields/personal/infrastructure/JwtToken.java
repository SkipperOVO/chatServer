package fields.personal.infrastructure;

import VO.UserInfo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import exception.TokenParseErrorException;
import fields.personal.token.Token;
import org.springframework.stereotype.Component;

@Component
public class JwtToken implements Token {

    private final static String TOKEN_SECRET = "secret";

    private UserInfo user;

    private String tokenStr;

    private String refreshTokenStr;

    private DecodedJWT jwt;

    public JwtToken(String tokenStr) throws TokenParseErrorException {
        this.tokenStr = tokenStr;
        if (!verify(tokenStr))
            throw new TokenParseErrorException();
//        fillUser()

        //only for test
        user = new UserInfo();
        user.setUserId(123);
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

    public boolean verify(String token) {
        // for test now
        return true;
//        try {
//            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
//            JWTVerifier verifier = JWT.require(algorithm).build();
//            jwt = verifier.verify(token);
//
//            return true;
//        } catch (TokenExpiredException toke) {
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

//    private void fillUser() {
//        try {
//            String playload = token.split("\\.")[1];
//            JSONObject jsonObject = JSONObject.parseObject(Util.decodeBase64(playload));
////            return RSAEncrypt.decrypt(jsonObject.getString("stuId"), RSAEncrypt.getPrivateKey());
//            return jsonObject.getString("stuId");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public String getTokenString() {
        return tokenStr;
    }

}
