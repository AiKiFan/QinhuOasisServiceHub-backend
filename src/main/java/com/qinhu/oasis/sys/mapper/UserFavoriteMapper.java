package com.qinhu.oasis.sys.mapper;

import com.qinhu.oasis.sys.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏Mapper接口
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Mapper
public interface UserFavoriteMapper {

    /**
     * 添加收藏
     */
    int insert(UserFavorite favorite);

    /**
     * 删除收藏（软删除）
     */
    int deleteByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 恢复软删除的收藏（重新收藏时复用）
     */
    int restore(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("folderId") Long folderId);

    /**
     * 查询用户是否已收藏
     */
    UserFavorite selectByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 查询用户收藏列表（按类型）
     */
    List<UserFavorite> selectByUserAndType(@Param("userId") Long userId, @Param("targetType") String targetType);

    /**
     * 查询用户所有收藏
     */
    List<UserFavorite> selectByUser(@Param("userId") Long userId);

    /**
     * 查询收藏对象ID列表（用于批量查询详情）
     */
    List<Long> selectTargetIdsByUserAndType(@Param("userId") Long userId, @Param("targetType") String targetType);
}