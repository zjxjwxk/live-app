package com.zjxjwxk.live.user.provider.rpc;

import com.zjxjwxk.live.user.dto.UserDTO;
import com.zjxjwxk.live.user.interfaces.IUserRpc;
import org.apache.dubbo.config.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRpcImplTest {

    private static final String REGISTER_ADDRESS = "nacos://127.0.0.1:8848?username=nacos&&password=nacos";
    private static RegistryConfig registryConfig;
    private static ApplicationConfig applicationConfig;
    private static IUserRpc userRpc;

    public static void initConfig() {
        registryConfig = new RegistryConfig();
        registryConfig.setAddress(REGISTER_ADDRESS);

        applicationConfig = new ApplicationConfig();
        applicationConfig.setName("live-user-provider-test");
        applicationConfig.setRegistry(registryConfig);
    }

    public static void initProvider() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(9090);

        ServiceConfig<IUserRpc> userServiceConfig = new ServiceConfig<>();
        userServiceConfig.setInterface(IUserRpc.class);
        userServiceConfig.setProtocol(protocolConfig);
        userServiceConfig.setApplication(applicationConfig);
        userServiceConfig.setRegistry(registryConfig);
        userServiceConfig.setRef(new UserRpcImpl());

        userServiceConfig.export();
        System.out.println("IUserRpc 服务暴露");
    }

    public static void initConsumer() {
        ReferenceConfig<IUserRpc> userReferenceConfig = new ReferenceConfig<>();
        userReferenceConfig.setInterface(IUserRpc.class);
        userReferenceConfig.setApplication(applicationConfig);
        userReferenceConfig.setRegistry(registryConfig);
        userReferenceConfig.setLoadbalance("random");

        userRpc = userReferenceConfig.get();
    }

    @BeforeAll
    static void setUp() {
        initConfig();
        initProvider();
        initConsumer();
    }

    @Test
    void getByUserId() {
        UserDTO userDTO = userRpc.getByUserId(10000L);
        System.out.println(userDTO);
        assertNotNull(userDTO);
    }
}