package com.example.cartonbrowser


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.cartonbrowser.Globals.currentStringsStore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mozilla.geckoview.GeckoSession.ContentDelegate.ContextElement.*
import org.mozilla.geckoview.GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE
import org.mozilla.geckoview.GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE
import org.mozilla.geckoview.GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebResponse

@Composable
fun ComposableGeckoView(geckoSessionString: String, modifier: Modifier = Modifier) {
    var currentGeckoSessionString by rememberSaveable { mutableStateOf( geckoSessionString ) }
    // Adds view to Compose
    AndroidView(
        modifier = modifier.fillMaxWidth(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            // Creates custom view
            GeckoView(context).apply {
                // Sets up listeners for View -> Compose communication
                this.releaseSession()
                this.setSession(Globals.currentGeckoSessionWrapped.session)

            }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
            currentGeckoSessionString
            view.releaseSession()
            view.setSession(Globals.currentGeckoSessionWrapped.session)
            // As selectedItem is read here, AndroidView will recompose
            // whenever the state changes
            // Example of Compose -> View communication
            // view.coordinator.selectedItem = selectedItem.value

        }
    )
    GeckoViewContextMenu()
    GeckoViewDownloadPrompt()

    GeckoViewAlertPrompt()
    // GeckoViewAuthPrompt() // No plans to implement now
    // GeckoViewBeforeUnloadPrompt() // Mostly won't implement
    GeckoViewButtonPrompt()
    GeckoViewChoicePrompt()
    // GeckoViewColorPrompt() // No plans to implement now
    // GeckoViewDateTimePrompt() // No plans to implement now
    // GeckoViewFilePrompt() // To be implemented later
    // GeckoViewPopupPrompt() // Under consideration
    // GeckoViewRepostConfirmPrompt() // Mostly won't implement
    // GeckoViewSharePrompt() // Mostly won't implement
    GeckoViewTextPrompt()

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onCurrentSessionWrappedChangeEvent(event: CurrentSessionWrappedChangeEvent) {
            currentGeckoSessionString = event.newCurrentSessionWrapped.sessionString
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Composable
fun GeckoViewContextMenu() {
    var showingDialog by remember { mutableStateOf(false) }
    var linkUri by remember { mutableStateOf("") }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    if (showingDialog) {
        Dialog(onDismissRequest = {
            showingDialog = false
            Globals.dialogOnScreen = false
            linkUri = ""
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(linkUri,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    TextButton(
                        onClick = {
                            changeCurrentSession(newGeckoSessionWrapped(linkUri))
                            goToMainScreenOrLoadErrorScreenOrPortalScreen()
                            showingDialog = false
                            Globals.dialogOnScreen = false
                            linkUri = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.contextMenuOpenNewTabString)
                    }
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(linkUri))
                            showingDialog = false
                            Globals.dialogOnScreen = false
                            linkUri = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.contextMenuCopyLinkAddressString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onContextMenuEvent(event: ContextMenuEvent) {
            if (event.contextElement.type == TYPE_NONE) {
                event.contextElement.linkUri?.let {
                    linkUri = it
                    showingDialog = true
                    Globals.dialogOnScreen = true
                }
            }
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Composable
fun GeckoViewDownloadPrompt() {
    var showingDialog by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }
    var webResponse: WebResponse? by remember { mutableStateOf(null) }

    if (showingDialog) {
        Dialog(onDismissRequest = {
            promptText = ""
            webResponse = null
            showingDialog = false
            Globals.dialogOnScreen = false
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(promptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    TextButton(
                        onClick = {
                            webResponse?.let {
                                EventBus.getDefault().post(NewDownloadEvent(Uri.parse(it.uri)))
                            }
                            promptText = ""
                            webResponse = null
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.downloadPromptDownloadButtonString)
                    }
                    TextButton(
                        onClick = {
                            promptText = ""
                            webResponse = null
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.promptsCommonDismissButtonString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onDownloadPromptEvent(event: DownloadPromptEvent) {
            webResponse = event.webResponse
            promptText = currentStringsStore.downloadPromptPromptTextString.format(event.webResponse.uri)
            showingDialog = true
            Globals.dialogOnScreen = true
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}


@Composable
fun GeckoViewAlertPrompt() {
    var showingDialog by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }

    if (showingDialog) {
        Dialog(onDismissRequest = {
            promptText = ""
            showingDialog = false
            Globals.dialogOnScreen = false
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(promptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    TextButton(
                        onClick = {
                            promptText = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.promptsCommonDismissButtonString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onAlertPromptEvent(event: AlertPromptEvent) {
            promptText = event.promptMessage
            showingDialog = true
            Globals.dialogOnScreen = true
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Composable
fun GeckoViewButtonPrompt() {
    var showingDialog by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }
    var promptEvent: ButtonPromptEvent? by remember { mutableStateOf(null) }

    if (showingDialog) {
        Dialog(onDismissRequest = {
            promptEvent?.geckoResult?.complete(promptEvent?.buttonPrompt?.dismiss())
            promptEvent = null
            promptText = ""
            showingDialog = false
            Globals.dialogOnScreen = false
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(promptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.buttonPrompt?.confirm(POSITIVE))
                            promptEvent = null
                            promptText = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.buttonPromptAllowButtonString)
                    }
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.buttonPrompt?.confirm(NEGATIVE))
                            promptEvent = null
                            promptText = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.buttonPromptDenyButtonString)
                    }
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.buttonPrompt?.dismiss())
                            promptEvent = null
                            promptText = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.promptsCommonDismissButtonString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onButtonPromptEvent(event: ButtonPromptEvent) {
            promptText = event.buttonPrompt.message ?: ""
            promptEvent = event
            showingDialog = true
            Globals.dialogOnScreen = true
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

data class ChoicePromptEntry(val disabled: Boolean, val icon: String?,
                             val id: String?, val label: String,
                             var selected: Boolean, val separator: Boolean,
                             val groupLabel: String?)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GeckoViewChoicePrompt() {
    var showingDialog by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }
    var promptEvent: ChoicePromptEvent? by remember { mutableStateOf(null) }
    var promptChoices: SnapshotStateList<ChoicePromptEntry> by remember { mutableStateOf(mutableStateListOf()) }
    var promptResponse: MutableSet<String> by remember { mutableStateOf(mutableSetOf()) }

    if (showingDialog) {
        Dialog(onDismissRequest = {
            promptEvent?.geckoResult?.complete(promptEvent?.choicePrompt?.dismiss())
            promptEvent = null
            promptText = ""
            promptChoices = mutableStateListOf()
            promptResponse = mutableSetOf()
            showingDialog = false
            Globals.dialogOnScreen = false
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(promptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    LazyColumn(Modifier.heightIn(max = 250.dp)) {
                        itemsIndexed(
                            items = promptChoices
                        ) { index, entry ->
                            ListItem(
                                text = {
                                    Text( if (entry.groupLabel != null) "- " + entry.label else "  " + entry.label )
                                },
                                trailing = {
                                    if (entry.separator != true && entry.id != null) {
                                        Checkbox(
                                            checked = entry.selected,
                                            onCheckedChange = { entryCheckedStateChanged ->
                                                if (entry.disabled != true) {
                                                    if (entryCheckedStateChanged) {
                                                        if (promptEvent?.choicePrompt?.type != MULTIPLE) {
                                                            promptChoices.forEachIndexed { index2, entry2 ->
                                                                entry2.selected = false
                                                            }
                                                        }
                                                        // Use write operation on the list to trigger auto recomposition
                                                        promptChoices.set(
                                                            index,
                                                            entry.copy(selected = true)
                                                        )
                                                    } else {
                                                        promptChoices.set(
                                                            index,
                                                            entry.copy(selected = false)
                                                        )
                                                    }
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color.Transparent,
                                                uncheckedColor = LocalContentColor.current,
                                                checkmarkColor = LocalContentColor.current,
                                                disabledColor = Color.Transparent,
                                                disabledIndeterminateColor = Color.Transparent
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            try {
                                promptResponse = mutableSetOf()
                                promptChoices.forEach {
                                    if (it.selected) {
                                        if (it.id != null) {
                                            promptResponse.add(it.id)
                                        }
                                    }
                                }
                                if (promptResponse.size >= 1) {
                                    promptEvent?.geckoResult?.complete(promptEvent?.choicePrompt?.confirm(promptResponse.toTypedArray()))
                                    promptEvent = null
                                    promptText = ""
                                    promptChoices = mutableStateListOf()
                                    promptResponse = mutableSetOf()
                                    showingDialog = false
                                    Globals.dialogOnScreen = false
                                } else {
                                    // showingDialog = true
                                    // Globals.dialogOnScreen = true
                                }
                            } catch (e: Exception) {
                                //
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.choicePromptSubmitButtonString)
                    }
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.choicePrompt?.dismiss())
                            promptEvent = null
                            promptText = ""
                            promptChoices = mutableStateListOf()
                            promptResponse = mutableSetOf()
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.promptsCommonDismissButtonString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onChoicePromptEvent(event: ChoicePromptEvent) {
            promptText = event.choicePrompt.message ?: ""
            promptEvent = event
            for (choice in event.choicePrompt.choices) {
                if (choice.items != null) {
                    promptChoices.add(ChoicePromptEntry(choice.disabled, choice.icon, null,
                        choice.label, false, choice.separator, null))
                    for (item in choice.items!!) {
                        promptChoices.add(ChoicePromptEntry(item.disabled, item.icon, item.id,
                            item.label, item.selected, item.separator, choice.label))
                    }
                } else {
                    promptChoices.add(ChoicePromptEntry(choice.disabled, choice.icon, choice.id,
                        choice.label, choice.selected, choice.separator, null))
                }
            }
            showingDialog = true
            Globals.dialogOnScreen = true
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Composable
fun GeckoViewTextPrompt() {
    var showingDialog by remember { mutableStateOf(false) }
    var promptText by remember { mutableStateOf("") }
    var promptEvent: TextPromptEvent? by remember { mutableStateOf(null) }
    var inputValue by remember { mutableStateOf("") }

    if (showingDialog) {
        Dialog(onDismissRequest = {
            promptEvent?.geckoResult?.complete(promptEvent?.textPrompt?.dismiss())
            promptEvent = null
            promptText = ""
            inputValue = ""
            showingDialog = false
            Globals.dialogOnScreen = false
        }) {
            Box(
                Modifier
                    .wrapContentSize()
                    .background(Color.White)) {
                Column {
                    Text(promptText,
                        Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 16.dp, vertical = 12.dp))
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text(currentStringsStore.textPromptInputLabelString) },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(0.8f)
                    )
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.textPrompt?.confirm(inputValue))
                            promptEvent = null
                            promptText = ""
                            inputValue = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.textPromptEnterButtonString)
                    }
                    TextButton(
                        onClick = {
                            promptEvent?.geckoResult?.complete(promptEvent?.textPrompt?.dismiss())
                            promptEvent = null
                            promptText = ""
                            inputValue = ""
                            showingDialog = false
                            Globals.dialogOnScreen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(currentStringsStore.promptsCommonDismissButtonString)
                    }
                }
            }
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onTextPromptEvent(event: TextPromptEvent) {
            promptText = event.textPrompt.message ?: ""
            inputValue = event.textPrompt.defaultValue ?: ""
            promptEvent = event
            showingDialog = true
            Globals.dialogOnScreen = true
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

