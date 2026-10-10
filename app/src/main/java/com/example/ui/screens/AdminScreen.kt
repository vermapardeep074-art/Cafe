package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

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
  modifier: Modifier = Modifier
) {
  var pinInput by remember { mutableStateOf("") }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Menu & Prices, 2: Hours & Info, 3: Audit Log

  // Dialog state for price edit
  var editingItem by remember { mutableStateOf<MenuItem?>(null) }
  var editPriceInput by remember { mutableStateOf("") }

  // Dialog state for refund
  var refundingOrderId by remember { mutableStateOf<String?>(null) }
  var refundReasonInput by remember { mutableStateOf("Guest requested cancellation before preparation") }

  // Dialog state for adding new item
  var showAddItemDialog by remember { mutableStateOf(false) }

  if (!isAdminAuthenticated) {
    // PIN Login Screen
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            color = WarmCaramel.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(60.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = WarmCaramel,
                modifier = Modifier.size(32.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Owner Control Panel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Enter your Manager PIN (Default: 1234)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
            value = pinInput,
            onValueChange = { pinInput = it },
            placeholder = { Text("Manager PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_pin_input")
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { onAuthenticate(pinInput) },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("admin_login_btn")
          ) {
            Text("Unlock Dashboard", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          TextButton(
            onClick = { onAuthenticate("1234") },
            modifier = Modifier.testTag("admin_quick_demo_btn")
          ) {
            Text("One-Tap Demo Owner Access", color = WarmCaramel)
          }
        }
      }
    }
  } else {
    // Authenticated Dashboard
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Header
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
                text = "Owner Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                color = LeafGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "OWNER MODE",
                  color = LeafGreenDark,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Artesano Cafe · 90 Chambers St Unit B",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          TextButton(onClick = onLogout) {
            Text("Sign Out", color = StatusClosed, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Live Store Status Controller
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "LIVE STORE STATUS (INSTANT EFFECT)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            StoreStatus.values().forEach { status ->
              val isSelected = storeSettings.status == status
              val activeColor = when (status) {
                StoreStatus.OPEN -> StatusOpen
                StoreStatus.CLOSED -> StatusClosed
                StoreStatus.TEMPORARILY_CLOSED -> StatusTempClosed
              }
              FilledTonalButton(
                onClick = { onUpdateStoreStatus(status) },
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                  contentColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("admin_status_${status.name.lowercase()}")
              ) {
                Text(
                  text = status.title,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // Tab switcher
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Orders (${orders.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Menu & Prices", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Store Info", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Audit Log", fontWeight = FontWeight.Bold) }
        )
      }

      // Tab Content
      when (selectedTab) {
        0 -> {
          // Orders Manager
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
                    .testTag("admin_order_${order.id}")
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

        1 -> {
          // Menu & Price Manager
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp)
          ) {
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Catalogue (${menuItems.size} items)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Button(
                  onClick = { showAddItemDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            items(menuItems, key = { it.id }) { item ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                      )
                      if (!item.inStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "OUT OF STOCK",
                          color = StatusClosed,
                          style = MaterialTheme.typography.labelSmall,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                    Text(
                      text = "${item.category.title} · ${activeCurrency.format(item.priceUsd)}",
                      style = MaterialTheme.typography.labelSmall,
                      color = WarmCaramel
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stock toggle
                    Switch(
                      checked = item.inStock,
                      onCheckedChange = { onToggleItemStock(item.id) },
                      colors = SwitchDefaults.colors(checkedThumbColor = WarmCaramel)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Edit Price button
                    IconButton(onClick = {
                      editingItem = item
                      editPriceInput = String.format("%.2f", item.priceUsd)
                    }) {
                      Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = WarmCaramel)
                    }

                    // Delete item
                    IconButton(onClick = { onDeleteMenuItem(item.id) }) {
                      Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusClosed)
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          // Store Contact & Hours Editor
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

        3 -> {
          // Audit Log
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp)
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

  // Price Edit Dialog
  if (editingItem != null) {
    AlertDialog(
      onDismissRequest = { editingItem = null },
      title = { Text("Edit Price: ${editingItem?.name}") },
      text = {
        OutlinedTextField(
          value = editPriceInput,
          onValueChange = { editPriceInput = it },
          label = { Text("Price in USD ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val newP = editPriceInput.toDoubleOrNull()
            if (newP != null && newP > 0) {
              onUpdateItemPrice(editingItem!!.id, newP)
            }
            editingItem = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = WarmCaramel)
        ) {
          Text("Save Price")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingItem = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Refund Dialog
  if (refundingOrderId != null) {
    AlertDialog(
      onDismissRequest = { refundingOrderId = null },
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

  // Add Item Dialog
  if (showAddItemDialog) {
    var newItemName by remember { mutableStateOf("") }
    var newItemDesc by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("6.50") }
    var newCategory by remember { mutableStateOf(MenuCategory.PASTRIES) }

    AlertDialog(
      onDismissRequest = { showAddItemDialog = false },
      title = { Text("Create New Menu Item") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newItemDesc,
            onValueChange = { newItemDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newItemPrice,
            onValueChange = { newItemPrice = it },
            label = { Text("Price (USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val priceVal = newItemPrice.toDoubleOrNull() ?: 5.0
            val newItem = MenuItem(
              id = "custom_${System.currentTimeMillis()}",
              name = newItemName.ifBlank { "Artisan Creation" },
              description = newItemDesc.ifBlank { "Handmade in our Tribeca atelier" },
              priceUsd = priceVal,
              priceInr = priceVal * 83.0,
              category = newCategory
            )
            onAddNewMenuItem(newItem)
            showAddItemDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddItemDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
