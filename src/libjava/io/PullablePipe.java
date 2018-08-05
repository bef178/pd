package libjava.io;

public interface PullablePipe extends Pullable {

    /**
     * return the guest stream
     */
    public <T extends Pullable> T join(T upstream);
}
