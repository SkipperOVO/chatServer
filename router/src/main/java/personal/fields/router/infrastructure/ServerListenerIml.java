package personal.fields.router.infrastructure;

import VO.ServerInfo;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ServerListenerIml implements ServerListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerListenerIml.class);

    @Autowired
    private ZkClient zkClient;

    @Value("${server.local.address}")
    private String localAddress;

    @Value("${server.local.port}")
    private int port;

    @Value("${server.local.name}")
    private String name;

    @Value("${zk.server.root}")
    private String zkRoot;

    private List<ServerInfo> serverInfoList;

    public ServerListenerIml() {
        this.serverInfoList = new ArrayList<>();
    }

    @Override
    public List<ServerInfo> getAll() {
        return serverInfoList;
    }

    @Override
    public void updateAll() {
        // 主动更新 ip list
    }

    @Override
    public void register() {

        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String nodePath = zkRoot + "/" + localAddress + ":" + port;
                if (!zkClient.exists(nodePath)) {
                    zkClient.createEphemeral(nodePath);
                }
            }
        }, 0, 120, TimeUnit.SECONDS);

        serverInfoList.add(new ServerInfo(localAddress, port));
        // 监听 zookeeper 服务器 ip 节点列表
        zkClient.subscribeChildChanges(zkRoot, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {

                logger.info("Server list changed: now " + list);

                List<ServerInfo> newServerInfoList = new ArrayList<>();
                for (String ip : list) {
                    newServerInfoList.add(new ServerInfo(ip, port));
                }
                serverInfoList = newServerInfoList;
            }
        });
    }

    @PostConstruct
    public void postRegister() {
        this.register();
    }

}
