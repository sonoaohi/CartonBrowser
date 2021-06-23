package com.example.cartonbrowser

import android.app.DownloadManager
import android.content.SharedPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.mozilla.geckoview.*
import org.mozilla.geckoview.GeckoRuntimeSettings.HTTPS_ONLY
import org.mozilla.geckoview.GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW
import java.net.URI

object Globals {
    lateinit var geckoRuntime: GeckoRuntime

    lateinit var currentGeckoSessionWrapped: GeckoSessionWrapped
    var keyGlobalVariablesInitialized: Boolean = false
    var currentScreen: ScreenEnum = ScreenEnum.MainScreen
    // Is there a way to make the list distinct?
    val geckoSessionsList: MutableList<GeckoSessionWrapped> = mutableListOf()
    lateinit var sharedPreferences: SharedPreferences
    var isAllowingJavascriptByDefault = true
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putBoolean("isAllowingJavascriptByDefault", value)
                apply()
            }
        }
    var isAllowingImagesByDefault = true
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putBoolean("isAllowingImagesByDefault", value)
                apply()
            }
        }
    var isAllowingMediaByDefault = true
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putBoolean("isAllowingMediaByDefault", value)
                apply()
            }
        }
    var isAllowingWebfontsByDefault = true
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putBoolean("isAllowingWebfontsByDefault", value)
                apply()
            }
        }
    var defaultHttpsOnlyMode = HTTPS_ONLY
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putInt("defaultHttpsOnlyMode", value)
                apply()
            }
        }
    var searchEngineConfigMap: MutableMap<String, String> = defaultSearchEngineJsonString.toSearchEngineConfigMap()
        set(value) {
            if (value.isEmpty()) {
                field = defaultSearchEngineJsonString.toSearchEngineConfigMap()
            } else {
                field = value
            }
            with (sharedPreferences.edit()) {
                this.putString("searchEngineConfigJsonString", value.toSearchEngineConfigJsonString())
                apply()
            }
        }
    var defaultSearchEngine: String = searchEngineConfigMap.keys.first()
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putString("defaultSearchEngine", value)
                apply()
            }
        }
    var bookmarkConfigSet: MutableSet<Bookmark> = mutableSetOf()
        set(value) {
            field = value
            with (sharedPreferences.edit()) {
                this.putString("bookmarkConfigJsonString", value.toBookmarkConfigJsonString())
                apply()
            }
        }
    lateinit var mediaSwitchWebExtensionPortDelegate : CustomWebExtensionPortDelegate
    lateinit var mediaSwitchWebExtensionMessageDelegate : CustomWebExtensionMessageDelegate
    var mediaSwitchWebExtensionPort: WebExtension.Port? = null
    lateinit var downloadManager: DownloadManager
    var dialogOnScreen: Boolean = false
    lateinit var currentStringsStore: LangStringsStore
}

