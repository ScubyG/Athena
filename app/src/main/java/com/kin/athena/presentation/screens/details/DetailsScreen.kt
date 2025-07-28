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

package com.kin.athena.presentation.screens.details

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.RuleFolder
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kin.athena.R
import com.kin.athena.core.utils.extensions.getAppNameFromPackage
import com.kin.athena.core.utils.extensions.getApplicationIcon
import com.kin.athena.core.utils.extensions.getApplicationName
import com.kin.athena.core.utils.extensions.getPermissions
import com.kin.athena.core.utils.extensions.safeNavigate
import com.kin.athena.core.utils.extensions.toBitmap
import com.kin.athena.core.utils.extensions.toFormattedDateTime
import com.kin.athena.core.utils.extensions.toPackageInfo
import com.kin.athena.domain.model.Application
import com.kin.athena.presentation.components.CircleWrapper
import com.kin.athena.presentation.components.material.MaterialBar
import com.kin.athena.presentation.components.material.MaterialScaffold
import com.kin.athena.presentation.components.material.MaterialText
import com.kin.athena.presentation.navigation.routes.LogRoutes
import com.kin.athena.presentation.screens.details.components.NetworkOption
import com.kin.athena.presentation.screens.details.viewModel.DetailsUiState
import com.kin.athena.presentation.screens.details.viewModel.DetailsViewModel
import com.kin.athena.presentation.screens.settings.components.IconType
import com.kin.athena.presentation.screens.settings.components.ListDialog
import com.kin.athena.presentation.screens.settings.components.RenderCustomIcon
import com.kin.athena.presentation.screens.settings.components.SettingType
import com.kin.athena.presentation.screens.settings.components.SettingsBox
import com.kin.athena.presentation.screens.settings.components.settingsContainer
import com.kin.athena.presentation.screens.settings.viewModel.SettingsViewModel

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    settings: SettingsViewModel,
    navController: NavController,
) {
    val uiState = viewModel.uiState.value
    val context = LocalContext.current

    when (uiState) {
        is DetailsUiState.Success -> { ApplicationContent(application = uiState.application, context = context, onBack, viewModel, settings, navController) }
        else -> { /* Don't do anything */ }
    }
}


@Composable
fun ApplicationContent(application: Application, context: Context, onBack: () -> Unit, viewModel: DetailsViewModel, settings: SettingsViewModel, navController: NavController) {
    MaterialScaffold(
        topBar = {
            MaterialBar(
                title = stringResource(id = R.string.details),
                onBackNavClicked = { onBack() }
            )
        },
        content = {
            PackageDetails(packageEntity = application, context = context, viewModel = viewModel, settings = settings, navController = navController)
        }
    )
}

@Suppress("DEPRECATION")
@Composable
private fun PackageDetails(
    viewModel: DetailsViewModel,
    packageEntity: Application,
    context: Context,
    settings: SettingsViewModel,
    navController: NavController
) {
    val packageInfo = packageEntity.toPackageInfo(context.packageManager)
    val title = packageEntity.getApplicationName(context.packageManager)

    val icon = packageEntity.getApplicationIcon(
        context.packageManager,
        tintColor = MaterialTheme.colorScheme.onSurface.toArgb(),
        useDynamicIcon = settings.settings.value.useDynamicIcons,
        context
    )

    @Composable
    fun showIcon() {
        icon?.let {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .height(68.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                if (settings.settings.value.useDynamicIcons) {
                    CircleWrapper(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        size = 7.dp
                    ) {
                        showIcon()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    showIcon()
                }
                title?.let {
                    MaterialText(
                        title = title,
                        description = packageEntity.packageID,
                        center = true,
                        titleSize = 22.sp,
                        descriptionSize = 13.sp
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                NetworkOption(
                    icon = Icons.Rounded.Wifi,
                    label = "WiFi",
                    cornerShape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
                    onClick = {
                        viewModel.updatePackage(packageEntity.copy(internetAccess = !packageEntity.internetAccess))
                    },
                    color = if (packageEntity.internetAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(1.dp))
                NetworkOption(
                    icon = Icons.Rounded.SignalCellularAlt,
                    label = "Cellular",
                    cornerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                    onClick = { viewModel.updatePackage(packageEntity.copy(cellularAccess = !packageEntity.cellularAccess)) },
                    color = if (packageEntity.cellularAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
        packageInfo?.let {
            settingsContainer {
                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.Numbers),
                    title = stringResource(id = R.string.uid),
                    description = packageEntity.uid.toString(),
                    actionType = SettingType.TEXT
                )
                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.Info),
                    title = stringResource(id = R.string.version),
                    description = it.versionName,
                    actionType = SettingType.TEXT
                )
            }
            settingsContainer {
                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.Timer),
                    title = stringResource(id = R.string.installation_time),
                    description = it.firstInstallTime.toFormattedDateTime(),
                    actionType = SettingType.TEXT
                )
                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.Update),
                    title = stringResource(id = R.string.last_updated_time),
                    description = it.lastUpdateTime.toFormattedDateTime(),
                    actionType = SettingType.TEXT
                )
            }
            val permissions = packageEntity.getPermissions(context.packageManager)!!
            settingsContainer {
                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.RuleFolder),
                    title = stringResource(id = R.string.permissions),
                    description = permissions.size.toString(),
                    actionType = SettingType.CUSTOM,
                    customAction = { onExit ->
                        if (permissions.isNotEmpty()) {
                            ListDialog(
                                text = stringResource(id = R.string.permissions),
                                list = permissions,
                                onExit = { onExit() },
                                extractDisplayData = { it },
                            ) { displayData ->
                                SettingsBox(
                                    title = displayData.first,
                                    actionType = SettingType.TEXT,
                                    description = displayData.second
                                )
                            }
                        }
                    },
                    customButton = {
                        if (permissions.isNotEmpty()) {
                            RenderCustomIcon()
                        }
                    }
                )
                val installedBy =
                    context.packageManager.getInstallerPackageName(packageEntity.packageID)
                        ?.let { context.getAppNameFromPackage(it) }

                SettingsBox(
                    icon = IconType.VectorIcon(Icons.Rounded.InstallMobile),
                    title = stringResource(id = R.string.installed),
                    isEnabled = installedBy != null,
                    description = installedBy,
                    actionType = SettingType.TEXT
                )
            }
            settingsContainer {
                if (viewModel.topDomainsAndIps.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Most established connections",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight(530)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        ) {
                            viewModel.topDomainsAndIps.value.forEach { pair: Triple<String, Int, String> ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(16.dp)
                                        .clickable {
                                            navController.safeNavigate(LogRoutes.Packet.createRoute(pair.third.toInt()))
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (pair.first.length > 20) "${pair.first.take(20)}..." else pair.first
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = pair.second.toString())
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowForwardIos,
                                        contentDescription = null,
                                        modifier = Modifier.scale(0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}