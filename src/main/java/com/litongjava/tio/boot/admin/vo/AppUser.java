package com.litongjava.tio.boot.admin.vo;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
  // 用户唯一标识 (对应 uid)，表中为 VARCHAR，故采用 String 类型
  private String id;
  
  // 用户邮箱
  private String email;
  
  // 邮箱是否已验证
  private Boolean emailVerified;
  
  // 用户信息是否已经更新
  private Boolean updatedProfile;
  
  // 用户显示名称
  private String displayName;
  
  // 个人简介
  private String bio;
  
  // 用户头像 URL
  private String photoUrl;
  
  // 用户背景 URL
  private String backgroundUrl;
  
  // 用户电话号码
  private String phoneNumber;
  
  // 用户是否已禁用
  private Boolean disabled;
  
  // 用户生日
  private OffsetDateTime birthday;
  
  // 用户金币数量
  private Long coin;
  
  // 邀请人的用户 ID
  private String invitedByUserId;
  
  // 用户注册的系统
  private String of;
  
  // 用户注册来源平台
  private String platform;
  
  // 第三方平台的 URL
  private String thirdPlatformUrl;
  
  // 学校 ID (可选字段)
  private Long schoolId;
  
  // 用户类型 (如 0: 普通用户, 1: 管理员等)
  private Integer userType;
  
  // 密码加盐
  private String passwordSalt;
  
  // 密码哈希值
  private String passwordHash;
  
  // 提供信息，存储为 JSON 格式
  private String providerData;
  
  // 多因素认证信息，存储为 JSON 格式
  private String mfaInfo;
  
  // 用户元数据 (如创建时间、最后登录时间)，存储为 JSON 格式
  private String metadata;
  
  // 备注信息
  private String remark;
  
  // 创建人
  private String creator;
  
  // 创建时间
  private OffsetDateTime createTime;
  
  // 更新人
  private String updater;
  
  // 更新时间
  private OffsetDateTime updateTime;
  
  // 逻辑删除标识 (0: 未删除, 1: 已删除)
  private Short deleted;
  
  // 租户 ID (支持多租户架构)
  private Long tenantId;
}
