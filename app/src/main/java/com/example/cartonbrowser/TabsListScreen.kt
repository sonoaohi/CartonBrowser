package com.example.cartonbrowser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cartonbrowser.Globals.currentStringsStore
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabsItem(
    modifier: Modifier = Modifier, tabSessionTitle: String,
    tabSessionUri: String, onClose: () -> Unit, isCurrentTab: Boolean = false, tabIndex: Int
) {
    ListItem(
        overlineText = {
        if (isCurrentTab) {
            Text(currentStringsStore.tabItemCurrentTabFormatString.format(tabIndex))
        } else {
            Text("#$tabIndex")
        }
    },
        text = { Text(tabSessionTitle) },
        secondaryText = { Text(tabSessionUri) },
        singleLineSecondaryText = true,
        trailing = {
            IconButton(
                onClick = onClose
            ) {
                Icon(Icons.Rounded.Close, contentDescription = currentStringsStore.tabItemCloseTabButtonIconDescription)
            }
        },
        modifier = modifier
            .background(
                if (isCurrentTab)
                    MaterialTheme.colors.primaryVariant.copy(alpha = 0.12f)
                else
                    Color.Transparent)
            .padding(bottom = 16.dp)

        // Remove this bottom padding modifier
        // if the bottom padding quirk of ListItem
        // that multi-line secondary text have no bottom padding
        // is gone
    )
}

data class TabsItemData(val sessionString: String, var sessionTitle: String,
                        var sessionUri: String, var isCurrentTab: Boolean = false)

@Composable
fun TabsListComposable(modifier: Modifier = Modifier, sessionsList: MutableList<GeckoSessionWrapped>) {

    // Use SnapshotStateList to enable automatic recomposition
    val sessionsTripleList: SnapshotStateList<TabsItemData> by remember {
        mutableStateOf(
            sessionsList.map({
                TabsItemData(it.sessionString, it.sessionTitle, it.sessionUri)
            }).toMutableStateList()
        )
    }

    sessionsTripleList
        .first({ it.sessionString == Globals.currentGeckoSessionWrapped.sessionString })
        .isCurrentTab = true

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(state = listState, reverseLayout = true, modifier = modifier) {
        itemsIndexed(
            items = sessionsTripleList
        ) { tabIndex, tabItemData ->
            TabsItem(
                modifier = Modifier.clickable {
                    changeCurrentSession(
                        Globals.geckoSessionsList.firstOrNull(
                            { it.sessionString == tabItemData.sessionString}
                        )!!
                    )
                    goToMainScreenOrLoadErrorScreenOrPortalScreen()
                },
                tabSessionTitle = tabItemData.sessionTitle,
                tabSessionUri = tabItemData.sessionUri,
                onClose = {
                    if (tabItemData.isCurrentTab) {
                        val tabSessionIndex =
                            sessionsTripleList.indexOfFirst { it == tabItemData }
                        val nextFocusSessionStringOnCurrentTabClose: String
                        if (sessionsTripleList.size > 1) { // There exist more tabs than the current one
                            nextFocusSessionStringOnCurrentTabClose = if (tabSessionIndex == 0) {
                                sessionsTripleList[1].sessionString
                            } else {
                                sessionsTripleList[tabSessionIndex - 1].sessionString
                            }
                            changeCurrentSession(
                                Globals.geckoSessionsList.firstOrNull(
                                    {it.sessionString == nextFocusSessionStringOnCurrentTabClose}
                                )!!
                            )
                            Globals.geckoSessionsList.removeIf({ it.sessionString == tabItemData.sessionString })
                            sessionsTripleList.removeIf { it.sessionString == tabItemData.sessionString }
                            sessionsTripleList
                                .first { it.sessionString == nextFocusSessionStringOnCurrentTabClose }
                                .isCurrentTab = true
                        } else {
                            changeCurrentSession(newGeckoSessionWrapped())
                            Globals.geckoSessionsList.removeIf({ it.sessionString == tabItemData.sessionString })
                            // sessionsTripleList.removeIf { it.sessionString == tabItemData.sessionString }
                            goToMainScreenOrLoadErrorScreenOrPortalScreen()
                        }
                    } else {
                        Globals.geckoSessionsList.removeIf({ it.sessionString == tabItemData.sessionString })
                        sessionsTripleList.removeIf { it.sessionString == tabItemData.sessionString }
                    }
                },
                isCurrentTab = tabItemData.isCurrentTab,
                tabIndex = tabIndex
            )
            Divider()
        }
    }
    SideEffect {
        coroutineScope.launch {
            listState.scrollToItem(
                sessionsTripleList.indexOfFirst {
                    it.sessionString == Globals.currentGeckoSessionWrapped.sessionString }
            )
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onSessionUriChangeEvent(event: SessionUriChangeEvent) {
            val relevantTabItemIndex = sessionsTripleList.indexOfFirst(
                { it.sessionString == event.sessionString })
            // Use write operation on sessionsTripleList to induce automatic recomposition
            sessionsTripleList[relevantTabItemIndex] =
                sessionsTripleList[relevantTabItemIndex].copy(sessionUri = event.newSessionUri)
        }

        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onSessionTitleChangeEvent(event: SessionTitleChangeEvent) {
            val relevantTabItemIndex = sessionsTripleList.indexOfFirst(
                { it.sessionString == event.sessionString })
            // Use write operation on sessionsTripleList to induce automatic recomposition
            sessionsTripleList[relevantTabItemIndex] =
                sessionsTripleList[relevantTabItemIndex].copy(sessionTitle = event.newSessionTitle)
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Composable
fun NewTabButton() {
    TextButton(
        onClick = {
            changeCurrentSession(newGeckoSessionWrapped())
            goToMainScreenOrLoadErrorScreenOrPortalScreen()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.newTabButtonTextString, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun SessionReloadButton() {
    TextButton(
        onClick = {
            Globals.currentGeckoSessionWrapped.session.reload()
            goToMainScreenOrLoadErrorScreenOrPortalScreen()
        },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
    ) {
        Icon(Icons.Rounded.Refresh,
            contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(currentStringsStore.sessionReloadButtonText, style = MaterialTheme.typography.caption)
    }
}

@Composable
fun TabsListScreenBar() {
    Row (modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.End) {
        // Is the horizontalArrangement effective?
        NewTabButton()
        Spacer(Modifier.width(8.dp))
        SessionReloadButton()
    }
}

@Composable
fun TabsListScreen() {
    Column {
        Text(
            text = currentStringsStore.tabsListHeader,
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            style = MaterialTheme.typography.caption
        )
        Divider()
        Box(Modifier.weight(1f)) {
            Column {
                TabsListComposable(Modifier.weight(1f), Globals.geckoSessionsList)
            }
        }
        TabsListScreenBar()
    }
}
