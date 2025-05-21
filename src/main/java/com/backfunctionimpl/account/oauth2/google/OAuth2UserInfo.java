package com.backfunctionimpl.account.oauth2.google;

public interface OAuth2UserInfo {
    String getEmail();
    String getName();
    String getProvider();
    String getProviderId();
}
