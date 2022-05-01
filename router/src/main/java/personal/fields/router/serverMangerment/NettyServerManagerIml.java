package personal.fields.router.serverMangerment;

import VO.ServerInfo;
import exception.NoAvaliableServerException;
import personal.fields.router.infrastructure.ServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class NettyServerManagerIml implements NettyServerManager {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerManagerIml.class);


    @Autowired
    private ServerListener serverListener;

    @Autowired
    BalanceLoader<ServerInfo> balanceLoader;


    public NettyServerManagerIml() {
    }

    public ServerInfo selectOne() throws NoAvaliableServerException {
        ServerInfo serverInfo = balanceLoader.getOne(serverListener.getAll());
        if (serverInfo == null) {
            return new RandomBalanceLoader<ServerInfo>().getOne(serverListener.getAll());
        }

        logger.info("router 选中服务器：" + serverInfo);

        return serverInfo;
    }

}
