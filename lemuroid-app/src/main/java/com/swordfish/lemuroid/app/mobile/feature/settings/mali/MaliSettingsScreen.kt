package com.swordfish.lemuroid.app.mobile.feature.settings.mali

import androidx.compose.foundation.layout.Column
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
        title = stringResource(R.string.settings_title_mali_te), // Passing string directly
        onBackClick = { navController.popBackStack() }
    ) {
        // Essential: Wrapping in Column fixes the '@Composable invocations' error
        Column {
            LemuroidCardSettingsGroup(
                title = { Text(text = "Hardware Performance") }
            ) {
                if (architecture.supportsTE) {
                    LemuroidSettingsSwitch(
                        state = booleanPreferenceState(R.string.pref_key_mali_te, true),
                        title = { Text(text = stringResource(R.string.settings_title_mali_te)) },
                        subtitle = { Text(text = stringResource(R.string.settings_description_mali_te)) }
                    )
                }

                if (GpuInfo.supportsAFBC(context)) {
                    LemuroidSettingsSwitch(
                        state = booleanPreferenceState(R.string.pref_key_mali_afbc, true),
                        title = { Text(text = "Force AFBC") },
                        subtitle = { Text(text = "Enable Arm Frame Buffer Compression for supported cores") }
                    )
                }
            }

            LemuroidCardSettingsGroup(
                title = { Text(text = "GPU Diagnostics") }
            ) {
                LemuroidSettingsMenuLink(
                    title = { Text(text = "Renderer") },
                    subtitle = { Text(text = GpuInfo.getRenderer(context)) },
                    onClick = {}
                )
            }
        }
    }
}
