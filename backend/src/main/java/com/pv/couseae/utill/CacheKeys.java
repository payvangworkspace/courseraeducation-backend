package com.pv.couseae.utill;

/**
 * Central registry for Redis cache keys.
 * Both UserController and UserServiceImpl MUST reference these — never inline literals —
 * so read/write paths can never drift onto different keys.
 *
 * Bump the :vN suffix whenever the serializer/typing format changes so stale
 * entries MISS (→ DB reload) instead of throwing a deserialization error.
 */
public final class CacheKeys {

    private CacheKeys() {} // no instances

    public static final String MERCHANTS_MERCHANTMODEL = "AllMerchants:v2";
    public static final String MERCHANTS_USERLIST      = "MerchantsUserListModel:v2";

    /** Per-user cache key. Kept as a method because it's parameterised by userId. */
    public static String userKey(String userId) {
        return "user:v1:" + userId;
    }
}