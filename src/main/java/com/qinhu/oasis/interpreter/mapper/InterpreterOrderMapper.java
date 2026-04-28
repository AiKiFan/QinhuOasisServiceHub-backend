package com.qinhu.oasis.interpreter.mapper;

import com.qinhu.oasis.interpreter.dto.InterpreterOrderVO;
import com.qinhu.oasis.tourism.entity.BizOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 翻译服务订单数据访问层（MyBatis Mapper），对应 mapper/interpreter/InterpreterOrderMapper.xml
 * <p>操作的底层表仍为 biz_order，通过 order_type=1 与车位订单区分</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface InterpreterOrderMapper {

    /**
     * 插入翻译服务订单，自动回填主键 ID
     *
     * @param order 订单实体（order_type=1）
     * @return 受影响行数
     */
    int insert(BizOrder order);

    /**
     * 根据 ID 查询订单（排除软删除）
     *
     * @param id 订单 ID
     * @return 订单实体，不存在时返回 null
     */
    BizOrder selectById(Long id);

    /**
     * 分页查询用户（游客侧）翻译服务订单列表
     *
     * @param userId 游客用户 ID
     * @param offset 分页偏移量
     * @param size   每页条数
     * @return 订单 VO 列表
     */
    List<InterpreterOrderVO> selectByUserId(@Param("userId") Long userId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    /**
     * 统计用户的翻译订单总数
     *
     * @param userId 用户 ID
     * @return 总数
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 分页查询译员侧订单列表（按 interpreter_id 过滤）
     *
     * @param interpreterId 译员用户 ID
     * @param offset        分页偏移量
     * @param size          每页条数
     * @return 订单 VO 列表
     */
    List<InterpreterOrderVO> selectByInterpreterId(@Param("interpreterId") Long interpreterId,
                                                   @Param("offset") int offset,
                                                   @Param("size") int size);

    /**
     * 统计译员的翻译订单总数
     *
     * @param interpreterId 译员用户 ID
     * @return 总数
     */
    long countByInterpreterId(@Param("interpreterId") Long interpreterId);

    /**
     * 更新订单状态
     *
     * @param id     订单 ID
     * @param status 目标状态（参见 OrderStatus）
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 译员接单：设置 interpreter_id 并将状态改为 ACCEPTED
     *
     * @param id            订单 ID
     * @param interpreterId 接单译员的用户 ID
     * @param status        目标状态（OrderStatus.ACCEPTED）
     * @return 受影响行数
     */
    int acceptOrder(@Param("id") Long id,
                    @Param("interpreterId") Long interpreterId,
                    @Param("status") Integer status);
}
