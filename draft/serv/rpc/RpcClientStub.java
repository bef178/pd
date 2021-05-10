package pd.net.serv.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class RpcClientStub {

    @SuppressWarnings("unchecked")
    public static <T> T getRemoteService(String host, int port, Class<T> interfaceClass) {

        return (T) Proxy.newProxyInstance(
                RpcClientStub.class.getClassLoader(),
                new Class<?>[] { interfaceClass },
                new RpcClientInvocationHandler(host, port, interfaceClass));
    }
}

class RpcClientInvocationHandler implements InvocationHandler {

    private static final RpcSerializer rpcSerializer = new RpcSerializer();

    private final String host;

    private final int port;

    private final Class<?> interfaceClass;

    public RpcClientInvocationHandler(String host, int port, Class<?> interfaceClass) {
        this.host = host;
        this.port = port;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] methodArgs)
            throws IOException, ClassNotFoundException {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            rpcSerializer.sendReq(interfaceClass, method, methodArgs, socket.getOutputStream());

            // ... remote service is calculating

            return rpcSerializer.recvAck(socket.getInputStream());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
