package com.zjxjwxk.live.user.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjxjwxk.live.user.provider.dao.po.UserTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author Xinkang Wu
 * @date 2025/6/2 17:05
 */
@Mapper
public interface IUserTagMapper extends BaseMapper<UserTagPO> {

    @Update("UPDATE t_user_tag SET ${fieldName} = ${fieldName} | #{tag} WHERE user_id = #{userId}")
    int setTag(Long userId, String fieldName, long tag);

    @Update("UPDATE t_user_tag SET ${fieldName} = ${fieldName} & ~#{tag} WHERE user_id = #{userId}")
    int cancelTag(Long userId, String fieldName, long tag);
}
