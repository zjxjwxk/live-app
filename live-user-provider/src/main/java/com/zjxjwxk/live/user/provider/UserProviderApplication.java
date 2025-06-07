package com.zjxjwxk.live.user.provider;

import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.dto.UserDTO;
import com.zjxjwxk.live.user.provider.service.IUserService;
import com.zjxjwxk.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.concurrent.CountDownLatch;

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
    @Resource
    private IUserService userService;

    public static void main(String[] args) {
        SpringApplication springBootApplication = new SpringApplication(UserProviderApplication.class);
        springBootApplication.setWebApplicationType(WebApplicationType.NONE);
        springBootApplication.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        long userId = 10001L;

//        // 测试设置标签
//        System.out.println("======开始设置标签======");
//        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_RICH));
//        System.out.println("当前用户是否拥有IS_RICH标签: " + userTagService.containTag(userId, UserTagsEnum.IS_RICH));
//        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println("当前用户是否拥有IS_VIP标签: " + userTagService.containTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println("当前用户是否拥有IS_OLD_USER标签: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
//
//        // 测试删除标签
//        System.out.println("======开始删除标签======");
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_RICH));
//        System.out.println("当前用户是否拥有IS_RICH标签: " + userTagService.containTag(userId, UserTagsEnum.IS_RICH));
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println("当前用户是否拥有IS_VIP标签: " + userTagService.containTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println("当前用户是否拥有IS_OLD_USER标签: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
//
//        // 测试设置标签失败
//        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println(userTagService.setTag(userId, UserTagsEnum.IS_VIP));
//
//        // 测试删除标签失败
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_VIP));
//        System.out.println(userTagService.cancelTag(userId, UserTagsEnum.IS_VIP));

//        // 测试并发设置标签
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        for (int i = 0; i < 100; ++i) {
//            Thread thread = new Thread(() -> {
//                try {
//                    countDownLatch.await();
//                    System.out.println("Set Result is " + userTagService.setTag(userId, UserTagsEnum.IS_VIP));
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            thread.start();
//        }
//        countDownLatch.countDown();
//        Thread.sleep(100000);

        // 测试用户信息延迟双删
        UserDTO userDTO = userService.getByUserId(userId);
        userDTO.setNickName("zjxjwxk1998");
        userService.updateUserInfo(userDTO);

        // 测试用户标签延迟双删
        System.out.println("IS_OLD_USER: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("Set Tag IS_OLD_USER: " + userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("IS_OLD_USER: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("Cancel Tag IS_OLD_USER: " + userTagService.cancelTag(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("IS_OLD_USER: " + userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
    }
}
