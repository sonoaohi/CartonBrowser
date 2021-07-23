package com.example.cartonbrowser

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.cartonbrowser.ui.theme.CartonBrowserTheme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.WebExtensionController


class MainActivity : ComponentActivity() {
    var requestedDownloadUri : Uri? = null

    override fun onBackPressed() {

        // Don't call super.onBackPressed()
        if (Globals.keyGlobalVariablesInitialized) {
            if (!Globals.dialogOnScreen) {
                if (Globals.currentScreen == ScreenEnum.MainScreen) {
                    Globals.currentGeckoSessionWrapped.session.goBack()
                } else if (Globals.currentScreen in arrayOf(
                        ScreenEnum.ErrorScreen,
                        ScreenEnum.PortalScreen
                    )
                ) {
                    // finishAfterTransition()
                } else {
                    goToMainScreenOrLoadErrorScreenOrPortalScreen()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Globals.keyGlobalVariablesInitialized) {
            Globals.downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            Globals.sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
            Globals.isAllowingJavascriptByDefault = Globals.sharedPreferences.
                getBoolean("isAllowingJavascriptByDefault", true)
            Globals.isAllowingImagesByDefault = Globals.sharedPreferences.
                getBoolean("isAllowingImagesByDefault", true)
            Globals.isAllowingMediaByDefault = Globals.sharedPreferences.
                getBoolean("isAllowingMediaByDefault", true)
            Globals.isAllowingWebfontsByDefault = Globals.sharedPreferences.
                getBoolean("isAllowingWebfontsByDefault", true)
            Globals.defaultHttpsOnlyMode = Globals.sharedPreferences.getInt(
                "defaultHttpsOnlyMode", GeckoRuntimeSettings.HTTPS_ONLY)
            Globals.searchEngineConfigMap =
                Globals.sharedPreferences.getString(
                    "searchEngineConfigJsonString", defaultSearchEngineJsonString
                )!!.toSearchEngineConfigMap()
            Globals.defaultSearchEngine = Globals.sharedPreferences.getString(
                "defaultSearchEngine", Globals.searchEngineConfigMap.keys.first())!!

            Globals.bookmarkConfigSet =
                Globals.sharedPreferences.getString(
                    "bookmarkConfigJsonString", JSONObject().toString()
                )!!.toBookmarkConfigSet()

            LocalizedStringsProvider.setCurrentLangTagWithSystemOrFallbackConfig()
            Globals.currentStringsStore = LocalizedStringsProvider.getCurrentStringsStore()

            Globals.geckoRuntime = createGeckoRuntime()
            Globals.currentGeckoSessionWrapped = newGeckoSessionWrapped(firstSession = true)
            Globals.currentScreen = ScreenEnum.PortalScreen

            Globals.mediaSwitchWebExtensionMessageDelegate = CustomWebExtensionMessageDelegate()
            Globals.mediaSwitchWebExtensionPortDelegate = CustomWebExtensionPortDelegate()

            Globals.geckoRuntime.webExtensionController
                .ensureBuiltIn("resource://android/assets/media-switch/", "media-switch@example.com")
                .accept( // Register message delegate for background script
                    { extension ->
                        extension?.let {
                            Globals.geckoRuntime.webExtensionController.update(it)
                            Globals.geckoRuntime.webExtensionController.enable(it, WebExtensionController.EnableSource.APP)
                            extension.setMessageDelegate(Globals.mediaSwitchWebExtensionMessageDelegate,
                                "browser")
                        }
                })



            Globals.keyGlobalVariablesInitialized = true
        }
        setContent {
            CartonBrowserTheme {
                Surface {
                    // A surface container using the 'background' color from the theme
                    //MainScreen()
                    ScreenHost(Globals.currentScreen)
                }
            }
        }
    }
    private fun createGeckoRuntime() : GeckoRuntime {
        val geckoRuntimeSetting = GeckoRuntimeSettings.Builder()
                                    .aboutConfigEnabled(true)
                                    .loginAutofillEnabled(false)
                                    .remoteDebuggingEnabled(false)
                                    .automaticFontSizeAdjustment(true)
                                    .allowInsecureConnections(Globals.defaultHttpsOnlyMode)
                                    .build()
        return GeckoRuntime.create(applicationContext, geckoRuntimeSetting)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
    fun onNewDownloadEvent(event: NewDownloadEvent) {
        if (Build.VERSION.SDK_INT < 29) {
            when(checkSelfPermission(WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_GRANTED -> {
                    event.downloadUri.let { responseUri ->
                        DownloadManager.Request(responseUri).let { request ->
                            // TODO: add cookie header, useragent header,
                            //  and etc from initiating site
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                responseUri.lastPathSegment
                            )
                            request.setTitle(responseUri.lastPathSegment)
                            Globals.downloadManager.enqueue(request)
                        }
                    }
                }
                else -> {
                    requestedDownloadUri = event.downloadUri
                    requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 1072)
                }
            }
        } else {
            event.downloadUri.let { responseUri ->
                DownloadManager.Request(responseUri).let { request ->
                    // TODO: add cookie header, useragent header,
                    //  and etc from initiating site
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        responseUri.lastPathSegment
                    )
                    request.setTitle(responseUri.lastPathSegment)
                    Globals.downloadManager.enqueue(request)
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1072 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestedDownloadUri?.let { responseUri ->
                        DownloadManager.Request(responseUri).let { request ->
                            // TODO: add cookie header, useragent header,
                            //  and etc from initiating site
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                responseUri.lastPathSegment
                            )
                            request.setTitle(responseUri.lastPathSegment)
                            Globals.downloadManager.enqueue(request)
                        }
                    }
                    requestedDownloadUri = null
                } else {

                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    init {
        EventBus.getDefault().register(this)
    }
}

@Composable
fun ScreenHost(currentScreen: ScreenEnum) {
    // By changing states of a composable,
    // we can make it recompose
    var _currentScreen by rememberSaveable { mutableStateOf(currentScreen) }
    Column {
        Box(Modifier.weight(1f)) {
            Column {
                if (_currentScreen == ScreenEnum.MainScreen) {
                    ComposableGeckoView(Globals.currentGeckoSessionWrapped.sessionString)
                } else if (_currentScreen == ScreenEnum.ErrorScreen) {
                    LoadErrorScreenComposable(Globals.currentGeckoSessionWrapped.sessionErrorMessage)
                } else if (_currentScreen == ScreenEnum.PortalScreen) {
                    PortalScreen(Modifier.weight(1f))
                } else {
                    Box(Modifier.weight(1f)) {
                        Column {
                            when (_currentScreen) {
                                ScreenEnum.TabsListScreen -> {
                                    TabsListScreen()
                                }
                                ScreenEnum.OptionScreen -> {
                                    OptionsComposable()
                                }
                                ScreenEnum.CurrentTabScreen -> {
                                    CurrentTabScreen()
                                }
                                else -> {

                                }
                            }
                        }
                    }
                    BackScreenTabRow()
                }
            }
        }
        AddressBar()
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onCurrentScreenChangeEvent(event: CurrentScreenChangeEvent) {
            _currentScreen = event.newScreen
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}
