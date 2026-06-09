package com.catedra.feruturnos.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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

    // Estructuramos los idiomas en una lista de pares (Código de idioma -> ID del String)
    val languages = remember {
        listOf(
            "es" to R.string.spanish,
            "en" to R.string.english,
            "pt" to R.string.portugues,
            "fr" to R.string.french
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título de sección estilizado según Material 3
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Renderizado dinámico y limpio mediante un bucle
        languages.forEach { (langCode, stringResId) ->
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(stringResId),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingContent = {
                    RadioButton(
                        selected = selectedLanguage == langCode,
                        onClick = null // Dejamos en null para que no intercepte el click de la fila completa
                    )
                },
                modifier = Modifier
                    .clickable {
                        if (selectedLanguage != langCode) {
                            selectedLanguage = langCode
                            LanguagePreferences.saveLanguage(context, langCode)
                            (context as? Activity)?.recreate()
                        }
                    }
            )
        }
    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(stringResource(R.string.language))
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.spanish)) },
//            leadingContent = {
//                RadioButton(
//                    selected = selectedLanguage == "es",
//                    onClick = {
//                        selectedLanguage = "es"
//                        LanguagePreferences.saveLanguage(context, "es")
//                        (context as? Activity)?.recreate()
//                    }
//                )
//            }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.english)) },
//            leadingContent = {
//                RadioButton(
//                    selected = selectedLanguage == "en",
//                    onClick = {
//                        selectedLanguage = "en"
//                        LanguagePreferences.saveLanguage(context, "en")
//                        (context as? Activity)?.recreate()
//                    }
//                )
//            }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.portugues)) },
//            leadingContent = {
//                RadioButton(
//                    selected = selectedLanguage == "pt",
//                    onClick = {
//                        selectedLanguage = "pt"
//                        LanguagePreferences.saveLanguage(context, "pt")
//                        (context as? Activity)?.recreate()
//                    }
//                )
//            }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.french)) },
//            leadingContent = {
//                RadioButton(
//                    selected = selectedLanguage == "fr",
//                    onClick = {
//                        selectedLanguage = "fr"
//                        LanguagePreferences.saveLanguage(context, "fr")
//                        (context as? Activity)?.recreate()
//                    }
//                )
//            }
//        )
//    }
}