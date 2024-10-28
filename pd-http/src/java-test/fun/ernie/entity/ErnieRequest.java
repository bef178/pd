package fun.ernie.entity;

import java.util.List;

public class ErnieRequest {

    public List<ErnieMessage> messages;

    public Boolean stream;

    public Float temperature;

    public Float top_p;

    public Float penalty_score;

    public String system;

    public List<String> stop;

    public Integer max_output_tokens;

    public Float frequency_penalty;

    public Float presence_penalty;

    public String user_id;
}
