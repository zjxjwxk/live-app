package com.zjxjwxk.live.user.provider.service.impl;

import com.google.common.base.CaseFormat;
import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.provider.dao.mapper.IUserTagMapper;
import com.zjxjwxk.live.user.provider.dao.po.UserTagPO;
import com.zjxjwxk.live.user.provider.service.IUserTagService;
import com.zjxjwxk.live.user.utils.UserTagUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Xinkang Wu
 * @date 2025/6/2 17:04
 */
@Service
public class UserTagServiceImpl implements IUserTagService {

    @Resource
    private IUserTagMapper userTagMapper;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null) {
            return false;
        }
        String fieldName = userTagsEnum.getFieldName();
        String methodName = "get" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, fieldName);
        try {
            Long tagInfo = (Long) UserTagPO.class.getMethod(methodName).invoke(userTagPO);
            return UserTagUtils.isContain(tagInfo, userTagsEnum.getTag());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
