package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ProtectionStatus
import com.example.model.SupportedApp
import com.example.ui.components.ActionConfirmDialog
import com.example.ui.components.AppBrandBadge
import com.example.ui.components.ProtectionStatusBadge
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusAllowedSlate
import com.example.ui.theme.StatusProtectedGreen
import com.example.ui.theme.StatusSetupAmber
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ReelGuardViewModel

enum class AppFilter(val label: String) {
    ALL("All"),
    BLOCKED("Blocked"),
    ALLOWED("Allowed"),
    SETUP_REQUIRED("Setup Required")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedAppsScreen(
    viewModel: ReelGuardViewModel,
    onNavigateToAppDetail: (String) -> Unit
) {
    val supportedApps by viewModel.supportedApps.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilter.ALL) }
    var showBlockAllDialog by remember { mutableStateOf(false) }
    var showAllowAllDialog by remember { mutableStateOf(false) }

    // Filtered apps based on query and filter tab
    val filteredApps = supportedApps.filter { app ->
        val matchesQuery = app.name.contains(searchQuery, ignoreCase = true) ||
                app.feedType.contains(searchQuery, ignoreCase = true)

        val status = app.calculateStatus(isServiceActive)
        val matchesFilter = when (selectedFilter) {
            AppFilter.ALL -> true
            AppFilter.BLOCKED -> app.isBlocked
            AppFilter.ALLOWED -> !app.isBlocked
            AppFilter.SETUP_REQUIRED -> status == ProtectionStatus.SETUP_REQUIRED
        }

        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supported Apps",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Control Shorts & Reels feeds independently for each application.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apps_search_field"),
                placeholder = { Text("Search apps or feed types (e.g. YouTube, Reels)...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                singleLine = true
            )
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppFilter.values()) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            val count = when (filter) {
                                AppFilter.ALL -> supportedApps.size
                                AppFilter.BLOCKED -> supportedApps.count { it.isBlocked }
                                AppFilter.ALLOWED -> supportedApps.count { !it.isBlocked }
                                AppFilter.SETUP_REQUIRED -> supportedApps.count { it.calculateStatus(isServiceActive) == ProtectionStatus.SETUP_REQUIRED }
                            }
                            Text("${filter.label} ($count)", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = DarkSurfaceBorder,
                            selectedBorderColor = VioletPrimary,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Bulk Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bulk Controls",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${supportedApps.count { it.isBlocked }} of ${supportedApps.size} blocked",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showBlockAllDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_bulk_block_all")
                        ) {
                            Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Block All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showAllowAllDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_bulk_allow_all")
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Allow All", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Apps list
        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No apps match current filters",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing the search query or changing filter tabs.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.id }) { app ->
                SupportedAppItemCard(
                    app = app,
                    isServiceActive = isServiceActive,
                    onToggleBlocked = { blocked ->
                        viewModel.toggleAppBlocked(app.id, blocked)
                    },
                    onClick = {
                        onNavigateToAppDetail(app.id)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmation dialogs for bulk actions
    if (showBlockAllDialog) {
        ActionConfirmDialog(
            title = "Block All Feeds?",
            message = "This will configure all supported short-video feeds to Blocked mode. You can adjust any app individually at any time.",
            confirmText = "Block All",
            onConfirm = {
                viewModel.blockAllApps()
                showBlockAllDialog = false
            },
            onDismiss = { showBlockAllDialog = false }
        )
    }

    if (showAllowAllDialog) {
        ActionConfirmDialog(
            title = "Allow All Feeds?",
            message = "This will set all feeds to Allowed. No feeds will be blocked until re-enabled.",
            confirmText = "Allow All",
            onConfirm = {
                viewModel.allowAllApps()
                showAllowAllDialog = false
            },
            onDismiss = { showAllowAllDialog = false }
        )
    }
}

@Composable
fun SupportedAppItemCard(
    app: SupportedApp,
    isServiceActive: Boolean,
    onToggleBlocked: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val status = app.calculateStatus(isServiceActive)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("app_card_${app.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppBrandBadge(appName = app.name)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${app.feedType} Feed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Operational status badge
                    ProtectionStatusBadge(status = status)

                    // Independent ON/OFF switch
                    Switch(
                        checked = app.isBlocked,
                        onCheckedChange = { isChecked ->
                            onToggleBlocked(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VioletPrimary,
                            uncheckedThumbColor = StatusAllowedSlate,
                            uncheckedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("switch_app_${app.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Honest operational summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val requestedLabel = if (app.isBlocked) "User setting: Blocked" else "User setting: Allowed"
                Text(
                    text = requestedLabel,
                    fontSize = 11.sp,
                    color = if (app.isBlocked) StatusProtectedGreen else StatusAllowedSlate,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Details",
                        fontSize = 11.sp,
                        color = VioletPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = VioletPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
