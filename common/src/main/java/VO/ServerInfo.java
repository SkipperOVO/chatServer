package VO;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@Data
public class ServerInfo {

    @Value("${server.local.address}")
    private String ip;

    @Value("${server.local.port}")
    private Integer port;

    @Value("${server.local.name}")
    private String name;

    @Autowired
    private Environment env;

    public ServerInfo() {
//        this.ip = env.getProperty("server.local.address");
//        this.port = Integer.parseInt(env.getProperty("server.local.port"));
    };

    public ServerInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object serverInfo) {
        if (this == serverInfo)
            return  true;
        if (serverInfo instanceof ServerInfo) {
            ServerInfo obj = (ServerInfo) serverInfo;
            return (this.ip.equals(obj.ip) && this.port.equals(obj.port));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (ip + port).hashCode();
    }

    @Override
    public String toString() {
        return name + " : , " + ip + ":" + port;
    }

}
