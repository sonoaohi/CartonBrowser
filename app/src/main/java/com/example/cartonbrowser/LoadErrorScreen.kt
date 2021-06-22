package com.example.cartonbrowser

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Composable
fun LoadErrorScreenComposable(sessionErrorMessage: String?) {
    var errorMessage: String by remember { mutableStateOf(sessionErrorMessage ?: "No Error") }
    Column {
        Box(modifier = Modifier.fillMaxSize().weight(1f),
            contentAlignment = Alignment.Center) {
            Text(errorMessage)
        }
    }

    object {
        @Subscribe(threadMode = ThreadMode.MAIN_ORDERED, sticky = false, priority = 0)
        fun onShowErrorPageEvent(event: ShowErrorPageEvent) {
            errorMessage = event.errorMessage
        }

        init {
            EventBus.getDefault().register(this)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadErrorScreenPreview() {
    LoadErrorScreenComposable("Sample Error Message")
}