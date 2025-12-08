package com.litongjava.tio.boot.admin.sql;

public interface AppUsersSql {

  String findById = """
        SELECT
        id, email, phone, email_verified, updated_profile,
        display_name, bio, photo_url, background_url, phone_number,
        disabled, birthday, coin, invited_by_user_id, "of", platform,
        third_platform_url, school_id, user_type, provider_data, mfa_info,
        metadata, user_info, google_id, google_info, facebook_id, facebook_info,
        twitter_id, twitter_info, github_id, github_info, wechat_id, wechat_info,
        qq_id, qq_info, weibo_id, weibo_info, remark, creator,
        create_time, updater, update_time, deleted, tenant_id
      FROM app_users
      WHERE ID = ? AND deleted = 0;
            """;
}