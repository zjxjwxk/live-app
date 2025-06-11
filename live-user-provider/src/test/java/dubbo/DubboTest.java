package dubbo;

import com.zjxjwxk.live.user.interfaces.IUserRpc;
import com.zjxjwxk.live.user.provider.rpc.UserRpcImpl;
import org.apache.dubbo.config.*;

/**
 * @author Xinkang Wu
 * @date 2025/3/30 22:55
 */
public class DubboTest {

    private static final String REGISTER_ADDRESS = "nacos://192.168.10.109:8848?username=nacos&&password=nacos";
    private static RegistryConfig registryConfig;
    private static ApplicationConfig applicationConfig;
    private IUserRpc userRpc;

    public static void initConfig() {
        registryConfig = new RegistryConfig();
        registryConfig.setAddress(REGISTER_ADDRESS);

        applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbo-test");
        applicationConfig.setRegistry(registryConfig);
    }

    public void initProvider() {
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

    public void initConsumer() {
        ReferenceConfig<IUserRpc> userReferenceConfig = new ReferenceConfig<>();
        userReferenceConfig.setInterface(IUserRpc.class);
        userReferenceConfig.setApplication(applicationConfig);
        userReferenceConfig.setRegistry(registryConfig);
        userReferenceConfig.setLoadbalance("random");

        userRpc = userReferenceConfig.get();
    }

    public static void main(String[] args) throws InterruptedException {
//        initConfig();
//        DubboTest dubboTest = new DubboTest();
//        dubboTest.initProvider();
//        dubboTest.initConsumer();
//        while (true) {
//            dubboTest.userRpc.test();
//            Thread.sleep(3000);
//        }
    }
}
