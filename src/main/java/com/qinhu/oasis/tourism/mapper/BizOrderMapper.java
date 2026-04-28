package com.qinhu.oasis.tourism.mapper;

import com.qinhu.oasis.tourism.entity.BizOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 业务订单数据访问层（MyBatis Mapper），对应 mapper/tourism/BizOrderMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface BizOrderMapper {

    /**
     * 插入订单记录，自动回填主键 ID
     *
     * @param order 订单实体
     * @return 受影响行数
     */
    int insert(BizOrder order);

    /**
     * 根据 ID 查询订单（排除软删除记录）
     *
     * @param id 订单 ID
     * @return 订单实体，不存在时返回 null
     */
    BizOrder selectById(@Param("id") Long id);

    /**
     * 更新订单状态
     *
     * @param id     订单 ID
     * @param status 目标状态（参见 OrderStatus）
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
