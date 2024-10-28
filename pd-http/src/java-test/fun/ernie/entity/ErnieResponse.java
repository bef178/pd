package fun.ernie.entity;

public class ErnieResponse {

    public String id;

    public String object;

    public Integer created;

    public Integer sentence_id;

    public Boolean is_end;

    public Boolean is_truncated;

    public String result;

    public Boolean need_clear_history;

    public Integer ban_round;

    public ErnieUsage usage;

    public Integer error_code;

    public String error_msg;
}
