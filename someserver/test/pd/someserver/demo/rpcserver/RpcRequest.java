package pd.someserver.demo.rpcserver;

import java.io.Serializable;

public class RpcRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String interfaceClassName;

    public String methodName;

    public Class<?>[] methodArgClasses;

    public Object[] methodArgs;
}
