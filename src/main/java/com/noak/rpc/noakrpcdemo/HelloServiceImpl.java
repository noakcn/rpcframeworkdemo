package com.noak.rpc.noakrpcdemo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author noak
 * @date 2018-05-23 12:06
 */
@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        log.info("hello 方法被调用咯");
        return "hello " + name;
    }
}
