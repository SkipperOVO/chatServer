package personal.fields.protocol;

import lombok.Data;

@Data
public class NotifyMsgText {

    private Integer version;

    private Integer fromId;

    private Integer toId;

    private String msg;

    private Integer msgId;

    private Integer seq;

    private Integer type;

}
