package com.catedra.feruturnos.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    ProfileContent(
        profileState = profileState
    )
}