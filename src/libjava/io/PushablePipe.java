package libjava.io;

public interface PushablePipe extends Pushable {

    /**
     * return the guest stream
     */
    public <T extends Pushable> T join(T downstream);
}
