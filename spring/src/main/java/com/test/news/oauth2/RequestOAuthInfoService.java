package com.test.news.oauth2;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.test.news.oauth2.provider.OAuthProvider;
import com.test.news.oauth2.provider.OAuthUserInfo;

@Component
public class RequestOAuthInfoService {
    private final Map<OAuthProvider, OAuthUserInfo> userinfos;

    public RequestOAuthInfoService(List<OAuthUserInfo> userinfos) {
        this.userinfos = userinfos.stream()
                .collect(Collectors.toUnmodifiableMap(OAuthUserInfo::getProvider, Function.identity()));
    }

    public OAuthUserInfo identifyProvider(Map<String, Object> attributes, OAuthProvider provider) {
        return userinfos.get(provider).convertUserInfo(attributes);
    }
}
