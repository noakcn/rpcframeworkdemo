package com.noak.rpc.noakrpcdemo;

/**
 * @author noak
 * @date 2018-05-23 12:08
 */
public class RpcProvider {
    public static void main(String[] args) throws Exception {
        HelloService service = new HelloServiceImpl();
        RpcFramework.export(service,1234);

    }
}
