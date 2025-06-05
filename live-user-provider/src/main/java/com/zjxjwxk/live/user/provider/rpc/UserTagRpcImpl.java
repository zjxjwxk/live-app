package com.zjxjwxk.live.user.provider.rpc;

import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.interfaces.IUserTagRpc;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author Xinkang Wu
 * @date 2025/6/2 17:03
 */
@DubboService
public class UserTagRpcImpl implements IUserTagRpc {

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return false;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return false;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return false;
    }
}
