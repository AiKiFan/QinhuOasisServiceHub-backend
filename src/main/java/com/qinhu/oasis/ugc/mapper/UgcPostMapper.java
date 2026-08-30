package com.qinhu.oasis.ugc.mapper;

import com.qinhu.oasis.ugc.dto.PostDetailVO;
import com.qinhu.oasis.ugc.dto.PostListVO;
import com.qinhu.oasis.ugc.entity.UgcPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 攻略/动态 MyBatis Mapper 接口（XML 模式，对应 ugc_post 表）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface UgcPostMapper {

    /**
     * 分页查询已发布的公开攻略列表（LEFT JOIN sys_user 获取作者信息）
     *
     * @param postType 攻略类型（null 则不过滤类型）
     * @param offset   偏移量
     * @param size     每页条数
     * @return 列表 VO
     */
    List<PostListVO> selectPage(@Param("postType") Integer postType,
                                @Param("offset") int offset,
                                @Param("size") int size);

    /**
     * 统计已发布的公开攻略总数
     *
     * @param postType 攻略类型（null 则不过滤）
     * @return 总条数
     */
    long countPage(@Param("postType") Integer postType);

    /**
     * 根据主键查询攻略详情（含作者信息、正文、图片）
     *
     * @param id 攻略 ID
     * @return 详情 VO，不存在时返回 null
     */
    PostDetailVO selectDetailById(@Param("id") Long id);

    /**
     * 根据主键查询攻略实体
     *
     * @param id 攻略 ID
     * @return 实体，不存在时返回 null
     */
    UgcPost selectEntityById(@Param("id") Long id);

    /**
     * 插入新攻略（useGeneratedKeys 回填 id）
     *
     * @param post 攻略实体
     * @return 影响行数
     */
    int insert(UgcPost post);

    /**
     * 浏览量 +1
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 点赞数 +1
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 点赞数 -1（使用 GREATEST 防止出现负数）
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 评论数 +1
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int incrementCommentCount(@Param("id") Long id);

    /** 查询所有有图片的 UGC 帖子（用于图片清理） */
    List<UgcPost> selectAllForCleanup();

    /** 更新 UGC 帖子（用于图片清理） */
    int updateById(UgcPost post);

    /** 更新用户本人攻略私密状态 */
    int updateIsPrivate(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("isPrivate") Integer isPrivate);

    /** 更新用户本人未发布攻略正文内容 */
    int updateByUserId(UgcPost post);

    /** 用户软删除本人攻略 */
    int softDeleteByUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 管理员软删除攻略 */
    int softDeleteByAdmin(@Param("id") Long id);

    /** 管理员更新攻略状态 */
    int adminUpdateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 查询用户本人攻略列表 */
    List<PostListVO> selectMyPosts(@Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /** 统计用户本人攻略总数 */
    long countMyPosts(@Param("userId") Long userId);

    /** 管理员查询攻略列表 */
    List<PostListVO> selectAdminPage(@Param("postType") Integer postType,
                                     @Param("status") Integer status,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    /** 管理员统计攻略总数 */
    long countAdminPage(@Param("postType") Integer postType,
                        @Param("status") Integer status);

    /** 根据 ID 批量查询攻略 */
    List<PostListVO> selectByIds(@Param("ids") List<Long> ids);
}
