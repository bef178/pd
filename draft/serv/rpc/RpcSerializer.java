package pd.net.serv.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class RpcSerializer {

    public static class ReqObject {

        public String interfaceClassName;

        public String methodName;

        public Class<?>[] methodArgClasses;

        public Object[] methodArgs;

        public ReqObject() {
            // dummy
        }

        public ReqObject(Class<?> interfaceClass, Method method, Object[] methodArgs) {
            this.interfaceClassName = ServiceRegistry.getInterfaceClassName(interfaceClass);
            this.methodName = method.getName();
            this.methodArgClasses = method.getParameterTypes();
            this.methodArgs = methodArgs;
        }

        /**
         * called by client
         */
        public void serialize(OutputStream dst) throws IOException {
            ObjectOutputStream writer = new ObjectOutputStream(dst);
            writer.writeUTF(interfaceClassName);
            writer.writeUTF(methodName);
            writer.writeObject(methodArgClasses);
            writer.writeObject(methodArgs);
        }
    }

    /**
     * called by client
     */
    public Object recvAck(InputStream src) throws IOException, ClassNotFoundException {
        return new ObjectInputStream(src).readObject();
    }

    /**
     * called by server
     */
    public ReqObject recvReq(InputStream src) throws IOException, ClassNotFoundException {
        ObjectInputStream reader = new ObjectInputStream(src);
        ReqObject req = new ReqObject();
        req.interfaceClassName = reader.readUTF();
        req.methodName = reader.readUTF();
        req.methodArgClasses = (Class<?>[]) reader.readObject();
        req.methodArgs = (Object[]) reader.readObject();
        return req;
    }

    /**
     * called by server
     */
    public void sendAck(Object result, OutputStream dst) throws IOException {
        new ObjectOutputStream(dst).writeObject(result);
    }

    /**
     * called by client
     */
    public void sendReq(Class<?> interfaceClass, Method method, Object[] methodArgs,
            OutputStream dst) throws IOException {
        new ReqObject(interfaceClass, method, methodArgs).serialize(dst);
    }
}
