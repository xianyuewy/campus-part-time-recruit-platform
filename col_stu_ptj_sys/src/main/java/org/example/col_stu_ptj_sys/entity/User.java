package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends com.colstuptjsys.entity.BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称，对外展示时优先于登录名（企业另优先企业资料中的企业名称）
     */
    private String nickname;

    /**
     * 真实姓名；企业可填企业名称/法人，用于补充与核对，默认外显见 UserDisplayService
     */
    private String realName;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 用户角色
     */
    @TableField("`role`")
    private UserRole role;

    /**
     * 认证状态
     */
    private AuthStatus authStatus = AuthStatus.UNAUTH;

    /**
     * 学信网验证报告图片路径
     */
    private String idCard;

    /**
     * 审核备注
     */
    private String authRemark;

    /**
     * 认证审核时间
     */
    private LocalDateTime authTime;

    /**
     * 账号是否可用（停用后无法登录）
     */
    private Boolean accountEnabled = Boolean.TRUE;

    /**
     * 令牌版本：修改密码后递增，使旧 Access/Refresh Token 全部失效
     */
    private Integer tokenVersion;
}