package io.homeassistant.companion.android.common.data

import android.util.Log
import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

// Use cookies collected by webkit in the okhttp client.
//
// This can be useful for HomeAssistant setups that require cookies
// for authentication.
//
// By default okhttp doesn't handle cookies at all -- so here we use
// the builtin webkit CookieManager to handle policy and
// persistence. Any cookies that are set during the onboarding/login
// workflow are persisted and can be used for future API calls.

class CookieJarCookieManagerShim : CookieJar {

    companion object {
        private const val TAG = "CookieJarCookieManager"
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        try {
            val cookies: String = CookieManager.getInstance()
                ?.getCookie(url.toString()) ?: return emptyList()
            return cookies.split("; ").map {
                Cookie.parse(url, it)
            }.filterNotNull()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get cookie manager", e)
            return emptyList()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        try {
            val manager: CookieManager = CookieManager.getInstance() ?: return
            for (cookie in cookies) {
                manager.setCookie(url.toString(), cookie.toString(), null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get cookie manager", e)
            return
        }
    }
}
