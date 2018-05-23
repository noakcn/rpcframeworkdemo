package com.noak.rpc.noakrpcdemo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author noak
 * @date 2018-05-23 12:08
 */
@Slf4j
public class RpcConsumer {
    public static void main(String[] args) throws Exception {
        // 此时获得的service是被JDK动态代理的包装后的service，在调用方法的时候会进行远程调用
        HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String hello = service.hello("world " + i);
            log.info(hello);
            Thread.sleep(100);
        }
    }
}
