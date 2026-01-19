package com.zjxjwxk.live.framework.datasource.starter.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Sharding JDBC Nacos URL Provider
 *
 * @author Xinkang Wu
 * @date 2026/1/19 22:50
 */
@Slf4j
public class NacosDriverURLProvider implements ShardingSphereDriverURLProvider {

    private static final String NACOS_TYPE = "nacos:";
    private static final String GROUP = "DEFAULT_GROUP";
    private static final Logger log = LoggerFactory.getLogger(NacosDriverURLProvider.class);

    @Override
    public boolean accept(String url) {
        return StringUtils.isNotBlank(url) && url.contains(NACOS_TYPE);
    }

    @Override
    public byte[] getContent(String url) {
        // url: jdbc:shardingsphere:nacos:live.zjxjwxk.com:8848:live-user-shardingjdbc.yaml?username=nacos&&password=nacos
        if (StringUtils.isBlank(url)) {
            return null;
        }
        // nacosUrl: live.zjxjwxk.com:8848:live-user-shardingjdbc.yaml?username=nacos&&password=nacos
        String nacosUrl = url.substring(url.lastIndexOf(NACOS_TYPE) + NACOS_TYPE.length());

        // nacosUrlStr: {"live.zjxjwxk.com", "8848", "live-user-shardingjdbc.yaml?username=nacos&&password=nacos"}
        String[] nacosUrlStr = nacosUrl.split(":");
        // nacosFileUrl: live-user-shardingjdbc.yaml?username=nacos&&password=nacos
        String nacosFileUrl = nacosUrlStr[2];

        // dataId: live-user-shardingjdbc.yaml
        String dataId = getDataId(nacosFileUrl);

        // username=>nacos, password=>nacos
        Properties properties = getProperties(nacosFileUrl);

        // serverAddr=>live.zjxjwxk.com:8848
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosUrlStr[0] + ":" + nacosUrlStr[1]);

        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(dataId, GROUP, 6000);
            log.info("Sharding JDBC Configuration File from Nacos:\n{}", content);
            return content.getBytes();
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取 Data ID
     *
     * @param nacosFile live-user-shardingjdbc.yaml?username=nacos&&password=nacos
     * @return live-user-shardingjdbc.yaml
     */
    private String getDataId(String nacosFile) {
        // nacosProperties: {"live-user-shardingjdbc.yaml", "username=nacos&&password=nacos"}
        String[] nacosProperties = nacosFile.split("\\?");
        // dataId: live-user-shardingjdbc.yaml
        return nacosProperties[0];
    }

    /**
     * 获取 Properties
     *
     * @param nacosFileUrl live-user-shardingjdbc.yaml?username=nacos&&password=nacos
     * @return username=>nacos, password=>nacos
     */
    private Properties getProperties(String nacosFileUrl) {
        Properties properties = new Properties();

        // nacosProperties: {"live-user-shardingjdbc.yaml", "username=nacos&&password=nacos"}
        String[] nacosProperties = nacosFileUrl.split("\\?");
        // propertyPairs: {"username=nacos", "password=nacos"}
        String[] propertyPairs = nacosProperties[1].split("&&");

        for (String propertyPair : propertyPairs) {
            // propertyItem: {"username", "nacos"}
            String[] propertyItem = propertyPair.split("=");
            String key = propertyItem[0];
            String value = propertyItem[1];
            properties.put(key, value);
        }
        return properties;
    }
}
