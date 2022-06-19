import VO.ServerInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class TestFastJSON {

    public static void main(String[] args) {

        ServerInfo serverInfo = new ServerInfo("xxxx", 123);


        String jsonStr = JSON.toJSONString(serverInfo);

        ServerInfo serverInfo1 =  JSON.parseObject(jsonStr, ServerInfo.class);

        System.out.println(jsonStr);

    }
}
