# NettyIMServer
> An instant messaging system powerd by Netty.
> 基于 Netty 的即时通讯系统。
## Framework
![image](https://user-images.githubusercontent.com/24368223/175755918-3edb9620-2617-4e5f-9200-d85eab3c052c.png)
## implemented and Todo
- [x] 单机几十万人同时在线  
- [ ] 断线重连  
- [x] 心跳保活
- [x] 二进制消息协议
- [x] 集群支持（暂时支持 logic、NettyServer、router集群扩展）
- [x] zookeeper 动态路由
- [x] 应用层负载均衡算法
- [x] 单对单 单聊
- [x] ACK机制和RocketMQ实现消息可靠投递，消息不丢失
- [ ] 消息去重
- [x] 消息推送超时重试
- [x] 实现线程绑定线程模型，避免线程安全问题（参考Netty线程模型）
- [ ] MQ 数据库写操作削峰
- [ ] 数据库 MyCat 读写分离
- [ ] 离线消息拉取
- [ ] 历史消息拉取
- [ ] 消息记录删除
- [ ] ...
