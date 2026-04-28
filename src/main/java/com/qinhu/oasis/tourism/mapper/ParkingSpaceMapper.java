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
     * 查询所有非关闭状态的停车区域（用于初始化 Redis 库存及列表展示）
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

    /**
     * CAS 扣减可用车位数（防止并发超卖，WHERE available_count > 0）
     *
     * @param id 区域 ID
     * @return 受影响行数，0 表示库存已为 0 扣减失败
     */
    int decrementAvailable(@Param("id") Long id);

    /**
     * 增加可用车位数（订单取消时调用，WHERE available_count < total_capacity）
     *
     * @param id 区域 ID
     * @return 受影响行数
     */
    int incrementAvailable(@Param("id") Long id);
}
