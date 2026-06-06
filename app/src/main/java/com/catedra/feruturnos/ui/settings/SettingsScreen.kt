package com.catedra.feruturnos.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.R

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var selectedLanguage by remember {
        mutableStateOf(LanguagePreferences.getLanguage(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.language))

        ListItem(
            headlineContent = { Text(stringResource(R.string.spanish)) },
            leadingContent = {
                RadioButton(
                    selected = selectedLanguage == "es",
                    onClick = {
                        selectedLanguage = "es"
                        LanguagePreferences.saveLanguage(context, "es")
                        (context as? Activity)?.recreate()
                    }
                )
            }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.english)) },
            leadingContent = {
                RadioButton(
                    selected = selectedLanguage == "en",
                    onClick = {
                        selectedLanguage = "en"
                        LanguagePreferences.saveLanguage(context, "en")
                        (context as? Activity)?.recreate()
                    }
                )
            }
        )
    }
}