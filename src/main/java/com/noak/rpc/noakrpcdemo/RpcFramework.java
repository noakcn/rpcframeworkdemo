package com.noak.rpc.noakrpcdemo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

/**
 * @author noak
 * @date 2018-05-22 18:10
 */
@Slf4j
public class RpcFramework {
    /**
     * 暴露服务端口
     *
     * @param service
     * @param port
     */
    public static void export(final Object service, int port) throws Exception {
        log.info("export service {} on port {}", service.getClass().getName(), port);

        ServerSocket server = new ServerSocket(port);
        // 一直轮询
        for (; ; ) {
            // 此处一直阻塞等待有consumer古来
            final Socket socket = server.accept();

            // 每个请求打开一个独立线程
            new Thread(() -> {
                try {
                    @Cleanup
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    // consumer分三次发送所需方法信息
                    String methodName = input.readUTF();
                    Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                    Object[] arguments = (Object[]) input.readObject();
                    @Cleanup
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    // 获取到目标方法
                    Method method = service.getClass().getMethod(methodName, parameterTypes);
                    // 通过反射执行目标方法并返回结果
                    Object result = method.invoke(service, arguments);
                    // 将执行结果返回给consumer
                    output.writeObject(result);

                } catch (Exception e) {
                    log.error("e ", e);
                } finally {
                    try {
                        // 注意socket 必须在线程中关闭。
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    /**
     * 引用服务
     *
     * @param interfaceClass
     * @param host
     * @param port
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) {
        log.info("Get reomte service {} from server {}:{}", interfaceClass.getName(), host, port);
        // 通过jdk动态代理的方式直接返回给调用refer方法的调用者一个被动态代理处理过的对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            // 调用改对象的每个方法都会先调用下面的逻辑
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 当方法真实被调用的时候才会发起RPC远程请求provider执行服务
                Socket socket = new Socket(host, port);
                try {
                    @Cleanup
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    // 分三次发送方法所需信息
                    output.writeUTF(method.getName());
                    output.writeObject(method.getParameterTypes());
                    output.writeObject(args);
                    @Cleanup
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    // 得到服务执行的最终结果
                    Object result = input.readObject();
                    if (result instanceof Throwable) {
                        throw (Throwable) result;
                    }

                    return result;
                } catch (Exception e) {
                    log.error("execute fail", e);
                } finally {
                    socket.close();
                }
                return null;
            }
        });
    }


}
