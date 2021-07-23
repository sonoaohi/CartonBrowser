package com.example.cartonbrowser

import android.os.LocaleList
import android.text.BidiFormatter

// Notes on accept_languages:
// Desktop Firefox uses a different mechanism to specify accept_languages than Android Firefox
// or GeckoView.
// On desktop Firefox, files named intl.properties residing in language packs are used to determine
// the accept_languages of the corresponding language settings.
// On Android Firefox or GeckoView, Gecko will use directly system settings for accept_languages.
// For the time being, it is decided that the current project shall leave this behavior untouched,
// to align with Android Firefox's behaviour.

interface LangStringsStore {
    val langTag: String
    val bidiFormatter: BidiFormatter

    val contextMenuOpenNewTabString: String
    val contextMenuCopyLinkAddressString: String

    val promptsCommonDismissButtonString: String

    val downloadPromptDownloadButtonString: String
    val downloadPromptPromptTextString: String

    val buttonPromptAllowButtonString: String
    val buttonPromptDenyButtonString: String

    val choicePromptSubmitButtonString: String

    val textPromptInputLabelString: String
    val textPromptEnterButtonString: String

    val sessionReloadButtonText: String
    val sessionReloadButtonIconDescription: String

    val imagesToggleButtonText: String
    val imagesToggleButtonIconDescription: String

    val mediaToggleButtonText: String
    val mediaToggleButtonIconDescription: String

    val webfontsToggleButtonText: String
    val webfontsToggleButtonIconDescription: String

    val javascriptToggleButtonText: String
    val javascriptToggleButtonIconDescription: String

    val sessionHistoryListItemOverlineTextCurrent: String
    val sessionHistoryListItemOverlineTextNoncurrent: String

    val tabHistoryListHeader: String
    val tabsListHeader: String
    val siteSwitchesHeader: String
    val sessionSwitchesHeader: String
    val optionsHeader: String


    // TODO: localized error messages

    val preferenceActionDoneText: String

    val preferenceCollectionGoToAddEntryDialogButtonText: String
    val preferenceCollectionCloseDialogButtonText: String

    val preferenceCollectionEntryNameFieldLabelText: String
    val preferenceCollectionEntryValueFieldLabelText: String

    val preferenceCollectionBackToCollectionFromEntryText: String
    val preferenceCollectionAddCurrentEntryText: String
    val preferenceCollectionDeleteCurrentEntryText: String
    val preferenceCollectionApplyEditOfCurrentEntryText: String

    val preferenceHTTPSOnlyModeText: String
    val preferenceEnableJavascriptByDefaultText: String
    val preferenceEnableImagesByDefaultText: String
    val preferenceEnableMediaByDefaultText: String
    val preferenceEnableWebfontsByDefaultText: String
    val preferenceClearDataExceptSiteSettingsText: String
    val preferenceEditSearchEnginesText: String
    val preferenceEditSearchEnginesEntryText: String
    val preferenceEditBookmarksText: String
    val preferenceEditBookmarksEntryText: String

    val searchBarMenuButtonIconDescription: String
    val searchBarSearchButtonIconDescription: String

    val searchBarChooseEnginePromptText: String
    val searchBarCurrentEngineFormatString: String

    val portalScreenBookmarksSectionHeaderText: String

    val newTabButtonTextString: String
    val newTabButtonIconDescription: String

    val tabItemCloseTabButtonIconDescription: String
    val tabItemCurrentTabFormatString: String
}

object LocalizedStringsProvider {
    private val registry: MutableMap<String, LangStringsStore> = mutableMapOf("en-US" to EnglishStringsStore)
    var currentLangTag: String = "en-US"
        set(value) {
            if (value in registry.keys) {
                field = value
            }
        } 

    fun register(langStringsStore: LangStringsStore) {
        registry.set(langStringsStore.langTag, langStringsStore)
    }

    fun getStringsStore(langTag: String): LangStringsStore? {
        return registry[langTag]
    }

    fun getCurrentStringsStore(): LangStringsStore {
        return registry[currentLangTag]!!
    }

    fun updateCurrentLangCode() {
        currentLangTag = LocaleList.getDefault().getFirstMatch(registry.keys.toTypedArray())?.toLanguageTag() ?: "en-US"
    }
}

object EnglishStringsStore: LangStringsStore {
    override val langTag: String = "en-US"
    override val bidiFormatter: BidiFormatter = BidiFormatter.getInstance()


    override val contextMenuOpenNewTabString = bidiFormatter.unicodeWrap("Open In New Tab")
    override val contextMenuCopyLinkAddressString = bidiFormatter.unicodeWrap("Copy Link Address")

    override val promptsCommonDismissButtonString = bidiFormatter.unicodeWrap("Dismiss")

    override val downloadPromptDownloadButtonString = bidiFormatter.unicodeWrap("Download")
    override val downloadPromptPromptTextString = bidiFormatter.unicodeWrap("Do you want to download %s ?")

    override val buttonPromptAllowButtonString = bidiFormatter.unicodeWrap("Allow")
    override val buttonPromptDenyButtonString = bidiFormatter.unicodeWrap("Deny")

