package pd.fun.ernie.entity;

import java.util.List;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ErnieRequest {

    public List<ErnieMessage> messages;

    public boolean stream;
}
