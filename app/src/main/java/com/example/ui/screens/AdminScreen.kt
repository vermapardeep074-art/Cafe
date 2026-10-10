package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
  isAdminAuthenticated: Boolean,
  storeSettings: StoreSettings,
  menuItems: List<MenuItem>,
  orders: List<Order>,
  auditLogs: List<String>,
  activeCurrency: Currency,
  onAuthenticate: (String) -> Boolean,
  onLogout: () -> Unit,
  onUpdateStoreStatus: (StoreStatus) -> Unit,
  onUpdateItemPrice: (String, Double) -> Unit,
  onToggleItemStock: (String) -> Unit,
  onUpdateMenuItem: (MenuItem) -> Unit,
  onAddNewMenuItem: (MenuItem) -> Unit,
  onDeleteMenuItem: (String) -> Unit,
  onUpdateOrderStatus: (String, OrderStatus) -> Unit,
  onRefundOrder: (String, String) -> Unit,
  onUpdateStoreContact: (phone: String, address: String, hours: String, morningHours: String) -> Unit,
  onUploadDishPhoto: (itemId: String, uri: Uri) -> Boolean,
  onReorderMenuItem: (itemId: String, moveUp: Boolean) -> Unit,
  onUpdateBannerAndTheme: (headline: String, subheadline: String, announcement: String, announcementActive: Boolean, darkTheme: Boolean) -> Unit,
  onUploadCustomLogo: (uri: Uri) -> Boolean,
  onUploadCustomBanner: (uri: Uri) -> Boolean,
  onResetDefaults: () -> Unit,
  targetEditItemId: String? = null,
  onClearTargetEditItem: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var pinInput by remember { mutableStateOf("") }
  var pinVisible by remember { mutableStateOf(false) }
  var loginError by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Menu & Dishes, 1: Banners & Branding, 2: Orders, 3: Hours & Info, 4: Audit Log

  // Category filter in Menu Editor
  var selectedCategoryFilter by remember { mutableStateOf<MenuCategory?>(null) }

  // Dialog state for full dish edit
  var editingItem by remember { mutableStateOf<MenuItem?>(null) }

  // Auto-open target dish editor if requested
  LaunchedEffect(targetEditItemId, menuItems) {
    if (targetEditItemId != null) {
      val found = menuItems.find { it.id == targetEditItemId }
      if (found != null) {
        editingItem = found
        selectedTab = 0
        onClearTargetEditItem()
      }
    }
  }

  // Dialog state for delete confirmation
  var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }

  // Dialog state for refund confirmation
  var refundingOrderId by remember { mutableStateOf<String?>(null) }
  var refundReasonInput by remember { mutableStateOf("Guest requested cancellation before preparation") }

  // Dialog state for add new dish
  var showAddItemDialog by remember { mutableStateOf(false) }

  // Dialog state for factory reset confirmation
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  // Target item for gallery photo picker
  var pickingPhotoForItemId by remember { mutableStateOf<String?>(null) }
  var isUploadingPhoto by remember { mutableStateOf(false) }

  // Photo pickers using ActivityResultContracts.PickVisualMedia
  val dishPhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    val targetId = pickingPhotoForItemId
    if (uri != null && targetId != null) {
      isUploadingPhoto = true
      onUploadDishPhoto(targetId, uri)
      isUploadingPhoto = false
      pickingPhotoForItemId = null
    }
  }

  val logoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onUploadCustomLogo(uri)
    }
  }

  val bannerPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onUploadCustomBanner(uri)
    }
  }

  if (!isAdminAuthenticated) {
    // -------------------------------------------------------------------------
    // PROTECTED ADMIN LOGIN
    // -------------------------------------------------------------------------
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Column(
          modifier = Modifier.padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            color = WarmCaramel.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = WarmCaramel,
                modifier = Modifier.size(34.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "Owner Control Panel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Enter authorized Manager PIN (Default: 1234)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
            value = pinInput,
            onValueChange = {
              pinInput = it
              loginError = false
            },
            placeholder = { Text("Manager PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { pinVisible = !pinVisible }) {
                Icon(
                  imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = "Toggle visibility"
                )
              }
            },
            isError = loginError,
            supportingText = {
              if (loginError) {
                Text("Invalid PIN. Please try again.", color = StatusClosed)
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_pin_input")
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val success = onAuthenticate(pinInput)
              if (!success) loginError = true
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("admin_login_btn")
          ) {
            Text("Unlock Live Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }

          Spacer(modifier = Modifier.height(8.dp))

          TextButton(
            onClick = {
              pinInput = "1234"
              onAuthenticate("1234")
            },
            modifier = Modifier.testTag("admin_quick_demo_btn")
          ) {
            Text("One-Tap Demo Owner Access", color = WarmCaramel, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  } else {
    // -------------------------------------------------------------------------
    // AUTHENTICATED LIVE ADMIN CONSOLE
    // -------------------------------------------------------------------------
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Top Admin Bar
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Artesano Live Admin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                color = LeafGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "LIVE NO-CODE MODE",
                  color = LeafGreenDark,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "90 Chambers St Tribeca · Instant Sync without rebuild",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          TextButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = StatusClosed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sign Out", color = StatusClosed, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Quick Live Store Status Pill
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "STORE STATUS:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StoreStatus.values().forEach { status ->
              val isSelected = storeSettings.status == status
              val activeColor = when (status) {
                StoreStatus.OPEN -> StatusOpen
                StoreStatus.CLOSED -> StatusClosed
                StoreStatus.TEMPORARILY_CLOSED -> StatusTempClosed
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) activeColor else Color.Transparent,
                modifier = Modifier.clickable { onUpdateStoreStatus(status) }
              ) {
                Text(
                  text = status.title,
                  color = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // Tab Navigation
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 12.dp
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Menu & Dishes (${menuItems.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Banners & Theme", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Orders (${orders.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Hours & Info", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 4,
          onClick = { selectedTab = 4 },
          text = { Text("Audit & Reset", fontWeight = FontWeight.Bold) }
        )
      }

      // Tab Contents
      when (selectedTab) {
        0 -> {
          // ===================================================================
          // TAB 0: MENU & DISHES NO-CODE EDITOR
          // ===================================================================
          Column(modifier = Modifier.fillMaxSize()) {
            // Category filter chips + Add Dish button
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Catalogue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Button(
                onClick = { showAddItemDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Dish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Category filter chips
            LazyRow(
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              item {
                FilterChip(
                  selected = selectedCategoryFilter == null,
                  onClick = { selectedCategoryFilter = null },
                  label = { Text("All (${menuItems.size})") }
                )
              }
              items(MenuCategory.values()) { cat ->
                val count = menuItems.count { it.category == cat }
                FilterChip(
                  selected = selectedCategoryFilter == cat,
                  onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                  label = { Text("${cat.title} ($count)") }
                )
              }
            }

            val filteredItems = remember(menuItems, selectedCategoryFilter) {
              if (selectedCategoryFilter == null) menuItems
              else menuItems.filter { it.category == selectedCategoryFilter }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(filteredItems, key = { it.id }) { item ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    // Dish Thumbnail with photo change affordance
                    Box(
                      modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                          pickingPhotoForItemId = item.id
                          dishPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                          )
                        }
                    ) {
                      if (!item.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                          model = item.imageUri,
                          contentDescription = item.name,
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop,
                          placeholder = painterResource(id = item.imageRes),
                          error = painterResource(id = item.imageRes)
                        )
                      } else {
                        Image(
                          painter = painterResource(id = item.imageRes),
                          contentDescription = item.name,
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop
                        )
                      }

                      // Photo overlay icon
                      Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier
                          .align(Alignment.BottomEnd)
                          .size(22.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Upload photo",
                            tint = PureWhite,
                            modifier = Modifier.size(13.dp)
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Dish Details
                    Column(modifier = Modifier.weight(1f)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = item.name,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold
                        )
                        if (item.isChefSpecial) {
                          Spacer(modifier = Modifier.width(4.dp))
                          Surface(
                            color = WarmCaramel.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                          ) {
                            Text(
                              text = "SPECIAL",
                              color = WarmCaramel,
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                          }
                        }
                      }

                      Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                      )

                      Spacer(modifier = Modifier.height(2.dp))

                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = activeCurrency.format(item.priceUsd),
                          style = MaterialTheme.typography.titleSmall,
                          color = WarmCaramel,
                          fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = item.category.title,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    // Actions: In-Stock Switch, Up/Down Reorder, Edit, Delete
                    Column(horizontalAlignment = Alignment.End) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = if (item.inStock) "In Stock" else "Out",
                          fontSize = 10.sp,
                          color = if (item.inStock) LeafGreenDark else StatusClosed,
                          fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                          checked = item.inStock,
                          onCheckedChange = { onToggleItemStock(item.id) },
                          colors = SwitchDefaults.colors(checkedThumbColor = LeafGreen)
                        )
                      }

                      Row {
                        // Reorder Up
                        IconButton(
                          onClick = { onReorderMenuItem(item.id, true) },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                        }
                        // Reorder Down
                        IconButton(
                          onClick = { onReorderMenuItem(item.id, false) },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                        }
                        // Edit Dish
                        IconButton(
                          onClick = { editingItem = item },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.Edit, contentDescription = "Edit Dish", tint = WarmCaramel, modifier = Modifier.size(16.dp))
                        }
                        // Delete Dish
                        IconButton(
                          onClick = { itemToDelete = item },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusClosed, modifier = Modifier.size(16.dp))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          // ===================================================================
          // TAB 1: BANNERS, BRANDING & THEME CUSTOMIZATION
          // ===================================================================
          var headlineInput by remember { mutableStateOf(storeSettings.bannerHeadline) }
          var subheadlineInput by remember { mutableStateOf(storeSettings.bannerSubheadline) }
          var announcementInput by remember { mutableStateOf(storeSettings.announcementText) }
          var announcementActiveState by remember { mutableStateOf(storeSettings.announcementActive) }
          var darkThemeState by remember { mutableStateOf(storeSettings.darkTheme) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Live Website Banners Card
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ViewCarousel, contentDescription = null, tint = WarmCaramel)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "HERO BANNERS & HEADLINES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                  value = headlineInput,
                  onValueChange = { headlineInput = it },
                  label = { Text("Main Banner Headline") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = subheadlineInput,
                  onValueChange = { subheadlineInput = it },
                  label = { Text("Banner Subheadline / Schedule") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Hero Photo Picker
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("Hero Background Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Upload high-res café interior", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  OutlinedButton(
                    onClick = {
                      bannerPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select Photo", fontSize = 12.sp)
                  }
                }
              }
            }

            // Top Announcement Strip Card
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = WarmCaramel)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "TOP ANNOUNCEMENT BANNER",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Switch(
                    checked = announcementActiveState,
                    onCheckedChange = { announcementActiveState = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = WarmCaramel)
                  )
                }

                if (announcementActiveState) {
                  Spacer(modifier = Modifier.height(10.dp))
                  OutlinedTextField(
                    value = announcementInput,
                    onValueChange = { announcementInput = it },
                    label = { Text("Announcement Text (e.g. 10% off with TRIBECA)") },
                    placeholder = { Text("Special announcement shown at top of all pages") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                  )
                }
              }
            }

            // Brand Logo & Theme Card
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Palette, contentDescription = null, tint = WarmCaramel)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "LOGO & VISUAL THEME",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("Café Logo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Shown on header & receipts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  OutlinedButton(
                    onClick = {
                      logoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Logo", fontSize = 12.sp)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("Dark Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Espresso charcoal luxury mode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  Switch(
                    checked = darkThemeState,
                    onCheckedChange = { darkThemeState = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = WarmCaramel)
                  )
                }
              }
            }

            // Publish Changes Button
            Button(
              onClick = {
                onUpdateBannerAndTheme(
                  headlineInput,
                  subheadlineInput,
                  announcementInput,
                  announcementActiveState,
                  darkThemeState
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
            ) {
              Icon(Icons.Default.Save, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Save & Publish Branding Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }
        }

        2 -> {
          // ===================================================================
          // TAB 2: ORDERS MANAGER
          // ===================================================================
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp)
          ) {
            if (orders.isEmpty()) {
              item {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text("No incoming orders yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            } else {
              items(orders, key = { it.id }) { order ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(
                          text = "${order.orderNumber} · ${order.customerName}",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = "${order.serviceMode.title} · ${order.customerPhone}",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Text(
                        text = activeCurrency.format(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmCaramel
                      )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = order.items.joinToString { "${it.quantity}x ${it.item.name}" },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Progression buttons
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      if (order.status == OrderStatus.RECEIVED) {
                        Button(
                          onClick = { onUpdateOrderStatus(order.id, OrderStatus.PREPARING) },
                          colors = ButtonDefaults.buttonColors(containerColor = WarmCaramel),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("Accept & Prepare", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      } else if (order.status == OrderStatus.PREPARING) {
                        Button(
                          onClick = { onUpdateOrderStatus(order.id, OrderStatus.READY) },
                          colors = ButtonDefaults.buttonColors(containerColor = LeafGreenDark),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("Mark Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      } else if (order.status == OrderStatus.READY) {
                        Button(
                          onClick = { onUpdateOrderStatus(order.id, OrderStatus.COMPLETED) },
                          colors = ButtonDefaults.buttonColors(containerColor = StatusOpen),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      }

                      if (order.status != OrderStatus.REFUNDED && order.status != OrderStatus.CANCELLED) {
                        OutlinedButton(
                          onClick = { refundingOrderId = order.id },
                          shape = RoundedCornerShape(8.dp)
                        ) {
                          Text("Refund", color = StatusClosed, fontSize = 11.sp)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        3 -> {
          // ===================================================================
          // TAB 3: STORE HOURS & CONTACT
          // ===================================================================
          var editPhone by remember { mutableStateOf(storeSettings.phone) }
          var editAddress by remember { mutableStateOf(storeSettings.address) }
          var editHours by remember { mutableStateOf(storeSettings.storeHours) }
          var editMorningHours by remember { mutableStateOf(storeSettings.morningHours) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "STORE CONTACT & HOURS",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                  value = editPhone,
                  onValueChange = { editPhone = it },
                  label = { Text("Phone Number") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = editAddress,
                  onValueChange = { editAddress = it },
                  label = { Text("Address & Unit") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = editMorningHours,
                  onValueChange = { editMorningHours = it },
                  label = { Text("Morning Menu Window") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = editHours,
                  onValueChange = { editHours = it },
                  label = { Text("Daily Operating Hours") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    onUpdateStoreContact(editPhone, editAddress, editHours, editMorningHours)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Save Store Settings", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        4 -> {
          // ===================================================================
          // TAB 4: AUDIT LOG & FACTORY RESET
          // ===================================================================
          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "System Audit Trail",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusClosed),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Factory Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
              items(auditLogs) { log ->
                Surface(
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ) {
                  Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // ===========================================================================
  // EDIT DISH DIALOG (FULL NO-CODE ATTRIBUTES)
  // ===========================================================================
  if (editingItem != null) {
    val current = editingItem!!
    var editName by remember { mutableStateOf(current.name) }
    var editDesc by remember { mutableStateOf(current.description) }
    var editPrice by remember { mutableStateOf(String.format("%.2f", current.priceUsd)) }
    var editCategory by remember { mutableStateOf(current.category) }
    var editMorning by remember { mutableStateOf(current.isMorningMenu) }
    var editChefSpecial by remember { mutableStateOf(current.isChefSpecial) }
    var editStock by remember { mutableStateOf(current.inStock) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { editingItem = null },
      title = { Text("Edit Dish: ${current.name}") },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Photo selector in dialog
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
            ) {
              if (!current.imageUri.isNullOrEmpty()) {
                AsyncImage(
                  model = current.imageUri,
                  contentDescription = current.name,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop,
                  placeholder = painterResource(id = current.imageRes),
                  error = painterResource(id = current.imageRes)
                )
              } else {
                Image(
                  painter = painterResource(id = current.imageRes),
                  contentDescription = current.name,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(
              onClick = {
                pickingPhotoForItemId = current.id
                dishPhotoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Change Photo", fontSize = 12.sp)
            }
          }

          OutlinedTextField(
            value = editName,
            onValueChange = {
              editName = it
              nameError = it.isBlank()
            },
            label = { Text("Dish Name *") },
            isError = nameError,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = editDesc,
            onValueChange = { editDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )

          OutlinedTextField(
            value = editPrice,
            onValueChange = {
              editPrice = it
              priceError = (it.toDoubleOrNull() ?: -1.0) <= 0.0
            },
            label = { Text("Price in USD ($) *") },
            isError = priceError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
          )

          // Category picker
          Text("Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(MenuCategory.values()) { cat ->
              FilterChip(
                selected = editCategory == cat,
                onClick = { editCategory = cat },
                label = { Text(cat.title, fontSize = 11.sp) }
              )
            }
          }

          // Flags
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("In Stock Availability", fontSize = 13.sp)
            Switch(checked = editStock, onCheckedChange = { editStock = it })
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Morning Menu Item (8-11 AM)", fontSize = 13.sp)
            Switch(checked = editMorning, onCheckedChange = { editMorning = it })
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Chef's Special Badge", fontSize = 13.sp)
            Switch(checked = editChefSpecial, onCheckedChange = { editChefSpecial = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val priceVal = editPrice.toDoubleOrNull()
            if (editName.isBlank()) {
              nameError = true
              return@Button
            }
            if (priceVal == null || priceVal <= 0.0) {
              priceError = true
              return@Button
            }

            val updated = current.copy(
              name = editName.trim(),
              description = editDesc.trim(),
              priceUsd = priceVal,
              priceInr = priceVal * 83.0,
              category = editCategory,
              isMorningMenu = editMorning,
              isChefSpecial = editChefSpecial,
              inStock = editStock
            )
            onUpdateMenuItem(updated)
            editingItem = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = WarmCaramel)
        ) {
          Text("Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingItem = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // ===========================================================================
  // ADD DISH DIALOG
  // ===========================================================================
  if (showAddItemDialog) {
    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("9.95") }
    var newCategory by remember { mutableStateOf(MenuCategory.SANDWICHES_SAVOURY) }
    var newMorning by remember { mutableStateOf(false) }
    var newSpecial by remember { mutableStateOf(false) }
    var nameErr by remember { mutableStateOf(false) }
    var priceErr by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showAddItemDialog = false },
      title = { Text("Create New Dish") },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = newName,
            onValueChange = {
              newName = it
              nameErr = it.isBlank()
            },
            label = { Text("Dish Name *") },
            isError = nameErr,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newDesc,
            onValueChange = { newDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newPrice,
            onValueChange = {
              newPrice = it
              priceErr = (it.toDoubleOrNull() ?: -1.0) <= 0.0
            },
            label = { Text("Price (USD) *") },
            isError = priceErr,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
          )

          Text("Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(MenuCategory.values()) { cat ->
              FilterChip(
                selected = newCategory == cat,
                onClick = { newCategory = cat },
                label = { Text(cat.title, fontSize = 11.sp) }
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Morning Menu Window", fontSize = 13.sp)
            Switch(checked = newMorning, onCheckedChange = { newMorning = it })
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Chef's Special", fontSize = 13.sp)
            Switch(checked = newSpecial, onCheckedChange = { newSpecial = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val priceVal = newPrice.toDoubleOrNull()
            if (newName.isBlank()) {
              nameErr = true
              return@Button
            }
            if (priceVal == null || priceVal <= 0.0) {
              priceErr = true
              return@Button
            }

            val newItem = MenuItem(
              id = "item_${System.currentTimeMillis()}",
              name = newName.trim(),
              description = newDesc.trim(),
              priceUsd = priceVal,
              priceInr = priceVal * 83.0,
              category = newCategory,
              isMorningMenu = newMorning,
              isChefSpecial = newSpecial,
              inStock = true
            )
            onAddNewMenuItem(newItem)
            showAddItemDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Create Dish")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddItemDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // ===========================================================================
  // DESTRUCTIVE CONFIRMATION: DELETE DISH
  // ===========================================================================
  if (itemToDelete != null) {
    val target = itemToDelete!!
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusClosed) },
      title = { Text("Delete Dish?") },
      text = {
        Text("Are you sure you want to permanently delete '${target.name}' ($${String.format("%.2f", target.priceUsd)}) from the menu? This action cannot be undone.")
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteMenuItem(target.id)
            itemToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusClosed)
        ) {
          Text("Confirm Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // ===========================================================================
  // DESTRUCTIVE CONFIRMATION: ORDER REFUND
  // ===========================================================================
  if (refundingOrderId != null) {
    AlertDialog(
      onDismissRequest = { refundingOrderId = null },
      icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = StatusClosed) },
      title = { Text("Issue Order Refund") },
      text = {
        Column {
          Text("Order ID: $refundingOrderId", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = refundReasonInput,
            onValueChange = { refundReasonInput = it },
            label = { Text("Reason for Refund") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onRefundOrder(refundingOrderId!!, refundReasonInput)
            refundingOrderId = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusClosed)
        ) {
          Text("Confirm Refund")
        }
      },
      dismissButton = {
        TextButton(onClick = { refundingOrderId = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // ===========================================================================
  // DESTRUCTIVE CONFIRMATION: FACTORY RESET
  // ===========================================================================
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusClosed) },
      title = { Text("Restore Factory Defaults?") },
      text = {
        Text("This will reset all menu items, prices, uploaded photographs, and store settings back to the initial authentic Tribeca printed menu. All custom changes will be erased.")
      },
      confirmButton = {
        Button(
          onClick = {
            onResetDefaults()
            showResetConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusClosed)
        ) {
          Text("Reset Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
