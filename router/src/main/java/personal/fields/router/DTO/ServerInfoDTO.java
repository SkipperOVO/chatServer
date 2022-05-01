package personal.fields.router.DTO;

import VO.ServerInfo;
import lombok.Data;

@Data
public class ServerInfoDTO {

    private String ip;

    private int port;

    public ServerInfoDTO(ServerInfo serverInfo) {
        this.ip = serverInfo.getIp();
        this.port = serverInfo.getPort();
    }
}
