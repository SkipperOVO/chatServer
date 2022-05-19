import VO.ServerInfo;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.ArrayList;
import java.util.List;

public class zkClientTest {
    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient("192.168.1.101:2181", 30000, 30000, new SerializableSerializer());
        List<String> serverPath = zkClient.getChildren("/root");
        System.out.println(serverPath);
        List<ServerInfo> serverInfoList = new ArrayList<>();
//        for (String path : serverPath) {
//            ServerInfo serverInfo = new ServerInfo(zkClient.readData("/root/" + path), 7888, path);
//            System.out.println(serverInfo);
//        }

        String testClient = "testClient";
        zkClient.createEphemeral("/root/test", testClient);
        System.out.println((String) zkClient.readData("/root/test"));
        assert testClient.equals(zkClient.readData("/root/test"));

    }
}
