package com.example.cartonbrowser

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddressBar(modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    var textfieldEnabled by rememberSaveable { mutableStateOf(true) }
    var backscreenButtonCheckedState by rememberSaveable { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        // Or should it use URI type?
        var addressBarText by rememberSaveable { mutableStateOf("about:blank") }
        TextField(
            addressBarText,
            onValueChange = { addressBarText = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W800,
                textAlign = TextAlign.Start
            ),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                KeyboardCapitalization.None, false,
                KeyboardType.Uri, ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onAny = {
                keyboardController?.hide()
                focusRequester.freeFocus()
                Globals.currentGeckoSessionWrapped.loadUri(addressBarText)
                goToMainScreenOrLoadErrorScreenOrPortalScreen()
            }),
            enabled = textfieldEnabled,
            trailingIcon = {
                IconToggleButton(
                    checked = backscreenButtonCheckedState,
                    onCheckedChange = {
                        keyboardController?.hide()
                        focusRequester.freeFocus()
                        if (it) {
                            textfieldEnabled = false
                            changeCurrentScreen(ScreenEnum.TabsListScreen)
                        } else {
                            textfieldEnabled = true
                            goToMainScreenOrLoadErrorScreenOrPortalScreen()
                        }
                        backscreenButtonCheckedState = it
                    },
                    modifier = modifier.offset(x = 12.dp)
                ) {
                    val tint by animateColorAsState(
                        if (!backscreenButtonCheckedState) Color(0xFF7CB342) else Color(
                            0xFFF0F4C3
                        )
                    )
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Localized description",
                        tint = tint
                    )
                }
            }
        )
        object {
            @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
            fun onCurrentScreenChangeEvent(event: CurrentScreenChangeEvent) {
                val newScreen = event.newScreen
                when (newScreen) {
                    ScreenEnum.MainScreen -> {
                        backscreenButtonCheckedState = false
                        textfieldEnabled = true
                    }
                    ScreenEnum.ErrorScreen -> {
                        backscreenButtonCheckedState = false
                        textfieldEnabled = true
                    }
                    ScreenEnum.PortalScreen -> {
                        backscreenButtonCheckedState = false
                        textfieldEnabled = true
                    }
                    else -> {
                        backscreenButtonCheckedState = true
                        textfieldEnabled = false
                    }
                }
            }

            @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
            fun onSessionUriChangeEvent(event: SessionUriChangeEvent) {
                if (event.sessionString == Globals.currentGeckoSessionWrapped.sessionString) {
                    addressBarText = event.newSessionUri
                }
            }

            @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
            fun onCurrentSessionWrappedChangeEvent(event: CurrentSessionWrappedChangeEvent) {
                addressBarText = event.newCurrentSessionWrapped.sessionUri
            }

            init {
                EventBus.getDefault().register(this)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddressBarPreview() {
    AddressBar()
}
