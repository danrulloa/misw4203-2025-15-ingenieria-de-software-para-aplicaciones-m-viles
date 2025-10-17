package com.miso.vinilo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miso.vinilo.R
import com.miso.vinilo.ui.theme.ViniloTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

private val LogoSize = 200.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Bienvenido a Vinilo",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Seleccione su rol para continuar",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(200.dp))

        Box(modifier = Modifier
            .size(LogoSize)
            .clip(RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(LogoSize),
                contentScale = ContentScale.Fit
            )
        }

        SpacerSmall()

        // Mostrar solo el dropdown centrado y con el mismo ancho que el logo
        RoleDropdown()
    }
}

@Composable
private fun SpacerSmall() {
    Spacer(modifier = Modifier.size(8.dp))
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleDropdown() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedRole by rememberSaveable { mutableStateOf("Usuario") }
    // FocusRequester must be attached to the text field so ExposedDropdownMenuBox can request focus
    val focusRequester = remember { FocusRequester() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // El TextField puede consumir clicks; envolverlo en un Box clickable garantiza
        // que tocar el área togglee `expanded` y abra el menú.
        Box(modifier = Modifier.width(LogoSize).clickable { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .focusRequester(focusRequester)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(text = { Text("Usuario") }, onClick = {
                selectedRole = "Usuario"
                expanded = false
            })
            DropdownMenuItem(text = { Text("Coleccionista") }, onClick = {
                selectedRole = "Coleccionista"
                expanded = false
            })
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ViniloTheme {
        HomeScreen()
    }
}
