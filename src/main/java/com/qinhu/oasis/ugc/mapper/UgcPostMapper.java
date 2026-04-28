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
     * 分页查询已发布的攻略列表（LEFT JOIN sys_user 获取作者信息）
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
     * 统计已发布的攻略总数
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
    PostDetailVO selectDetailById(Long id);

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
    int incrementViewCount(Long id);

    /**
     * 点赞数 +1
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int incrementLikeCount(Long id);

    /**
     * 点赞数 -1（使用 GREATEST 防止出现负数）
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int decrementLikeCount(Long id);

    /**
     * 评论数 +1
     *
     * @param id 攻略 ID
     * @return 影响行数
     */
    int incrementCommentCount(Long id);
}
