package personal.fields.router.infrastructure;



import VO.ServerInfo;

import java.util.List;

public interface ServerListener {

    public List<ServerInfo> getAll();

    public void updateAll();

    public void register();
}
