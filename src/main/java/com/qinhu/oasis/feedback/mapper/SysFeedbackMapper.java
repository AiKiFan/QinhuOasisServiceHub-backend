package com.qinhu.oasis.feedback.mapper;

import com.qinhu.oasis.feedback.dto.FeedbackVO;
import com.qinhu.oasis.feedback.entity.SysFeedback;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉建议数据访问层（MyBatis Mapper），对应 mapper/feedback/SysFeedbackMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface SysFeedbackMapper {

    /**
     * 插入投诉建议记录，自动回填主键 ID
     *
     * @param feedback 实体
     * @return 受影响行数
     */
    int insert(SysFeedback feedback);

    /**
     * 根据 ID 查询投诉建议 VO
     *
     * @param id 记录 ID
     * @return VO，不存在时返回 null
     */
    FeedbackVO selectById(Long id);

    /**
     * 管理员分页查询投诉建议列表（可按状态和类型筛选）
     *
     * @param status       状态筛选（null=全部）
     * @param feedbackType 类型筛选（null=全部）
     * @param offset       分页偏移量
     * @param size         每页条数
     * @return VO 列表
     */
    List<FeedbackVO> selectAdminPage(@Param("status") Integer status,
                                     @Param("feedbackType") Integer feedbackType,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    /**
     * 统计管理员视图下符合条件的记录总数
     *
     * @param status       状态筛选（null=全部）
     * @param feedbackType 类型筛选（null=全部）
     * @return 总数
     */
    long countAdminPage(@Param("status") Integer status,
                        @Param("feedbackType") Integer feedbackType);

    /**
     * 管理员回复并更新处理状态
     *
     * @param id           记录 ID
     * @param replyContent 回复内容
     * @param status       目标状态（参见 FeedbackStatus）
     * @param handlerId    处理人管理员 ID
     * @param replyTime    回复时间
     * @return 受影响行数
     */
    int updateReply(@Param("id") Long id,
                    @Param("replyContent") String replyContent,
                    @Param("status") Integer status,
                    @Param("handlerId") Long handlerId,
                    @Param("replyTime") LocalDateTime replyTime);

    /**
     * 用户分页查询本人的投诉建议
     */
    List<FeedbackVO> selectByUserIdPage(@Param("userId") Long userId,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    /**
     * 统计用户名下投诉建议总数
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 用户更新本人投诉建议（PENDING 状态下允许）
     */
    int updateUserFields(@Param("id") Long id,
                         @Param("title") String title,
                         @Param("content") String content,
                         @Param("images") String images,
                         @Param("contact") String contact);

    /**
     * 仅更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 追加（覆盖）回复内容并设置状态（Service 层负责拼接完整内容）
     */
    int appendReplyContent(@Param("id") Long id,
                           @Param("replyContent") String replyContent,
                           @Param("status") Integer status);
}
