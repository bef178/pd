package pd.net.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class RpcCodec {

    /**
     * called by server
     */
    public RpcRequest deserializeRequest(InputStream src) throws IOException, ClassNotFoundException {
        return (RpcRequest) new ObjectInputStream(src).readObject();
    }

    /**
     * called by client
     */
    public Object deserializeResponse(InputStream src) throws IOException, ClassNotFoundException {
        RpcResponse response = (RpcResponse) new ObjectInputStream(src).readObject();
        return response.result;
    }

    /**
     * called by client
     */
    public void serializeRequest(Class<?> interfaceClass, Method method, Object[] methodArgs, OutputStream dst)
            throws IOException {
        RpcRequest request = new RpcRequest();
        request.interfaceClassName = ServiceRegistry.getInterfaceClassName(interfaceClass);
        request.methodName = method.getName();
        request.methodArgClasses = method.getParameterTypes();
        request.methodArgs = methodArgs;
        new ObjectOutputStream(dst).writeObject(request);
    }

    /**
     * called by server
     */
    public void serializeResponse(Object result, OutputStream dst) throws IOException {
        RpcResponse response = new RpcResponse();
        response.result = result;
        new ObjectOutputStream(dst).writeObject(response);
    }
}
