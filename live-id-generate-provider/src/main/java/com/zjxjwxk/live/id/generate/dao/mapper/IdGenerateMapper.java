package com.zjxjwxk.live.id.generate.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjxjwxk.live.id.generate.dao.po.IdGeneratePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 23:21
 */
@Mapper
public interface IdGenerateMapper extends BaseMapper<IdGeneratePO> {

    @Update("UPDATE t_id_generate_config " +
            "SET next_threshold = next_threshold + step, " +
            "current_start = current_start + step, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version}")
    int updateCurrentStartById(@Param("id") int id, @Param("version") int version);

    @Select("select * from t_id_generate_config")
    List<IdGeneratePO> selectAll();
}
