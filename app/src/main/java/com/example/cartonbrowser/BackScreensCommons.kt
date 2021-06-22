package com.example.cartonbrowser

import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BackScreenTabRow() {
    var selectedTabIndexState: Int by remember { mutableStateOf(0) }
    TabRow(selectedTabIndex = selectedTabIndexState,
           backgroundColor = Color.White,
           indicator =@Composable { tabPositions ->
               TabRowDefaults.Indicator(
                   Modifier
                       //.wrapContentSize(Alignment.TopStart) // position the indicator to the top
                       .tabIndicatorOffset(tabPositions[selectedTabIndexState])
               )
           }
    ) {
        Tab(
            text = { Text("Tabs") },
            selected = (selectedTabIndexState == 0),
            onClick = {
                selectedTabIndexState = 0
                changeCurrentScreen(ScreenEnum.TabsListScreen)
            }
        )
        Tab(
            text = { Text("Current") },
            selected = (selectedTabIndexState == 1),
            onClick = {
                selectedTabIndexState = 1
                changeCurrentScreen(ScreenEnum.CurrentTabScreen)
            }
        )
        Tab(
            text = { Text("Options") },
            selected = (selectedTabIndexState == 2),
            onClick = {
                selectedTabIndexState = 2
                changeCurrentScreen(ScreenEnum.OptionScreen)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BackScreenTabRowPreview() {
    BackScreenTabRow()
}
