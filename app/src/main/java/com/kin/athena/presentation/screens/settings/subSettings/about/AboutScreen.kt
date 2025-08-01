/*
 * Copyright (C) 2025 Vexzure
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.kin.athena.presentation.screens.settings.subSettings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ContactSupport
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kin.athena.R
import com.kin.athena.core.utils.constants.ProjectConstants
import com.kin.athena.core.utils.extensions.safeNavigate
import com.kin.athena.presentation.components.material.MaterialTextField
import com.kin.athena.presentation.navigation.routes.HomeRoutes
import com.kin.athena.presentation.screens.settings.components.IconType
import com.kin.athena.presentation.screens.settings.components.SettingDialog
import com.kin.athena.presentation.screens.settings.components.SettingType
import com.kin.athena.presentation.screens.settings.components.SettingsBox
import com.kin.athena.presentation.screens.settings.components.SettingsScaffold
import com.kin.athena.presentation.screens.settings.components.settingsContainer
import com.kin.athena.presentation.screens.settings.subSettings.about.components.LogoWithBlob
import com.kin.athena.presentation.screens.settings.subSettings.about.components.SettingsVerification
import com.kin.athena.presentation.screens.settings.viewModel.SettingsViewModel
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.rounded.Numbers

@Composable
fun AboutScreen(
    navController: NavController,
    settings: SettingsViewModel,
) {
    val uriHandler = LocalUriHandler.current
    SettingsScaffold(
        settings = settings,
        title = stringResource(id = R.string.about_title),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        item {
            LogoWithBlob {
                navController.safeNavigate(HomeRoutes.Debug.route)
            }
        }
        item {
            SettingsVerification(
                isValid = settings.getAppSignature() == ProjectConstants.SHA_256_SIGNING,
                title = stringResource(id = R.string.verified_as_official_build),
                description = stringResource(id = R.string.maintained_by) + " " + ProjectConstants.DEVELOPER
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(id = R.string.translator),
                description = stringResource(id = R.string.your_name),
                actionType = SettingType.TEXT,
                icon = IconType.VectorIcon(Icons.Rounded.Translate),
            )
            SettingsBox(
                title = stringResource(id = R.string.version),
                description = settings.version,
                icon = IconType.VectorIcon(Icons.Rounded.Info),
                actionType = SettingType.TEXT,
            )
            SettingsBox(
                title = stringResource(id = R.string.build_type),
                description = settings.build,
                icon = IconType.VectorIcon(Icons.Rounded.Build),
                actionType = SettingType.TEXT,
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(id = R.string.latest_release),
                icon = IconType.VectorIcon(Icons.Rounded.Verified),
                actionType = SettingType.LINK,
                onLinkClicked = { uriHandler.openUri(ProjectConstants.PROJECT_DOWNLOADS) },
            )
            SettingsBox(
                title = stringResource(id = R.string.source_code),
                icon = IconType.VectorIcon(Icons.Rounded.Download),
                actionType = SettingType.LINK,
                onLinkClicked = { uriHandler.openUri(ProjectConstants.PROJECT_SOURCE_CODE) }
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(id = R.string.email),
                icon = IconType.VectorIcon(Icons.Rounded.Email),
                clipboardText = ProjectConstants.SUPPORT_MAIL,
                actionType = SettingType.CLIPBOARD,
            )
            SettingsBox(
                title = stringResource(id = R.string.discord),
                icon = IconType.VectorIcon(Icons.AutoMirrored.Rounded.ContactSupport),
                actionType = SettingType.LINK,
                onLinkClicked = { uriHandler.openUri(ProjectConstants.SUPPORT_DISCORD) },
            )
            SettingsBox(
                title = stringResource(id = R.string.feature),
                icon = IconType.VectorIcon(Icons.Rounded.BugReport),
                onLinkClicked = { uriHandler.openUri(ProjectConstants.GITHUB_FEATURE_REQUEST) },
                actionType = SettingType.LINK,
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(id = R.string.premium_code),
                icon = IconType.VectorIcon(Icons.Rounded.Numbers),
                actionType = SettingType.CUSTOM,
                customAction = { onExit ->
                    PremiumCodeDialog(
                        settings =  settings,
                        onExit = { onExit() },
                    )
                }
            )
        }
    }
}

@Composable
fun PremiumCodeDialog(
    onExit: () -> Unit,
    settings: SettingsViewModel
) {
    var code by remember { mutableStateOf(TextFieldValue("")) }

    SettingDialog(
        text = stringResource(id = R.string.premium_code),
        onExit = { onExit() }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                MaterialTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = code,
                    onValueChange = { code = it },
                    placeholder = "Enter premium code",
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    settings.verifyLicense(code.text)
                    onExit()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(id = R.string.done))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}