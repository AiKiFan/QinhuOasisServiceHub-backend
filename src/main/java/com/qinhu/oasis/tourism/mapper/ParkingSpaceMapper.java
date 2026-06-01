package com.qinhu.oasis.tourism.mapper;

import com.qinhu.oasis.tourism.entity.ParkingSpace;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 停车区域数据访问层（MyBatis Mapper），对应 mapper/tourism/ParkingSpaceMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface ParkingSpaceMapper {

    /**
     * 查询所有非关闭状态的停车区域
     *
     * @return 停车区域列表
     */
    List<ParkingSpace> selectAll();

    /**
     * 根据 ID 查询停车区域
     *
     * @param id 区域 ID
     * @return 停车区域实体，不存在时返回 null
     */
    ParkingSpace selectById(@Param("id") Long id);
}
