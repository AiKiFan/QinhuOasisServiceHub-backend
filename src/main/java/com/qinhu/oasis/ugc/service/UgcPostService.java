package com.qinhu.oasis.ugc.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.ugc.dto.CreatePostReq;
import com.qinhu.oasis.ugc.dto.PostDetailVO;
import com.qinhu.oasis.ugc.dto.PostListVO;

/**
 * 攻略/动态业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface UgcPostService {

    /**
     * 分页查询已发布的攻略/动态列表
     *
     * @param postType 类型筛选（null 不过滤）
     * @param page     页码（从1开始）
     * @param size     每页条数
     * @return 分页结果
     */
    PageResult<PostListVO> listPosts(Integer postType, int page, int size);

    /**
     * 查询攻略/动态详情（同时触发浏览量 +1）
     *
     * @param postId 攻略 ID
     * @return 详情 VO
     */
    PostDetailVO getPostDetail(Long postId);

    /**
     * 发布攻略/动态（需登录）
     * <p>游客动态（type=3）直接发布；游客攻略（type=2）进入审核状态</p>
     *
     * @param req    请求参数
     * @param userId 当前登录用户 ID
     * @return 新建帖子的详情 VO
     */
    PostDetailVO createPost(CreatePostReq req, Long userId);

    /**
     * 点赞/取消点赞（同一接口，调用时自动切换）
     *
     * @param postId 攻略 ID
     * @param userId 当前登录用户 ID
     * @return true=已点赞，false=已取消点赞
     */
    boolean toggleLike(Long postId, Long userId);
}
