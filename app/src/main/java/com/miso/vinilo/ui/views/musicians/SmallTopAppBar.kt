package com.miso.vinilo.ui.views.musicians

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.miso.vinilo.ui.theme.BaseWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = BaseWhite,
            navigationIconContentColor = BaseWhite
        )
    )
}