class GeckoSessionWrapped(sessionUri: String,
                          allowJavascript: Boolean = Globals.isAllowingJavascriptByDefault,
                          allowImages: Boolean = Globals.isAllowingImagesByDefault,
                          allowMedia: Boolean = Globals.isAllowingMediaByDefault,
                          allowWebfonts: Boolean = Globals.isAllowingWebfontsByDefault) {
    var sessionUri: String = sessionUri
    var sessionTitle: String = ""
    var sessionErrorMessage: String? = null
    var sessionHistory: GeckoSession.HistoryDelegate.HistoryList? = null

    // TODO: Wrap GeckoSession.HistoryDelegate.HistoryList as tabHistory
    //       to avoid losing tab history when load errors occur

    var isActive: Boolean = true
        set(value: Boolean) {
            field = value
            session.setFocused(value)
            session.setActive(value)
        }
    var isAllowingJavascript: Boolean = allowJavascript
        set(value) {
            field = value
            session.settings.allowJavascript = value
            session.reload()
        }
    var isAllowingImages: Boolean = allowImages
        // TODO: update this when global setting changes and session host is not in exception list
        set(value) {
            if (value != Globals.isAllowingImagesByDefault) {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Add this host to temporary images blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            } else {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Remove this host from temporary images blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            }
        }
    var isAllowingMedia: Boolean = allowMedia
        // TODO: update this when global setting changes and session host is not in exception list
        set(value) {
            if (value != Globals.isAllowingMediaByDefault) {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Add this host to temporary media blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            } else {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Remove this host from temporary media blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            }
        }
    var isAllowingWebfonts: Boolean = allowWebfonts
        // TODO: update this when global setting changes and session host is not in exception list
        set(value) {
            if (value != Globals.isAllowingWebfontsByDefault) {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Add this host to temporary webfonts blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            } else {
                Globals.mediaSwitchWebExtensionPort?.let {
                    it.postMessage(
                        JSONObject()
                            .put("message", "Remove this host from temporary webfonts blocking exceptions")
                            .put("hostException", getURLOrigin(sessionUri))
                    )
                    field = value
                }
            }
        }


    // session.toString() does not guarantee uniqueness, is there an alternative?

    private fun createGeckoSession(): GeckoSession {
        val geckoSessionSettings = GeckoSessionSettings.Builder()
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
            .allowJavascript(isAllowingJavascript)
            .build()
        return GeckoSession(geckoSessionSettings)
    }

    val session: GeckoSession = createGeckoSession()
    val sessionString: String = session.toString()

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onHistoryStateChangeEvent(event: HistoryStateChangeEvent) {
        if (event.sessionString == sessionString) {
            this.sessionHistory = event.historyList
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onSessionUriChangeEvent(event: SessionUriChangeEvent) {
        if (event.sessionString == sessionString) {
            this.sessionUri = event.newSessionUri
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onSessionTitleChangeEvent(event: SessionTitleChangeEvent) {
        if (event.sessionString == sessionString) {
            this.sessionTitle = event.newSessionTitle
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onMediaBlockingHostExceptionResultEvent(event: MediaBlockingHostExceptionResultEvent) {
        // There will be a side-effect that all tabs could be affected upon checking
        if (event.host == getURLOrigin(sessionUri)) {
            if (event.imageResult) {
                if (event.imageListType == MediaBlockingHostExceptionListType.WHITELIST) {
                    isAllowingImages = true
                } else if (event.imageListType == MediaBlockingHostExceptionListType.BLACKLIST) {
                    isAllowingImages = false
                }
            }
            if (event.mediaResult) {
                if (event.mediaListType == MediaBlockingHostExceptionListType.WHITELIST) {
                    isAllowingMedia = true
                } else if (event.mediaListType == MediaBlockingHostExceptionListType.BLACKLIST) {
                    isAllowingMedia = false
                }
            }
            if (event.fontResult) {
                if (event.fontListType == MediaBlockingHostExceptionListType.WHITELIST) {
                    isAllowingWebfonts = true
                } else if (event.fontListType == MediaBlockingHostExceptionListType.BLACKLIST) {
                    isAllowingWebfonts = false
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onLoadErrorEvent(event: LoadErrorEvent) {
        if (event.sessionString == sessionString) {
            sessionErrorMessage = when(event.loadError.code) {
                WebRequestError.ERROR_UNKNOWN -> "ERROR_UNKNOWN"
                WebRequestError.ERROR_SECURITY_SSL -> "ERROR_SECURITY_SSL"
                WebRequestError.ERROR_SECURITY_BAD_CERT -> "ERROR_SECURITY_BAD_CERT"
                WebRequestError.ERROR_NET_INTERRUPT -> "ERROR_NET_INTERRUPT"
                WebRequestError.ERROR_NET_TIMEOUT -> "ERROR_NET_TIMEOUT"
                WebRequestError.ERROR_CONNECTION_REFUSED -> "ERROR_CONNECTION_REFUSED"
                WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE -> "ERROR_UNKNOWN_SOCKET_TYPE"
                WebRequestError.ERROR_REDIRECT_LOOP -> "ERROR_REDIRECT_LOOP"
                WebRequestError.ERROR_OFFLINE -> "ERROR_OFFLINE"
                WebRequestError.ERROR_PORT_BLOCKED -> "ERROR_PORT_BLOCKED"
                WebRequestError.ERROR_NET_RESET -> "ERROR_NET_RESET"
                WebRequestError.ERROR_UNSAFE_CONTENT_TYPE -> "ERROR_UNSAFE_CONTENT_TYPE"
                WebRequestError.ERROR_CORRUPTED_CONTENT -> "ERROR_CORRUPTED_CONTENT"
                WebRequestError.ERROR_CONTENT_CRASHED -> "ERROR_CONTENT_CRASHED"
                WebRequestError.ERROR_INVALID_CONTENT_ENCODING -> "ERROR_INVALID_CONTENT_ENCODING"
                WebRequestError.ERROR_UNKNOWN_HOST -> "ERROR_UNKNOWN_HOST"
                WebRequestError.ERROR_MALFORMED_URI -> "ERROR_MALFORMED_URI"
                WebRequestError.ERROR_UNKNOWN_PROTOCOL -> "ERROR_UNKNOWN_PROTOCOL"
                WebRequestError.ERROR_FILE_NOT_FOUND -> "ERROR_FILE_NOT_FOUND"
                WebRequestError.ERROR_FILE_ACCESS_DENIED -> "ERROR_FILE_ACCESS_DENIED"
                WebRequestError.ERROR_PROXY_CONNECTION_REFUSED -> "ERROR_PROXY_CONNECTION_REFUSED"
                WebRequestError.ERROR_UNKNOWN_PROXY_HOST -> "ERROR_UNKNOWN_PROXY_HOST"
                WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI -> "ERROR_SAFEBROWSING_MALWARE_URI"
                WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI -> "ERROR_SAFEBROWSING_UNWANTED_URI"
                WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI -> "ERROR_SAFEBROWSING_HARMFUL_URI"
                WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI -> "ERROR_SAFEBROWSING_PHISHING_URI"
                else -> "UNKNOWN_ERROR_CODE"
            }
            if (this == Globals.currentGeckoSessionWrapped) {
                EventBus.getDefault().post(
                    ShowErrorPageEvent(this.sessionString, this.sessionErrorMessage!!)
                )
                changeCurrentScreen(ScreenEnum.ErrorScreen)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onSessionCrashEvent(event: SessionCrashEvent) {
        if (event.sessionString == sessionString) {
            sessionErrorMessage = "CRASHED"
            if (this == Globals.currentGeckoSessionWrapped) {
                EventBus.getDefault().post(
                    ShowErrorPageEvent(this.sessionString, this.sessionErrorMessage!!)
                )
                changeCurrentScreen(ScreenEnum.ErrorScreen)
            }
        }
    }

    fun loadUri(uri: String) {
        sessionUri = uri
        sessionErrorMessage = null
        session.loadUri(uri)
    }

    init {
        session.open(Globals.geckoRuntime)
        session.loadUri(sessionUri)
        session.navigationDelegate = CustomNavigationDelegate()
        session.contentDelegate = CustomContentDelegate()
        session.historyDelegate = CustomHistoryDelegate()
        session.promptDelegate = CustomPromptDelegate()

        EventBus.getDefault().register(this)
    }
}

class CustomNavigationDelegate : GeckoSession.NavigationDelegate {
    override fun onNewSession(session: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
        GeckoResult<GeckoSession>().let { geckoResult ->
            newGeckoSessionWrapped(sessionUri = uri).let { sessionWrapped ->
                changeCurrentSession(sessionWrapped)
                geckoResult.complete(sessionWrapped.session)
            }
            return geckoResult
        }
    }

    override fun onLoadRequest(
        session: GeckoSession,
        request: GeckoSession.NavigationDelegate.LoadRequest
    ): GeckoResult<AllowOrDeny>? {
        request.uri.let {
            Globals.mediaSwitchWebExtensionPort?.postMessage(
                JSONObject()
                    .put("message", "Request host exception check")
                    .put("hostException", getURLOrigin(it))
            )
            EventBus.getDefault().post(SessionUriChangeEvent(session.toString(), it))
        }

        if (request.target == TARGET_WINDOW_NEW) {
            changeCurrentSession(newGeckoSessionWrapped(sessionUri = request.uri))
            GeckoResult<AllowOrDeny>().let {
                // Returning DENY prevents default loading
                it.complete(AllowOrDeny.DENY)
                return it
            }
        }

        return super.onLoadRequest(session, request)
    }

//    override fun onLocationChange(session: GeckoSession, url: String?) {
//        url?.let {
//            EventBus.getDefault().post(SessionUriChangeEvent(session.toString(), it))
//
//        }
//        super.onLocationChange(session, url)
//    }

    override fun onLoadError(
        session: GeckoSession,
        uri: String?,
        error: WebRequestError
    ): GeckoResult<String>? {
        EventBus.getDefault().post(LoadErrorEvent(session.toString(), uri, error))
        return super.onLoadError(session, uri, error)
    }


}

class CustomContentDelegate : GeckoSession.ContentDelegate {
    override fun onTitleChange(session: GeckoSession, title: String?) {
        title?.let {
            EventBus.getDefault().post(SessionTitleChangeEvent(session.toString(), it))
        }
        super.onTitleChange(session, title)
    }

    override fun onContextMenu(
        session: GeckoSession,
        screenX: Int,
        screenY: Int,
        element: GeckoSession.ContentDelegate.ContextElement
    ) {
        EventBus.getDefault().post(ContextMenuEvent(element))
        super.onContextMenu(session, screenX, screenY, element)
    }

    override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
        response.body?.close()
        if (response.uri.startsWith("https://") || response.uri.startsWith("http://")) {
            EventBus.getDefault().post(DownloadPromptEvent(session.toString(), response))
        }
    }

    override fun onCrash(session: GeckoSession) {
        EventBus.getDefault().post(SessionCrashEvent(session.toString()))
        super.onCrash(session)
    }
}

class CustomHistoryDelegate : GeckoSession.HistoryDelegate {
    override fun onHistoryStateChange(
        session: GeckoSession,
        historyList: GeckoSession.HistoryDelegate.HistoryList
    ) {
        val sessionString = session.toString()
        try {
            val itemTitle = historyList[historyList.currentIndex].title
            val itemUri = historyList[historyList.currentIndex].uri

            EventBus.getDefault().post(HistoryStateChangeEvent(sessionString, historyList))
            EventBus.getDefault().post(SessionTitleChangeEvent(sessionString, itemTitle))
            EventBus.getDefault().post(SessionUriChangeEvent(sessionString, itemUri))
        } catch (e: Exception) {

        }


        super.onHistoryStateChange(session, historyList)
    }
}

class CustomPromptDelegate : GeckoSession.PromptDelegate {
    override fun onAlertPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.AlertPrompt
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
        EventBus.getDefault().post(AlertPromptEvent(session.toString(),
            prompt.title ?: "", prompt.message ?: ""))
        return super.onAlertPrompt(session, prompt)
    }

    override fun onButtonPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.ButtonPrompt
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
        val _sessionString = session.toString()
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse>().let { geckoResult ->
            EventBus.getDefault().post(ButtonPromptEvent(_sessionString, prompt, geckoResult))
            return geckoResult
        }
    }

    override fun onChoicePrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.ChoicePrompt
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
        val _sessionString = session.toString()
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse>().let { geckoResult ->
            EventBus.getDefault().post(ChoicePromptEvent(_sessionString, prompt, geckoResult))
            return geckoResult
        }
    }

    override fun onTextPrompt(
        session: GeckoSession,
        prompt: GeckoSession.PromptDelegate.TextPrompt
    ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
        val _sessionString = session.toString()
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse>().let { geckoResult ->
            EventBus.getDefault().post(TextPromptEvent(_sessionString, prompt, geckoResult))
            return geckoResult
        }
    }
}

// Test pages at resource://android/assets/test-pages/
fun newGeckoSessionWrapped(sessionUri: String = "about:blank",
                           firstSession: Boolean = false): GeckoSessionWrapped {
    return GeckoSessionWrapped(sessionUri).also { newSessionWrapped ->
        Globals.geckoSessionsList.let {
            if (firstSession) {
                // Use it when Globals.currentGeckoSessionWrapped is not initialized
                it.add(newSessionWrapped)
            } else {
                it.add(it.indexOf(Globals.currentGeckoSessionWrapped) + 1, newSessionWrapped)
            }
        }
    }
}

fun changeCurrentScreen(newScreen: ScreenEnum) {
    Globals.currentScreen = newScreen
    EventBus.getDefault().post(CurrentScreenChangeEvent(newScreen))
}

fun changeCurrentSession(newSessionWrapped: GeckoSessionWrapped) {
    Globals.currentGeckoSessionWrapped.isActive = false
    if (!newSessionWrapped.isActive) newSessionWrapped.isActive = true
    Globals.currentGeckoSessionWrapped = newSessionWrapped
    EventBus.getDefault().post(CurrentSessionWrappedChangeEvent(newSessionWrapped))
}

fun goToMainScreenOrLoadErrorScreenOrPortalScreen() {
    Globals.currentGeckoSessionWrapped.let {
        if (it.sessionErrorMessage != null) {
            EventBus.getDefault().post(
                ShowErrorPageEvent(it.sessionString, it.sessionErrorMessage!!)
            )
            changeCurrentScreen(ScreenEnum.ErrorScreen)
        } else if (it.sessionUri == "about:blank") {
            changeCurrentScreen(ScreenEnum.PortalScreen)
        } else {
            changeCurrentScreen(ScreenEnum.MainScreen)
        }
    }
}

fun setValueInSearchEngineConfigMap(key: String, value: String) {
    Globals.searchEngineConfigMap.set(key, value)
    with (Globals.sharedPreferences.edit()) {
        this.putString("searchEngineConfigJsonString",
            Globals.searchEngineConfigMap.toSearchEngineConfigJsonString())
        apply()
    }
}

fun delValueInSearchEngineConfigMap(key: String) {
    Globals.searchEngineConfigMap.remove(key)
    if (Globals.searchEngineConfigMap.isEmpty()) {
        Globals.searchEngineConfigMap = defaultSearchEngineJsonString.toSearchEngineConfigMap()
    }
    with (Globals.sharedPreferences.edit()) {
        this.putString("searchEngineConfigJsonString",
            Globals.searchEngineConfigMap.toSearchEngineConfigJsonString())
        apply()
    }
}

fun addValueInBookmarkConfigList(newTitle: String, newUri: String) {
    Globals.bookmarkConfigSet.add(Bookmark(newTitle, newUri))
    with (Globals.sharedPreferences.edit()) {
        this.putString("bookmarkConfigJsonString",
            Globals.bookmarkConfigSet.toBookmarkConfigJsonString())
        apply()
    }
}

fun replaceValueInBookmarkConfigList(originalTitle: String, originalUri: String, newTitle: String, newUri: String) {
    Globals.bookmarkConfigSet.remove(Bookmark(originalTitle, originalUri))
    Globals.bookmarkConfigSet.add(Bookmark(newTitle, newUri))
    with (Globals.sharedPreferences.edit()) {
        this.putString("bookmarkConfigJsonString",
            Globals.bookmarkConfigSet.toBookmarkConfigJsonString())
        apply()
    }
}

fun delValueInBookmarkConfigList(title: String, uri: String) {
    Globals.bookmarkConfigSet.remove(Bookmark(title, uri))
    with (Globals.sharedPreferences.edit()) {
        this.putString("bookmarkConfigJsonString",
            Globals.bookmarkConfigSet.toBookmarkConfigJsonString())
        apply()
    }
}

fun getURLOrigin(uri: String): String {
    try {
        URI(uri).let {
            return if (it.port != -1) {
                it.scheme + "://" + it.host + ":" + it.port
            } else {
                it.scheme + "://" + it.host
            }
        }
    } catch (e: Exception) {
        // TODO: remove this temporary hack
        return "NULL"
    }
}