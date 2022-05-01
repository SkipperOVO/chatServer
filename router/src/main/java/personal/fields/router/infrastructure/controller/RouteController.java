package personal.fields.router.infrastructure.controller;

import VO.ServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import personal.fields.router.serverMangerment.NettyServerManager;
import result.ResultDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import personal.fields.router.DTO.ServerInfoDTO;
import token.JwtToken;
import token.Token;

@RestController
public class RouteController {

    @Autowired
    private NettyServerManager nettyServerManager;

    @RequestMapping(value="/getChatServerInfo", method = RequestMethod.POST)
    public ResultDTO<ServerInfoDTO> route(@RequestParam(value="userId") Integer userId,
                                          @RequestParam(value="token") String token) {

        // controller 统一拦截异常并生成合适的信息给展示层
        try {

            Token jwtToken = new JwtToken(token);
            ServerInfo serverInfo = nettyServerManager.selectOne();
            return ResultDTO.success(new ServerInfoDTO(serverInfo));

        } catch (Exception e) {
            return ResultDTO.error(e.toString());
        }

    }
}
