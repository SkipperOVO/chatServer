package personal.fields.router.serverMangerment;

import VO.ServerInfo;
import exception.NoAvaliableServerException;

public interface NettyServerManager {

    public ServerInfo selectOne() throws NoAvaliableServerException;


}
