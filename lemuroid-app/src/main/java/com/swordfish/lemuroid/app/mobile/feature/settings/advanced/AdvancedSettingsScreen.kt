package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import com.swordfish.lemuroid.common.system.GpuInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.utils.android.settings.*

@Composable
fun AdvancedSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdvancedSettingsViewModel,
    navController: NavHostController,
) {
    val uiState = viewModel.uiState.collectAsState().value

    LemuroidSettingsPage(
        modifier = modifier.fillMaxSize(),
    ) {
        if (uiState?.cache == null) {
            return@LemuroidSettingsPage
        }

        InputSettings()
        GeneralSettings(uiState.cache, viewModel, navController)
        GpuInfoSection()
    }
}

@Composable
private fun InputSettings() {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) },
    ) {
        val rumbleEnabled = booleanPreferenceState(R.string.pref_key_enable_rumble, false)
        LemuroidSettingsSwitch(
            state = rumbleEnabled,
            title = { Text(text = stringResource(id = R.string.settings_title_enable_rumble)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_rumble)) },
        )
        // ... (rest of your input settings)
    }
}

@Composable
private fun GeneralSettings(
    cacheState: AdvancedSettingsViewModel.CacheState,
    viewModel: AdvancedSettingsViewModel,
    navController: NavController,
) {
    val factoryResetDialogState = remember { mutableStateOf(false) }

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) },
    ) {
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_reset_settings)) },
            onClick = { factoryResetDialogState.value = true },
        )
    }

    if (factoryResetDialogState.value) {
        FactoryResetDialog(factoryResetDialogState, viewModel, navController)
    }
}

@Composable
private fun FactoryResetDialog(
    factoryResetDialogState: MutableState<Boolean>,
    viewModel: AdvancedSettingsViewModel,
    navController: NavController,
) {
    // ... (Your existing dialog code)
}

@Composable
private fun GpuInfoSection() {
    val context = LocalContext.current
    val vendor = GpuInfo.getVendor(context)
    val renderer = GpuInfo.getRenderer(context)
    val isMali = vendor == "ARM"

    LemuroidCardSettingsGroup(
        title = { Text("GPU & Performance") }
    ) {
        LemuroidSettingsMenuLink(
            title = { Text("Vendor: $vendor") },
            subtitle = { Text("Renderer: $renderer") },
            onClick = {}
        )

        // Fixed: Passing 'context' to isVulkanSupported
        LemuroidSettingsMenuLink(
            title = { Text("Vulkan Supported: ${GpuInfo.isVulkanSupported(context)}") },
            onClick = {}
        )

        if (isMali) {
            val architecture = GpuInfo.getMaliArchitecture(context)
            
            LemuroidSettingsMenuLink(
                title = { Text("Mali Architecture: ${architecture.generation}") },
                onClick = {}
            )

            // Extensive Feature: Transaction Elimination
            if (architecture.supportsTE) {
                LemuroidSettingsSwitch(
                    state = booleanPreferenceState(R.string.pref_key_mali_te, true),
                    title = { Text("Transaction Elimination") },
                    subtitle = { Text("Reduces power consumption by skipping redundant tile rendering.") }
                )
            }

            // Extensive Feature: AFBC
            if (GpuInfo.supportsAFBC(context)) {
                LemuroidSettingsSwitch(
                    state = booleanPreferenceState(R.string.pref_key_mali_afbc, true),
                    title = { Text("Force AFBC") },
                    subtitle = { Text("Lossless framebuffer compression to reduce memory bandwidth.") }
                )
            }
        }
    }
}
