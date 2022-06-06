package personal.fields.constant;

import io.netty.util.AttributeKey;
import personal.fields.channelMap.ChannelMap;

public class AttrbuteSet {


    public static final AttributeKey<Integer> ACK_SEQ = AttributeKey.valueOf("ackSeq");

    public static final AttributeKey<Integer> C2S_SEQ = AttributeKey.valueOf("c2sSeq");

    public static final AttributeKey<Integer> USER_ID = AttributeKey.valueOf("userId");

    public static final AttributeKey<ChannelMap> CHANNEL_MAP = AttributeKey.valueOf("channelMap") ;


}
