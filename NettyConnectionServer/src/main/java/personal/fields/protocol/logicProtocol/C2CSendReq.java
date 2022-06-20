package personal.fields.protocol.logicProtocol;

import lombok.Data;

@Data
public class C2CSendReq {

    private Integer version;

    private Integer fromId;

    private Integer toId;

    private Integer seq;

    private String msg;

    private Integer type;

}
