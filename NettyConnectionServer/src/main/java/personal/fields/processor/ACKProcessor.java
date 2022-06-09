package personal.fields.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.channelMap.SimpleHashMap;
import personal.fields.constant.AttrbuteSet;
import personal.fields.infrastructure.ioc.IOC.SpringIOC;
import personal.fields.protocol.ChatProtocol;
import personal.fields.util.ACKToString;
import personal.fields.vo.NotifyPendingPack;

import java.util.concurrent.*;

public class ACKProcessor extends BaseProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ACKProcessor.class);

    private volatile boolean isRunning = false;

    public ACKProcessor(ThreadPoolExecutor threadPool, ProcessorContainer container) {
        super(threadPool, container);
    }


    /**
     *
     * @desc 如何设计 ACK 和 notify 队列的匹配问题呢？
     * 方案1：
     *  一种朴素的想法是，用单个线程遍历所有的活跃 channel，然后处理每个channel上的ack和notify队列
     *  这种方法易于实现，但是有两个问题，一个是单线程模式虽然避免了线程安全问题，但是单线程没有利用到多处理器的性能，只能利用单个处理器，性能不理想。
     *  第二个问题是，如果有几十万的用户同时在线，但是在聊天的可能就是少数，那么这存在很大的无效遍历。
     * 方案2:
     *  这时候可能会想到，这种轮询方式不是正好是 selector 改进的
     *  地方吗，selector 已经帮我们选好了每次有IO发生的channel，直接在IO触发的时候在这些有IO的channel上进行ack和notify匹配。问题是我们不能直接使用 Netty
     *  的 IO 线程来做业务逻辑，这会严重影响IO的响应性。所以要开用户线程池处理。每次有IO发生时，在这个channel上提交一个任务到线程池处理。但是如果仅仅是这样，
     *  会有并发问题的，因为在同一个channel上每次IO都会提交一个任务，线程池中的多个线程会并发的在ack和notify队列上操作，有线程安全问题。我们希望每个channel上
     *  或者handler上尽量是单线程模式的，这样避免了同步问题。解决方法是使用一个标记变量，标记当前channel上是否正在进行匹配，如果正在执行，那么io线程直接把ack放入
     *  并发队列然后直接返回（还是有IO线程被阻塞的风险，但是因为有人正在take，所以几乎不会被阻塞，只要notFull，第一时间会通知），不会再提交匹配任务。这样就保持了单线程模型。
     *  其实实际上并不是单线程，单线程指的是匹配过程是单线程执行的，两个队列是仍存在并发的put和take的，不过因为基于快照和读取方向单一性，匹配过程中ackqueue只会take头节点
     *  快照节点个数次，不会有并发读写；而notifyqueue会take <= 尾节点快照节点个数次,也不会存在并发读写（notifyqueue 并发put在头部，在快照外）。您可能会想，如果正好
     *  我remove了快照的头节点，同时又有人插入头节点，这不并发读写了吗，然而，同步队列在remove时会调用fullLock，锁整个队列。put和fullLock互斥，相当于二者原子执行。线程安全。
     *  这个方法性能很好，利用了多线程多处理器和selector，同时又没有阻塞IO线程。存在的问题是，匹配过程依赖IO事件，如果队列中有ACK，但是没有IO事件了，相当于这个用户和所有人都没有
     *  数据交互了，那么就会出现饥饿ACK，然后触发了超时重发，白白浪费了。使用较短的队列可以避免这种情况出现，极端情况就是队列长度尾1，那么每次ACK都会被处理，而代价就是IO线程put时
     *  被阻塞，降低服务器响应性；与之相反，增大队列长度，将会提升服务器的响应性，但是饥饿ACK会降低用户端的响应性。越大的队列饥饿ACK约多。（一是完全没有数据交互是低频操作，另一个是
     *  使用稍微短些ACKqueue可以有效缓解这种情况）
     *  (糟糕的解决方法。解决方法可以是通过定时扫描一遍所有ACKqueue，避免出现饥饿ACK。或者利用心跳机制）
     *
     *
     * 方案3：
     *  使用一个全局 任务队列 ，每次有ACK消息到达，都递交到任务队列，由线程池中一个线程执行。每个任务标记自己的channel和自己所属的线程id，每次提交到线程池的任务队列时，都要只要自己的
     *  绑定线程执行自己，防止自己被并发执行，避免了同步问题。模仿netty的线程模式。如果不是自己的线程取到了自己（任务），那就把我再放回队列中吧，我只要我的线程。设置一个几十万长度的任务队列,
     *  按照一个任务 1KB 计算，几十万也才几十M的内存，可以接受。(如果超出任务队列，丢弃，和断网是一样的流程了）。性能高，不阻塞 IO 线程，客户端和服务器端响应性都很高。没有死ACK。
     *
     *
     * 超时重发机制：
     *  接受clientB的对于notify的ACk超时的话，那么就需要重发了，而且这个操作不能依赖IO线程的通知了，因为是客户端网络问题，可能一直都不会由IO事件，所以要赶紧重发。
     *  超时机制可以参考Netty的IdleStateHandler，通过提交延迟任务来实现。不过如果单独设置一个线程池来完成这个任务，将和匹配的任务出现同步问题，所以可以使用方案3，使用同一个线程池，
     *  然后还是使用线程绑定机制。
     * @param ctx
     * @param msg
     */
    @Override
    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {

        BlockingQueue<ChatProtocol.ChatProtoPack> ack_queue = ctx.channel().attr(AttrbuteSet.ACK_QUEUE).get();

        if (isRunning) {
            try {
                ack_queue.put(msg);
                return ;
            } catch (InterruptedException ine) {
                ine.printStackTrace();
                // 重试
            }
        }
        // put 会阻塞线程，使用业务线程组避免 netty worker 线程阻塞
        this.execute(() -> {
            try {
                // ack_queue 只有一个线程访问，线程安全
                ack_queue.put(msg);
                logger.info("放入一个 ACK 到 ACK 队列了");
                logger.info("{ ACK: " + msg.getAck().getAck() + ", SEQ: " + msg.getAck().getSeq() + "}");

                // 只处理一个批次，避免一直占用线程导致饥饿
                int batch = Math.min(ack_queue.size(), 16);
                // 开始匹配 Notify
                // notify_queue 可能会有另一个线程在并发在尾部添加元素，只有当前线程会 remove 队列中的元素（消费者生产者模型）
                BlockingQueue<NotifyPendingPack> notify_queue = ctx.channel().attr(AttrbuteSet.NOTIFY_QUEUE).get();
                SimpleHashMap<Integer, Channel> channelMap = ((SimpleHashMap) SpringIOC.getBean("getChannelIdMap"));
                for (int i = 0; i < batch; ++i) {
                    ChatProtocol.ChatProtoPack ack = ack_queue.take();
                    /** 可能我们想直接用当前的第一个 ACK ，也就是最先到达的 ACK，去匹配发送最久的 Notify，这符合时间关系顺序。
                     但是因为网路问题，很有可能先发送notify的ack，后到达，这个时候不可能去抛弃这个ack，因为这样的话，因为一个先发送notify的
                     网络不好的对端用户，就导致所有对端用户的ack都被抛弃，结果就是clientA的notfiy队列会不断堆积，然后导致大量离线消息，但是仅仅
                     是因为一个对端用户网络问题。
                     使用每个ACK遍历notify queue的方式匹配，虽然时间复杂度是近似 O(N),但是大部分请款下，网络是很好的，消息都是天然有序的，匹配过程
                     可以非常快，仅当双端用户都网络很差才会发生O(N)的时间复杂度。
                     **/
                    int notiftQSize = notify_queue.size();
                    // 遍历当前的 notifyQueue 快照(此时并发新添加的 notify 不会被处理）
                    boolean matched = false;
                    for (int k = 0; k < notiftQSize; ++k) {
                        NotifyPendingPack notify = notify_queue.peek();
                        // ACK 匹配 notify
                        if (notify.getNotifyPack().getS2CNotifyMsg().getSeq()+1 == ack.getAck().getAck()) {
                            // 这个remove会是一个 O(N) 的操作，但是可以通过自己实现阻塞队列来优化到 O(1)
                            notify_queue.remove(notify);
                            // 发送给 clientB ack 消息
                            Channel peerCh = channelMap.get(notify.getNotifyPack().getS2CNotifyMsg().getToId());
                            peerCh.writeAndFlush(ack.getAck().getSeq() + 1);
                            logger.info("收到客户端对notify的ACK：");
                            logger.info(ACKToString.ackString(ack));
                            logger.info("推送消息成功！clientB已接收到");
                            matched = true; break;
                        }
                    }
                    // Notify 队列中没有任何可以与 ACK 匹配的，说明是滞留 ACK，抛弃
                    if (!matched) {
                        logger.info("丢弃ACK消息" + ACKToString.ackString(ack));
                    }
                }
            } catch (InterruptedException ine) {
                ine.printStackTrace();
                // 发生异常重试两次
                // pass
            }
        });

    }
}
