package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.LuxuryMenuItemCard
import com.example.ui.theme.*

@Composable
fun AccountScreen(
  customerName: String,
  customerPhone: String,
  orders: List<Order>,
  favorites: Set<String>,
  menuItems: List<MenuItem>,
  activeCurrency: Currency,
  darkTheme: Boolean,
  onToggleCurrency: () -> Unit,
  onToggleTheme: () -> Unit,
  onTrackOrder: (Order) -> Unit,
  onItemClick: (MenuItem) -> Unit,
  onQuickAdd: (MenuItem) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onOpenAdmin: () -> Unit = {},
  onEditItem: (MenuItem) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val favoriteItems = menuItems.filter { favorites.contains(it.id) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Profile Header Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(WarmCaramel),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = customerName.firstOrNull()?.toString() ?: "A",
              style = MaterialTheme.typography.titleLarge,
              color = PureWhite,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = customerName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = customerPhone,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              color = SoftGold.copy(alpha = 0.2f),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.padding(top = 4.dp)
            ) {
              Text(
                text = "ARTESANO TRIBECA CLUB MEMBER",
                color = WarmCaramel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    // 2. Settings Strip (Currency & Theme)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "APP PREFERENCES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Currency switch
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Display Currency", fontWeight = FontWeight.SemiBold)
              Text("USD ($) or Indian Rupee (₹)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(
              onClick = onToggleCurrency,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(if (activeCurrency == Currency.USD) "$ USD (Active)" else "₹ INR (Active)", fontWeight = FontWeight.Bold)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGold)

          // Dark Theme toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Luxury Dark Theme", fontWeight = FontWeight.SemiBold)
              Text("Rich espresso charcoal aesthetic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = darkTheme,
              onCheckedChange = { onToggleTheme() },
              colors = SwitchDefaults.colors(checkedThumbColor = WarmCaramel)
            )
          }
        }
      }
    }

    // 2B. Owner & Staff Administration
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = WarmCaramel,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "CAFE OWNER PORTAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
              )
            }
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = LeafGreenDark.copy(alpha = 0.15f)
            ) {
              Text(
                text = "PIN: 1234",
                style = MaterialTheme.typography.labelSmall,
                color = LeafGreenDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Live Menu, Price & Banner CMS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Update photography, edit prices, mark stock availability, manage kitchen orders, and modify website banners.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onOpenAdmin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
              containerColor = WarmCaramel,
              contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Visual Admin CMS", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Past Orders & Reorder
    item {
      Text(
        text = "Order History (${orders.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    if (orders.isEmpty()) {
      item {
        Text("No past orders yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      items(orders, key = { it.id }) { order ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${order.orderNumber} · ${order.status.label}",
                fontWeight = FontWeight.Bold,
                color = WarmCaramel
              )
              Text(
                text = activeCurrency.format(order.total),
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = order.items.joinToString { "${it.quantity}x ${it.item.name}" },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = { onTrackOrder(order) },
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Status & Receipt")
              }
            }
          }
        }
      }
    }

    // 4. Saved Favorites
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Your Saved Favorites (${favoriteItems.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    if (favoriteItems.isEmpty()) {
      item {
        Text("Tap the heart on any menu item to save your favorites.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      items(favoriteItems, key = { it.id }) { item ->
        LuxuryMenuItemCard(
          item = item,
          activeCurrency = activeCurrency,
          isFavorite = true,
          onItemClick = { onItemClick(item) },
          onQuickAdd = { onQuickAdd(item) },
          onToggleFavorite = { onToggleFavorite(item.id) },
          onEditItem = { onEditItem(item) }
        )
      }
    }
  }
}
