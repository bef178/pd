package pd.net.serv.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import pd.log.ILogger;
import pd.log.LogManager;
import pd.net.serv.RequestContext;
import pd.net.serv.SomeServer;

public class RpcServer extends SomeServer<RequestContext> {

    private static final ILogger logger = LogManager.getLogger();

    public final ServiceRegistry serviceRegistry;

    private static final RpcSerializer rpcSerializer = new RpcSerializer();

    public RpcServer(int port) {
        this(port, new ServiceRegistry());
    }

    public RpcServer(int port, ServiceRegistry serviceRegistry) {
        super(port);
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected RequestContext buildRequest(Socket socket) throws IOException {
        return new RequestContext(socket);
    }

    private Object getImplementationClassInstance(Class<?> implementationClass)
            throws InstantiationException, IllegalAccessException {
        return implementationClass.newInstance(); // XXX where IoC/DI would function
    }

    @Override
    protected void onRequest(RequestContext request) {
        try {
            RpcSerializer.ReqObject reqObject = rpcSerializer.recvReq(request.reqStream);
            Object result = reflectionCall(reqObject.interfaceClassName, reqObject.methodName,
                    reqObject.methodArgClasses, reqObject.methodArgs);
            rpcSerializer.sendAck(result, request.ackStream);
        } catch (Exception e) {
            logger.logError(e.getMessage());
        }
    }

    private Object reflectionCall(String interfaceClassName, String methodName,
            Class<?>[] methodArgClasses, Object[] methodArgs)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException {
        Class<?> implClass = serviceRegistry.getImplementationClass(interfaceClassName);
        if (implClass == null) {
            throw new ClassNotFoundException("E: [" + interfaceClassName + "] not found");
        }
        Method method = implClass.getMethod(methodName, methodArgClasses);
        return method.invoke(getImplementationClassInstance(implClass), methodArgs);
    }
}
