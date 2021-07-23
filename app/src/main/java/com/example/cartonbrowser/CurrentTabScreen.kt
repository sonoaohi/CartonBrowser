package com.example.cartonbrowser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.cartonbrowser.Globals.currentStringsStore
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mozilla.geckoview.GeckoSession

@Composable
fun CurrentTabScreen() {
    if (Globals.currentGeckoSessionWrapped.sessionErrorMessage == null) {
        Column {
            Text(
                text = currentStringsStore.tabHistoryListHeader,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.caption
            )
            Divider()
            SessionHistoryList(Modifier.weight(1f))
            Divider()
            Text(
                text = currentStringsStore.siteSwitchesHeader,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.caption
            )
            Divider()
            Row (modifier = Modifier.padding(16.dp)) {
                ImagesToggleButton()
                Spacer(Modifier.width(8.dp))
                MediaToggleButton()
                Spacer(Modifier.width(8.dp))
                WebfontsToggleButton()
            }
            Divider()
            Text(
                text = currentStringsStore.sessionSwitchesHeader,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.caption
            )
            Divider()
            Row (modifier = Modifier.padding(16.dp)) {
//                SessionReloadButton()
//                Spacer(Modifier.width(8.dp))
                JavascriptToggleButton()
            }
        }
    }
}

@Composable
fun ImagesToggleButton() {
    var imagesButtonChecked by remember { mutableStateOf(Globals.currentGeckoSessionWrapped.isAllowingImages) }
    TextButton(
        onClick = {
            Globals.currentGeckoSessionWrapped.isAllowingImages =
                !Globals.currentGeckoSessionWrapped.isAllowingImages
            imagesButtonChecked = Globals.currentGeckoSessionWrapped.isAllowingImages
            Globals.currentGeckoSessionWrapped.session.reload()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        colors = (if (imagesButtonChecked)
            ButtonDefaults.buttonColors()
        else ButtonDefaults.textButtonColors()),
        modifier = Modifier.semantics {
            stateDescription = if (imagesButtonChecked) {
                currentStringsStore.toggleButtonEnabledStateDescription
            } else {
                currentStringsStore.toggleButtonDisabledStateDescription
            }
        }
    ) {
        Icon(
            if (imagesButtonChecked) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.imagesToggleButtonText, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun MediaToggleButton() {
    var mediaButtonChecked by remember { mutableStateOf(Globals.currentGeckoSessionWrapped.isAllowingMedia) }
    TextButton(
        onClick = {
            Globals.currentGeckoSessionWrapped.isAllowingMedia =
                !Globals.currentGeckoSessionWrapped.isAllowingMedia
            mediaButtonChecked = Globals.currentGeckoSessionWrapped.isAllowingMedia
            Globals.currentGeckoSessionWrapped.session.reload()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        colors = (if (mediaButtonChecked)
            ButtonDefaults.buttonColors()
        else ButtonDefaults.textButtonColors()),
        modifier = Modifier.semantics {
            stateDescription = if (mediaButtonChecked) {
                currentStringsStore.toggleButtonEnabledStateDescription
            } else {
                currentStringsStore.toggleButtonDisabledStateDescription
            }
        }
    ) {
        Icon(
            if (mediaButtonChecked) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.mediaToggleButtonText, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun WebfontsToggleButton() {
    var webfontsButtonChecked by remember { mutableStateOf(Globals.currentGeckoSessionWrapped.isAllowingWebfonts) }
    TextButton(
        onClick = {
            Globals.currentGeckoSessionWrapped.isAllowingWebfonts =
                !Globals.currentGeckoSessionWrapped.isAllowingWebfonts
            webfontsButtonChecked = Globals.currentGeckoSessionWrapped.isAllowingWebfonts
            Globals.currentGeckoSessionWrapped.session.reload()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        colors = (if (webfontsButtonChecked)
            ButtonDefaults.buttonColors()
        else ButtonDefaults.textButtonColors()),
        modifier = Modifier.semantics {
            stateDescription = if (webfontsButtonChecked) {
                currentStringsStore.toggleButtonEnabledStateDescription
            } else {
                currentStringsStore.toggleButtonDisabledStateDescription
            }
        }
    ) {
        Icon(
            if (webfontsButtonChecked) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.webfontsToggleButtonText, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun JavascriptToggleButton() {
    var javascriptButtonChecked by remember { mutableStateOf(Globals.currentGeckoSessionWrapped.isAllowingJavascript) }
    TextButton(
        onClick = {
            Globals.currentGeckoSessionWrapped.isAllowingJavascript =
                !Globals.currentGeckoSessionWrapped.isAllowingJavascript
            javascriptButtonChecked = Globals.currentGeckoSessionWrapped.isAllowingJavascript
            Globals.currentGeckoSessionWrapped.session.reload()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        colors = (if (javascriptButtonChecked)
            ButtonDefaults.buttonColors()
        else ButtonDefaults.textButtonColors()),
        modifier = Modifier.semantics {
            stateDescription = if (javascriptButtonChecked) {
                currentStringsStore.toggleButtonEnabledStateDescription
            } else {
                currentStringsStore.toggleButtonDisabledStateDescription
            }
        }
    ) {
        Icon(
            if (javascriptButtonChecked) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.javascriptToggleButtonText, style = MaterialTheme.typography.caption)
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onCurrentSessionWrappedChangeEvent(event: CurrentSessionWrappedChangeEvent) {
            javascriptButtonChecked = event.newCurrentSessionWrapped.isAllowingJavascript
        }
        init {
            EventBus.getDefault().register(this)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SessionHistoryListItem(historyItemIndex: Int, historyItemTitle: String,
                           historyItemUri: String, isAtTheCurrentLocation: Boolean = false) {
    ListItem(
        overlineText = {
            if (isAtTheCurrentLocation) {
                Text(currentStringsStore.sessionHistoryListItemOverlineTextCurrent.format(historyItemIndex))
            } else {
                Text(currentStringsStore.sessionHistoryListItemOverlineTextNoncurrent.format(historyItemIndex))
            }
        },
        text = { Text(historyItemTitle) },
        secondaryText = { Text(historyItemUri) },
        modifier = Modifier.background(
            if (isAtTheCurrentLocation)
                MaterialTheme.colors.primaryVariant.copy(alpha = 0.12f)
            else
                Color.Transparent).clickable {
            Globals.currentGeckoSessionWrapped.session.gotoHistoryIndex(historyItemIndex)
            goToMainScreenOrLoadErrorScreenOrPortalScreen()
        }.offset(y = (-5).dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryList(modifier: Modifier = Modifier) {
    var historyItemsList: GeckoSession.HistoryDelegate.HistoryList? by remember { mutableStateOf(
        Globals.currentGeckoSessionWrapped.sessionHistory
    ) }

    historyItemsList?.let {
        val currentIndex: Int? by derivedStateOf { historyItemsList?.currentIndex }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Column(modifier) {
            LazyColumn(state = listState, reverseLayout = true, modifier = modifier) {
                itemsIndexed(
                    items = it
                ) { index, historyItem ->
                    SessionHistoryListItem(
                        index,
                        historyItem.title,
                        historyItem.uri,
                        (currentIndex == index)
                    )
                }
            }
        }

        SideEffect {
            coroutineScope.launch {
                currentIndex?.let { index -> listState.scrollToItem(index) }
            }
        }
    }
}
