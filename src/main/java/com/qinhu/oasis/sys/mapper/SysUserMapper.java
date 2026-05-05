package com.qinhu.oasis.sys.mapper;

import com.qinhu.oasis.sys.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 用户 MyBatis Mapper 接口（XML 模式，对应 sys_user 表）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface SysUserMapper {

    /**
     * 根据主键查询用户（不过滤软删除，由业务层判断）
     *
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    SysUser selectById(Long id);

    /**
     * 根据用户名查询用户（用于登录/注册唯一校验）
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    SysUser selectByUsername(String username);

    /**
     * 插入新用户
     *
     * @param user 用户实体（插入后回填 id）
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新最后登录 IP 和时间
     *
     * @param id        用户 ID
     * @param ip        登录 IP
     * @param loginTime 登录时间
     * @return 影响行数
     */
    int updateLastLogin(@Param("id") Long id,
                        @Param("ip") String ip,
                        @Param("loginTime") LocalDateTime loginTime);

    /**
     * 更新用户角色（译员审核通过后升级为 STUDENT）
     *
     * @param id   用户 ID
     * @param role 目标角色（参见 UserRole）
     * @return 影响行数
     */
    int updateRole(@Param("id") Long id, @Param("role") Integer role);

    /**
     * 更新用户基本信息（昵称、邮箱、头像）
     *
     * @param user 用户实体（必须包含 id）
     * @return 影响行数
     */
    int updateById(SysUser user);
}
