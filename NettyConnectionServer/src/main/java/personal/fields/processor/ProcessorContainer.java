package personal.fields.processor;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import personal.fields.protocol.ChatProtocol;
import personal.fields.vo.NotifyPendingPack;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.*;

@Component
public class ProcessorContainer {

    // 线程安全，只会初始化一次，不会发生更改
    private final HashMap<String /* DataBodyCase */, Processor> handlers = new HashMap<>();

    private LinkedBlockingQueue<NotifyPendingPack> notifyQueue = new LinkedBlockingQueue<>(64);

    private LinkedBlockingQueue<ChatProtocol.ChatProtoPack> ackQueue = new LinkedBlockingQueue<>(64);

    private final ThreadPoolExecutor businessThreads = new ThreadPoolExecutor(
            8, 256, 5*60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(32));


    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {
        this.handlers.get(msg.getDataBodyCase().toString()).process(ctx, msg);
    }


    public void addProcessor(String processorName, Processor processor) {
        this.handlers.put(processorName, processor);
    }

    public ThreadPoolExecutor getThreadPool()  {
        return this.businessThreads;
    }

    public LinkedBlockingQueue<NotifyPendingPack> getNotifyQueue() {
        return this.notifyQueue;
    }

    public LinkedBlockingQueue<ChatProtocol.ChatProtoPack> getAckQueue() {
        return ackQueue;
    }
}
