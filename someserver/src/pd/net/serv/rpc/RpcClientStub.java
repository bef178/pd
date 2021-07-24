package pd.net.serv.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class RpcClientStub {

    public static <T> T getRemoteService(String host, int port, Class<T> interfaceClass) {
        return getRemoteService(host, port, interfaceClass, new RpcCodec());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRemoteService(String host, int port, Class<T> interfaceClass, RpcCodec rpcCodec) {

        return (T) Proxy.newProxyInstance(
                RpcClientStub.class.getClassLoader(),
                new Class<?>[] { interfaceClass },
                new RpcClientInvocationHandler(host, port, interfaceClass, rpcCodec));
    }
}

class RpcClientInvocationHandler implements InvocationHandler {

    private final String host;

    private final int port;

    private final Class<?> interfaceClass;

    private final RpcCodec rpcCodec;

    public RpcClientInvocationHandler(String host, int port, Class<?> interfaceClass, RpcCodec rpcCodec) {
        this.host = host;
        this.port = port;
        this.interfaceClass = interfaceClass;
        this.rpcCodec = rpcCodec;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] methodArgs)
            throws IOException, ClassNotFoundException {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            rpcCodec.serializeRequest(interfaceClass, method, methodArgs, socket.getOutputStream());

            // ... remote service is calculating

            return rpcCodec.deserializeResponse(socket.getInputStream());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
