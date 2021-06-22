package com.example.cartonbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cartonbrowser.Globals.currentStringsStore


@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var showingChooser by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var searchEngine by remember { mutableStateOf( Globals.defaultSearchEngine ) }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text(searchEngine, maxLines = 1) },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(0xFFF1F1F1)),
        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
        keyboardOptions = KeyboardOptions(
            KeyboardCapitalization.None, false,
            KeyboardType.Text, ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onAny = {
            if (searchText.isNotBlank()) {
                Globals.currentGeckoSessionWrapped.loadUri(
                    getSearchUri(searchEngine, searchText, Globals.searchEngineConfigMap)
                )
                goToMainScreenOrLoadErrorScreenOrPortalScreen()
            }
        }),
        leadingIcon = {
            IconButton(onClick = {
                showingChooser = true
            }) {
                Icon(Icons.Rounded.Menu,
                    contentDescription = currentStringsStore.searchBarMenuButtonIconDescription,
                    modifier = Modifier.size(ButtonDefaults.IconSize))
            }
        },
        trailingIcon = {
            IconButton(onClick = {
                if (searchText.isNotBlank()) {
                    Globals.currentGeckoSessionWrapped.loadUri(
                        getSearchUri(searchEngine, searchText, Globals.searchEngineConfigMap)
                    )
                    goToMainScreenOrLoadErrorScreenOrPortalScreen()
                }
            }) {
                Icon(Icons.Rounded.Search,
                    contentDescription = currentStringsStore.searchBarSearchButtonIconDescription,
                    modifier = Modifier.size(ButtonDefaults.IconSize))
            }
        },
        modifier = modifier
    )
    if (showingChooser) {
        Dialog(onDismissRequest = {
            showingChooser = false
        }) {
            Box(Modifier.wrapContentSize().background(Color.White)) {
                Column {
                    Text(
                        currentStringsStore.searchBarChooseEnginePromptText, Modifier.fillMaxWidth(0.8f)
                        .padding(horizontal = 16.dp, vertical = 12.dp), textAlign = TextAlign.Center)
                    Globals.searchEngineConfigMap.keys.forEach {
                        TextButton(
                            onClick = {
                                searchEngine = it
                                showingChooser = false
                            },
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            if (it == searchEngine) {
                                Text(
                                    currentStringsStore.searchBarCurrentEngineFormatString.format(it), maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(0.8f))
                            } else {
                                Text(it, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(0.8f))
                            }
                        }
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarksComposable(modifier: Modifier = Modifier) {
    Text(
        text = currentStringsStore.portalScreenBookmarksSectionHeaderText,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.overline
    )
    Divider(modifier = modifier)
    LazyColumn(modifier = modifier, reverseLayout = true) {
        itemsIndexed(items = Globals.bookmarkConfigSet.toList()) { index: Int, item: Bookmark ->
            ListItem(
                text = { Text(item.bookmarkTitle) },
                secondaryText = { Text(item.bookmarkUri) },
                modifier = Modifier.clickable{
                    Globals.currentGeckoSessionWrapped.loadUri(item.bookmarkUri)
                    goToMainScreenOrLoadErrorScreenOrPortalScreen()
                }
            )
        }
    }
}

@Composable
fun PortalScreen(modifier: Modifier = Modifier) {
    Box(modifier) {
        Column(Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally) {
            BookmarksComposable(Modifier.fillMaxWidth(0.9f))
            Divider(Modifier.fillMaxWidth(0.9f))
            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SearchBar(Modifier.fillMaxWidth(0.9f))
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PortalScreenPreview() {
    PortalScreen()
}
