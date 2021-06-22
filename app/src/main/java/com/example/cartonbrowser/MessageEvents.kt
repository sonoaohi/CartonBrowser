package com.example.cartonbrowser

import android.net.Uri
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse

data class SessionUriChangeEvent(val sessionString: String, val newSessionUri: String)

data class SessionTitleChangeEvent(val sessionString: String, val newSessionTitle: String)

data class CurrentScreenChangeEvent(val newScreen: ScreenEnum)

data class CurrentSessionWrappedChangeEvent(val newCurrentSessionWrapped: GeckoSessionWrapped)

data class ContextMenuEvent(val contextElement: GeckoSession.ContentDelegate.ContextElement)

data class LoadErrorEvent(val sessionString: String, val uri: String?, val loadError: WebRequestError)

data class ShowErrorPageEvent(val sessionString: String, val errorMessage: String)

data class HistoryStateChangeEvent(val sessionString: String, val historyList: GeckoSession.HistoryDelegate.HistoryList)

data class MediaBlockingHostExceptionResultEvent(val host: String, val imageResult: Boolean, val mediaResult: Boolean, val fontResult: Boolean, val imageListType: MediaBlockingHostExceptionListType, val mediaListType: MediaBlockingHostExceptionListType, val fontListType: MediaBlockingHostExceptionListType)

data class DownloadPromptEvent(val sessionString: String, val webResponse: WebResponse)

data class SessionCrashEvent(val sessionString: String)

data class AlertPromptEvent(val sessionString: String, val promptTitle: String, val promptMessage: String)

data class ButtonPromptEvent(val sessionString: String, val buttonPrompt: GeckoSession.PromptDelegate.ButtonPrompt, val geckoResult: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>)

data class ChoicePromptEvent(val sessionString: String, val choicePrompt: GeckoSession.PromptDelegate.ChoicePrompt, val geckoResult: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>)

data class TextPromptEvent(val sessionString: String, val textPrompt: GeckoSession.PromptDelegate.TextPrompt, val geckoResult: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>)

data class NewDownloadEvent(val downloadUri: Uri)
