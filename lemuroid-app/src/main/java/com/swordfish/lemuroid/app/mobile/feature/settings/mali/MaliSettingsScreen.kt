package com.swordfish.lemuroid.app.mobile.feature.settings.mali

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.*
import com.swordfish.lemuroid.common.system.GpuInfo

@Composable
fun MaliSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val architecture = GpuInfo.getMaliArchitecture(context)

    LemuroidSettingsPage(
        modifier = modifier.fillMaxSize(),
        title = { Text("Mali Extensive Features") },
        onBackClick = { navController.popBackStack() }
    ) {
        LemuroidCardSettingsGroup(title = { Text("Architecture: ${architecture.generation}") }) {
            // Transaction Elimination
            if (architecture.supportsTE) {
                LemuroidSettingsSwitch(
                    state = booleanPreferenceState(R.string.pref_key_mali_te, true),
                    title = { Text(text = stringResource(R.string.settings_title_mali_te)) },
                    subtitle = { Text(text = stringResource(R.string.settings_description_mali_te)) }
                )
            }

            // ARM Frame Buffer Compression
            if (GpuInfo.supportsAFBC(context)) {
                LemuroidSettingsSwitch(
                    state = booleanPreferenceState(R.string.pref_key_mali_afbc, true),
                    title = { Text(text = stringResource(R.string.settings_title_mali_afbc)) },
                    subtitle = { Text(text = stringResource(R.string.settings_description_mali_afbc)) }
                )
            }
        }

        LemuroidCardSettingsGroup(title = { Text("Device Diagnostics") }) {
            LemuroidSettingsMenuLink(
                title = { Text("Vulkan Support") },
                subtitle = { Text(if (GpuInfo.isVulkanSupported(context)) "Available" else "Not Supported") },
                onClick = {}
            )
            LemuroidSettingsMenuLink(
                title = { Text("GPU Renderer") },
                subtitle = { Text(GpuInfo.getRenderer(context)) },
                onClick = {}
            )
        }
    }
}
