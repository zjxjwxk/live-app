package com.zjxjwxk.live.id.generate.rpc;

import com.zjxjwxk.live.id.generate.interfaces.IdGenerateRpc;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 22:31
 */
@DubboService
public class IdGenerateRpcImpl implements IdGenerateRpc {

    @Override
    public Long getSeqId(Integer id) {
        return 0L;
    }

    @Override
    public Long getUnSeqId(Integer id) {
        return 0L;
    }
}