    override val choicePromptSubmitButtonString = bidiFormatter.unicodeWrap("Submit")

    override val textPromptInputLabelString = bidiFormatter.unicodeWrap("Input")
    override val textPromptEnterButtonString = bidiFormatter.unicodeWrap("Enter")

    override val sessionReloadButtonText = bidiFormatter.unicodeWrap("Reload")
    override val sessionReloadButtonIconDescription = bidiFormatter.unicodeWrap("")

    override val imagesToggleButtonText = bidiFormatter.unicodeWrap("Image")
    override val imagesToggleButtonIconDescription = bidiFormatter.unicodeWrap("Image")

    override val mediaToggleButtonText = bidiFormatter.unicodeWrap("Media")
    override val mediaToggleButtonIconDescription = bidiFormatter.unicodeWrap("Media")

    override val webfontsToggleButtonText = bidiFormatter.unicodeWrap("Font")
    override val webfontsToggleButtonIconDescription = bidiFormatter.unicodeWrap("Font")

    override val javascriptToggleButtonText = bidiFormatter.unicodeWrap("Javascript")
    override val javascriptToggleButtonIconDescription = bidiFormatter.unicodeWrap("Javascript")

    override val sessionHistoryListItemOverlineTextCurrent = bidiFormatter.unicodeWrap("#%s CURRENT")
    override val sessionHistoryListItemOverlineTextNoncurrent = bidiFormatter.unicodeWrap("#%s")

    override val tabHistoryListHeader = bidiFormatter.unicodeWrap("Tab History")
    override val tabsListHeader = bidiFormatter.unicodeWrap("Tabs")
    override val siteSwitchesHeader = bidiFormatter.unicodeWrap("Site-specific Switches")
    override val sessionSwitchesHeader = bidiFormatter.unicodeWrap("Tab-specific Switches")
    override val optionsHeader = bidiFormatter.unicodeWrap("Options")

    // TODO: localized error messages

    override val preferenceActionDoneText = bidiFormatter.unicodeWrap("Done")

    override val preferenceCollectionGoToAddEntryDialogButtonText = bidiFormatter.unicodeWrap("Add")
    override val preferenceCollectionCloseDialogButtonText = bidiFormatter.unicodeWrap("Close")

    override val preferenceCollectionEntryNameFieldLabelText = bidiFormatter.unicodeWrap("Name")
    override val preferenceCollectionEntryValueFieldLabelText = bidiFormatter.unicodeWrap("Value")

    override val preferenceCollectionBackToCollectionFromEntryText = bidiFormatter.unicodeWrap("Back")
    override val preferenceCollectionAddCurrentEntryText = bidiFormatter.unicodeWrap("Add")
    override val preferenceCollectionDeleteCurrentEntryText = bidiFormatter.unicodeWrap("Delete")
    override val preferenceCollectionApplyEditOfCurrentEntryText = bidiFormatter.unicodeWrap("Apply")

    override val preferenceHTTPSOnlyModeText = bidiFormatter.unicodeWrap("HTTPS-only mode")
    override val preferenceEnableJavascriptByDefaultText = bidiFormatter.unicodeWrap("Enable javascript by default")
    override val preferenceEnableImagesByDefaultText = bidiFormatter.unicodeWrap("Enable images by default")
    override val preferenceEnableMediaByDefaultText = bidiFormatter.unicodeWrap("Enable media by default")
    override val preferenceEnableWebfontsByDefaultText = bidiFormatter.unicodeWrap("Enable webfonts by default")
    override val preferenceClearDataExceptSiteSettingsText = bidiFormatter.unicodeWrap("Clear data sans site settings")
    override val preferenceEditSearchEnginesText = bidiFormatter.unicodeWrap("Edit search engines")
    override val preferenceEditSearchEnginesEntryText = bidiFormatter.unicodeWrap("Search Engine")
    override val preferenceEditBookmarksText = bidiFormatter.unicodeWrap("Edit bookmarks")
    override val preferenceEditBookmarksEntryText = bidiFormatter.unicodeWrap("Bookmark")

    override val searchBarMenuButtonIconDescription = bidiFormatter.unicodeWrap("Menu")
    override val searchBarSearchButtonIconDescription = bidiFormatter.unicodeWrap("Search")

    override val searchBarChooseEnginePromptText = bidiFormatter.unicodeWrap("Choose search engine")
    override val searchBarCurrentEngineFormatString = bidiFormatter.unicodeWrap("%s (Current)")

    override val portalScreenBookmarksSectionHeaderText = bidiFormatter.unicodeWrap("Bookmarks")

    override val newTabButtonTextString = bidiFormatter.unicodeWrap("New Tab")
    override val newTabButtonIconDescription = bidiFormatter.unicodeWrap("New Tab")

    override val tabItemCloseTabButtonIconDescription = bidiFormatter.unicodeWrap("Close Tab")
    override val tabItemCurrentTabFormatString = bidiFormatter.unicodeWrap("#%s CURRENT")

}