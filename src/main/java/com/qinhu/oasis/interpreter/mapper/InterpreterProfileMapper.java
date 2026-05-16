package com.qinhu.oasis.interpreter.mapper;

import com.qinhu.oasis.interpreter.dto.InterpreterVO;
import com.qinhu.oasis.interpreter.entity.InterpreterProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 译员档案数据访问层（MyBatis Mapper），对应 mapper/interpreter/InterpreterProfileMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface InterpreterProfileMapper {

    /**
     * 插入译员档案，自动回填主键 ID
     *
     * @param profile 档案实体
     * @return 受影响行数
     */
    int insert(InterpreterProfile profile);

    /**
     * 根据 ID 查询档案
     *
     * @param id 档案 ID
     * @return 档案实体，不存在时返回 null
     */
    InterpreterProfile selectById(Long id);

    /**
     * 根据用户 ID 查询档案（一个用户只能有一份）
     *
     * @param userId 用户 ID
     * @return 档案实体，不存在时返回 null
     */
    InterpreterProfile selectByUserId(Long userId);

    /**
     * 分页查询已通过审核的译员列表（对外展示）
     *
     * @param offset 分页偏移量
     * @param size   每页条数
     * @return 译员 VO 列表
     */
    List<InterpreterVO> selectPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计已通过审核的译员总数
     *
     * @return 总数
     */
    long countPage();

    /**
     * 管理员分页查询所有译员档案（可按状态筛选）
     *
     * @param status 状态筛选（null 则查全部）
     * @param offset 分页偏移量
     * @param size   每页条数
     * @return 译员 VO 列表
     */
    List<InterpreterVO> selectAdminPage(@Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    /**
     * 统计管理员视图下符合条件的档案总数
     *
     * @param status 状态筛选（null 则统计全部）
     * @return 总数
     */
    long countAdminPage(@Param("status") Integer status);

    /**
     * 更新档案审核状态与拒绝原因
     *
     * @param id           档案 ID
     * @param status       目标状态（参见 InterpreterStatus）
     * @param rejectReason 拒绝原因（通过时传 null）
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("rejectReason") String rejectReason);

    /**
     * 根据ID列表查询译员档案（用于收藏功能）
     *
     * @param ids 档案ID列表
     * @return 译员档案列表
     */
    List<InterpreterProfile> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 更新译员档案（用于申请者修改待审核申请）
     *
     * @param profile 档案实体（必须包含 id）
     * @return 影响行数
     */
    int updateById(InterpreterProfile profile);

    /**
     * 更新译员评分（评论提交后自动刷新）
     *
     * @param id     档案ID
     * @param rating 新评分
     */
    void updateRating(@Param("id") Long id, @Param("rating") java.math.BigDecimal rating);

    /** 查询所有有证书的译员档案（用于图片清理） */
    List<InterpreterProfile> selectAllForCleanup();
}
