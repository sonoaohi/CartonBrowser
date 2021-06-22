package com.example.cartonbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cartonbrowser.Globals.currentStringsStore
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntimeSettings.ALLOW_ALL
import org.mozilla.geckoview.GeckoRuntimeSettings.HTTPS_ONLY
import org.mozilla.geckoview.StorageController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CheckboxPreferenceComposable(preferenceText: String,
                                 preferenceInitialState: Boolean,
                                 onCheckedAction: (Boolean) -> Unit) {
    var checkboxState: Boolean by remember { mutableStateOf( preferenceInitialState ) }
    ListItem(
        text = { Text(preferenceText) },
        trailing = {
            Checkbox(
                checked = checkboxState,
                onCheckedChange = {
                    onCheckedAction(it)
                    checkboxState = it
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Transparent,
                    uncheckedColor = LocalContentColor.current,
                    checkmarkColor = LocalContentColor.current,
                    disabledColor = Color.Transparent,
                    disabledIndeterminateColor = Color.Transparent
                )
            )
        },
        modifier = Modifier.clickable(
            onClick = {
                onCheckedAction(!checkboxState)
                checkboxState = !checkboxState
            }
        )
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreferenceActionComposable(preferenceText: String, onCheckedAction: () -> Unit) {
    var showTrailing by remember { mutableStateOf(false) }
    ListItem(
        text = { Text(preferenceText) },
        modifier = Modifier.clickable(
            onClick = {
                onCheckedAction()
                showTrailing = true
        }),
        trailing = {
            if (showTrailing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //Button()
                    Text(currentStringsStore.preferenceActionDoneText)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PreferenceOpenDialogComposable(preferenceText: String, onCheckedAction: () -> Unit) {
    ListItem(
        text = { Text(preferenceText) },
        modifier = Modifier.clickable(
            onClick = onCheckedAction)
    )
}

data class CollectionEntriesEditEntry(var entryName: String, var entryValue: String)

enum class CollectionEntriesChangeAction {
    ADD,
    MODIFY,
    DELETE
}

enum class CollectionEntriesEditScreenEnum {
    LIST,
    ENTRY
}

@Composable
fun CollectionEntriesEditComposable(listPromptText: String,
                                    entryPromptText: String,
                                    collection: List<CollectionEntriesEditEntry>,
                                    onApplyEdit: (entryChangeStatus: CollectionEntriesChangeAction,
                                                  changedEntry: CollectionEntriesEditEntry,
                                                  originalEntry: CollectionEntriesEditEntry) -> Unit,
                                    onClose: () -> Unit,
                                    nameOfEntryNameField: String? = null,
                                    nameOfEntryValueField: String? = null
) {
    var dialogScreen: CollectionEntriesEditScreenEnum by
        remember { mutableStateOf(CollectionEntriesEditScreenEnum.LIST) }
    var entryChangeAction: CollectionEntriesChangeAction? by remember { mutableStateOf(null) }
    var originalEntryNameUnderEditing: String by remember { mutableStateOf("") }
    var originalEntryValueUnderEditing: String by remember { mutableStateOf("") }
    var entryNameUnderEditing: String by remember { mutableStateOf("") }
    var entryValueUnderEditing: String by remember { mutableStateOf("") }
    var _collection: List<CollectionEntriesEditEntry> by remember { mutableStateOf(collection) }

    Dialog(onDismissRequest = onClose) {
        Box(Modifier.wrapContentSize().background(Color.White)) {
            Column {
                if (dialogScreen == CollectionEntriesEditScreenEnum.LIST) {
                    Text(listPromptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp), textAlign = TextAlign.Center)
                    LazyColumn(Modifier.heightIn(max = 250.dp)) {
                        itemsIndexed(
                            items = _collection
                        ) { index, entry ->
                            TextButton(
                                onClick = {
                                    originalEntryNameUnderEditing = entry.entryName
                                    originalEntryValueUnderEditing = entry.entryValue
                                    entryNameUnderEditing = entry.entryName
                                    entryValueUnderEditing = entry.entryValue
                                    entryChangeAction = CollectionEntriesChangeAction.MODIFY
                                    dialogScreen = CollectionEntriesEditScreenEnum.ENTRY
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    "#$index: ${entry.entryName}", maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(0.8f))
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            entryChangeAction = CollectionEntriesChangeAction.ADD
                            dialogScreen = CollectionEntriesEditScreenEnum.ENTRY
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(
                            currentStringsStore.preferenceCollectionGoToAddEntryDialogButtonText, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(0.8f))
                    }
                    Divider(Modifier.fillMaxWidth(0.8f))
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Text(
                            currentStringsStore.preferenceCollectionCloseDialogButtonText, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.8f))
                    }
                } else {
                    Text(entryPromptText,
                        Modifier
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .fillMaxWidth(0.8f)
                            , textAlign = TextAlign.Center)
                    OutlinedTextField(
                        value = entryNameUnderEditing,
                        onValueChange = { entryNameUnderEditing = it },
                        label = { Text(nameOfEntryNameField ?: currentStringsStore.preferenceCollectionEntryNameFieldLabelText) },
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(0.8f)
                    )
                    OutlinedTextField(
                        value = entryValueUnderEditing,
                        onValueChange = { entryValueUnderEditing = it },
                        label = { Text(nameOfEntryValueField ?: currentStringsStore.preferenceCollectionEntryValueFieldLabelText) },
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(0.8f)
                    )
                    Row(
                        Modifier.padding(horizontal = 24.dp).fillMaxWidth(0.8f)) {
                        TextButton(
                            onClick = {
                                originalEntryNameUnderEditing = ""
                                originalEntryValueUnderEditing = ""
                                entryNameUnderEditing = ""
                                entryValueUnderEditing = ""
                                entryChangeAction = null
                                dialogScreen = CollectionEntriesEditScreenEnum.LIST
                            },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text(currentStringsStore.preferenceCollectionBackToCollectionFromEntryText)
                        }
                        if (entryChangeAction == CollectionEntriesChangeAction.ADD) {
                            TextButton(
                                onClick = {
                                    onApplyEdit(CollectionEntriesChangeAction.ADD,
                                        CollectionEntriesEditEntry(entryNameUnderEditing, entryValueUnderEditing),
                                        CollectionEntriesEditEntry(originalEntryNameUnderEditing,
                                            originalEntryValueUnderEditing))
                                    _collection = _collection.plus(CollectionEntriesEditEntry(entryNameUnderEditing, entryValueUnderEditing))
                                    originalEntryNameUnderEditing = ""
                                    originalEntryValueUnderEditing = ""
                                    entryNameUnderEditing = ""
                                    entryValueUnderEditing = ""
                                    entryChangeAction = null
                                    dialogScreen = CollectionEntriesEditScreenEnum.LIST
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(currentStringsStore.preferenceCollectionAddCurrentEntryText)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    onApplyEdit(CollectionEntriesChangeAction.DELETE,
                                        CollectionEntriesEditEntry(originalEntryNameUnderEditing,
                                            originalEntryValueUnderEditing),
                                        CollectionEntriesEditEntry(originalEntryNameUnderEditing,
                                            originalEntryValueUnderEditing))
                                    _collection = _collection.minus(CollectionEntriesEditEntry(originalEntryNameUnderEditing,
                                        originalEntryValueUnderEditing))
                                    originalEntryNameUnderEditing = ""
                                    originalEntryValueUnderEditing = ""
                                    entryNameUnderEditing = ""
                                    entryValueUnderEditing = ""
                                    entryChangeAction = null
                                    dialogScreen = CollectionEntriesEditScreenEnum.LIST
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(currentStringsStore.preferenceCollectionDeleteCurrentEntryText)
                            }
                            TextButton(
                                onClick = {
                                    onApplyEdit(CollectionEntriesChangeAction.MODIFY,
                                        CollectionEntriesEditEntry(entryNameUnderEditing,
                                            entryValueUnderEditing),
                                        CollectionEntriesEditEntry(originalEntryNameUnderEditing,
                                            originalEntryValueUnderEditing))
                                    _collection = _collection.map({ it ->
                                        if (it.entryName == originalEntryNameUnderEditing &&
                                            it.entryValue == originalEntryValueUnderEditing) {
                                            CollectionEntriesEditEntry(entryNameUnderEditing,
                                                entryValueUnderEditing)
                                        } else { it } })
                                    originalEntryNameUnderEditing = ""
                                    originalEntryValueUnderEditing = ""
                                    entryNameUnderEditing = ""
                                    entryValueUnderEditing = ""
                                    entryChangeAction = null
                                    dialogScreen = CollectionEntriesEditScreenEnum.LIST
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(currentStringsStore.preferenceCollectionApplyEditOfCurrentEntryText)
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun OptionsComposable() {
    var searchEngineEditDialogOpened by remember { mutableStateOf(false) }
    var bookmarksEditDialogOpened by remember { mutableStateOf(false) }
    Text(
        text = currentStringsStore.optionsHeader,
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.caption
    )
    Divider()
    CheckboxPreferenceComposable(
        currentStringsStore.preferenceHTTPSOnlyModeText,
        (Globals.defaultHttpsOnlyMode == HTTPS_ONLY),
        { enableHttpsOnly ->
            if (enableHttpsOnly) {
                Globals.geckoRuntime.settings.allowInsecureConnections = HTTPS_ONLY
                Globals.defaultHttpsOnlyMode = HTTPS_ONLY
            } else {
                Globals.geckoRuntime.settings.allowInsecureConnections = ALLOW_ALL
                Globals.defaultHttpsOnlyMode = ALLOW_ALL
            }
        }
    )
    Divider()
    CheckboxPreferenceComposable(
        currentStringsStore.preferenceEnableJavascriptByDefaultText,
        Globals.isAllowingJavascriptByDefault,
        { enableGlobalJavascript ->
            Globals.geckoRuntime.settings.javaScriptEnabled = enableGlobalJavascript
            Globals.isAllowingJavascriptByDefault = enableGlobalJavascript
        })
    Divider()
    CheckboxPreferenceComposable(
        currentStringsStore.preferenceEnableImagesByDefaultText,
        Globals.isAllowingImagesByDefault,
        { enableGlobalImages ->
            Globals.mediaSwitchWebExtensionPort?.postMessage( JSONObject().put("message",
                if (enableGlobalImages)
                    "Deactivate images global blocking"
                else "Activate images global blocking")
            )
            Globals.isAllowingImagesByDefault = enableGlobalImages
        })
    Divider()
    CheckboxPreferenceComposable(
        currentStringsStore.preferenceEnableMediaByDefaultText,
        Globals.isAllowingMediaByDefault,
        { enableGlobalMedia ->
            Globals.mediaSwitchWebExtensionPort?.postMessage( JSONObject().put("message",
                if (enableGlobalMedia)
                    "Deactivate media global blocking"
                else "Activate media global blocking")
            )
            Globals.isAllowingMediaByDefault = enableGlobalMedia
        })
    Divider()
    CheckboxPreferenceComposable(
        currentStringsStore.preferenceEnableWebfontsByDefaultText,
        Globals.isAllowingWebfontsByDefault,
        { enableGlobalWebfonts ->
            Globals.mediaSwitchWebExtensionPort?.postMessage( JSONObject().put("message",
                if (enableGlobalWebfonts)
                    "Deactivate webfonts global blocking"
                else "Activate webfonts global blocking")
            )
            Globals.isAllowingWebfontsByDefault = enableGlobalWebfonts
        })
    Divider()
    PreferenceActionComposable(currentStringsStore.preferenceClearDataExceptSiteSettingsText, {
        Globals.geckoRuntime.storageController.clearData(
            StorageController.ClearFlags.ALL - StorageController.ClearFlags.SITE_SETTINGS
        )
    })
    Divider()
    PreferenceOpenDialogComposable(currentStringsStore.preferenceEditSearchEnginesText, {
        searchEngineEditDialogOpened = true
        Globals.dialogOnScreen = true
    })
    if (searchEngineEditDialogOpened) {
        CollectionEntriesEditComposable(listPromptText = currentStringsStore.preferenceEditSearchEnginesText, entryPromptText = currentStringsStore.preferenceEditSearchEnginesEntryText,
            collection = Globals.searchEngineConfigMap.map({
                CollectionEntriesEditEntry(it.key, it.value) }),
            onApplyEdit = { changeAction, entry, originalEntry ->
                when(changeAction) {
                    CollectionEntriesChangeAction.ADD -> {
                        setValueInSearchEngineConfigMap(entry.entryName, entry.entryValue)
                    }
                    CollectionEntriesChangeAction.MODIFY -> {
                        setValueInSearchEngineConfigMap(entry.entryName, entry.entryValue)
                    }
                    CollectionEntriesChangeAction.DELETE -> {
                        delValueInSearchEngineConfigMap(entry.entryName)
                    }
                }
            },
            onClose = {
                searchEngineEditDialogOpened = false
                Globals.dialogOnScreen = false
            }
        )
    }
    Divider()
    PreferenceOpenDialogComposable(currentStringsStore.preferenceEditBookmarksText, {
        bookmarksEditDialogOpened = true
        Globals.dialogOnScreen = true
    })
    if (bookmarksEditDialogOpened) {
        CollectionEntriesEditComposable(listPromptText = currentStringsStore.preferenceEditBookmarksText, entryPromptText = currentStringsStore.preferenceEditBookmarksEntryText,
            collection = Globals.bookmarkConfigSet.map({ CollectionEntriesEditEntry(it.bookmarkTitle, it.bookmarkUri) }),
            onApplyEdit = { changeAction, entry, originalEntry ->
                when(changeAction) {
                    CollectionEntriesChangeAction.ADD -> {
                        addValueInBookmarkConfigList(entry.entryName, entry.entryValue)
                    }
                    CollectionEntriesChangeAction.MODIFY -> {
                        replaceValueInBookmarkConfigList(originalEntry.entryName,
                            originalEntry.entryValue, entry.entryName, entry.entryValue)
                    }
                    CollectionEntriesChangeAction.DELETE -> {
                        delValueInBookmarkConfigList(originalEntry.entryName, originalEntry.entryValue)
                    }
                }
            },
            onClose = {
                bookmarksEditDialogOpened = false
                Globals.dialogOnScreen = false
            }
        )
    }
}
