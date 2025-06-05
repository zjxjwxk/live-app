package com.zjxjwxk.live.user.provider;

import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户中台 Dubbo 服务提供者
 *
 * @author Xinkang Wu
 * @date 2025/3/30 16:32
 */
@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication implements CommandLineRunner {

    @Resource
    private IUserTagService userTagService;

    public static void main(String[] args) {
        SpringApplication springBootApplication = new SpringApplication(UserProviderApplication.class);
        springBootApplication.setWebApplicationType(WebApplicationType.NONE);
        springBootApplication.run(args);
    }

    @Override
    public void run(String... args) {
        long userId = 10001L;

        System.out.println("======开始设置标签======");
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_RICH));
        System.out.println("当前用户是否拥有IS_RICH标签: " + userTagService.containTag(userId, UserTagsEnum.IS_RICH));
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_VIP));
        System.out.println("当前用户是否拥有IS_VIP标签: " + userTagService.containTag(userId, UserTagsEnum.IS_VIP));
        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("当前用户是否拥有IS_OLD_USER标签: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));

        System.out.println("======开始删除标签======");
        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_RICH));
        System.out.println("当前用户是否拥有IS_RICH标签: " + userTagService.containTag(userId, UserTagsEnum.IS_RICH));
        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_VIP));
        System.out.println("当前用户是否拥有IS_VIP标签: " + userTagService.containTag(userId, UserTagsEnum.IS_VIP));
        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("当前用户是否拥有IS_OLD_USER标签: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
    }
}
