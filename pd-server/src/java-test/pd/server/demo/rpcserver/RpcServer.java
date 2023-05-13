package pd.server.demo.rpcserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import pd.logger.Logger;
import pd.server.RequestContext;
import pd.server.SomeServer;

public class RpcServer extends SomeServer<RequestContext> {

    public final ServiceRegistry serviceRegistry;

    private final RpcCodec rpcCodec;

    public RpcServer(Logger logger) {
        this(new ServiceRegistry(), logger);
    }

    public RpcServer(ServiceRegistry serviceRegistry, Logger logger) {
        this(serviceRegistry, new RpcCodec(), logger);
    }

    public RpcServer(ServiceRegistry serviceRegistry, RpcCodec rpcCodec, Logger logger) {
        super(logger);
        this.serviceRegistry = serviceRegistry;
        this.rpcCodec = rpcCodec;
    }

    @Override
    protected RequestContext buildRequest(Socket socket) throws IOException {
        return new RequestContext(socket);
    }

    private Object getImplementationClassInstance(Class<?> implementationClass)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return implementationClass.getDeclaredConstructor().newInstance(); // XXX where IoC/DI would function
    }

    @Override
    protected void executeRequest(RequestContext request) {
        try {
            RpcRequest requestPayload = rpcCodec.deserializeRequest(request.reqStream);
            Object result = reflectionCall(
                    requestPayload.interfaceClassName,
                    requestPayload.methodName,
                    requestPayload.methodArgClasses,
                    requestPayload.methodArgs);
            rpcCodec.serializeResponse(result, request.ackStream);
        } catch (Exception e) {
            logger.logError(e.getMessage());
        }
    }

    private Object reflectionCall(String interfaceClassName, String methodName, Class<?>[] methodArgClasses, Object[] methodArgs)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class<?> implClass = serviceRegistry.getImplementationClass(interfaceClassName);
        if (implClass == null) {
            throw new ClassNotFoundException("E: [" + interfaceClassName + "] not found");
        }
        Method method = implClass.getMethod(methodName, methodArgClasses);
        return method.invoke(getImplementationClassInstance(implClass), methodArgs);
    }
}
