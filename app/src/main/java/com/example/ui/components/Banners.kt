package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Currency
import com.example.data.StoreStatus
import com.example.ui.AppDestination
import com.example.ui.theme.*

@Composable
fun MorningMenuHighlightBanner(
  onViewMorningMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onViewMorningMenu() }
      .testTag("morning_menu_banner"),
    colors = CardDefaults.cardColors(
      containerColor = WarmCaramel.copy(alpha = 0.12f)
    ),
    shape = RoundedCornerShape(14.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        color = WarmCaramel,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(44.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "MORNING MENU",
            style = MaterialTheme.typography.labelSmall,
            color = WarmCaramel,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = SoftGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "8:00 AM – 11:00 AM",
              style = MaterialTheme.typography.labelSmall,
              color = EspressoBrown,
              fontWeight = FontWeight.SemiBold,
              fontSize = 9.sp,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }
        }
        Text(
          text = "Maple Cinnamon Latte, Sourdough B.E.C. & Fresh Pastries",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Medium
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "View Morning Menu",
        tint = WarmCaramel
      )
    }
  }
}

@Composable
fun KfcStyleStickyCartBar(
  itemCount: Int,
  subtotalUsd: Double,
  activeCurrency: Currency,
  onOpenCart: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = itemCount > 0,
    enter = slideInVertically(initialOffsetY = { it }),
    exit = slideOutVertically(targetOffsetY = { it }),
    modifier = modifier
  ) {
    Surface(
      color = MaterialTheme.colorScheme.primary,
      shape = RoundedCornerShape(18.dp),
      tonalElevation = 8.dp,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .clickable { onOpenCart() }
        .testTag("kfc_sticky_cart_bar")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = WarmCaramel,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "$itemCount",
              color = PureWhite,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelLarge,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "View Order",
              color = PureWhite,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Tribeca Express Checkout",
              color = SoftGoldLight,
              style = MaterialTheme.typography.labelSmall
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = activeCurrency.format(subtotalUsd),
            color = PureWhite,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = SoftGold,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ArtesanoBottomNav(
  currentScreen: AppDestination,
  cartCount: Int,
  onNavigate: (AppDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    windowInsets = WindowInsets.navigationBars,
    modifier = modifier.testTag("bottom_nav_bar")
  ) {
    val items = listOf(
      Triple(AppDestination.HOME, "Home", Icons.Default.Home to Icons.Outlined.Home),
      Triple(AppDestination.MENU, "Menu", Icons.Default.RestaurantMenu to Icons.Outlined.RestaurantMenu),
      Triple(AppDestination.CART, "Cart", Icons.Default.ShoppingBag to Icons.Outlined.ShoppingBag),
      Triple(AppDestination.TRACKING, "Track", Icons.Default.LocalShipping to Icons.Outlined.LocalShipping),
      Triple(AppDestination.ABOUT, "About", Icons.Default.Storefront to Icons.Outlined.Storefront),
      Triple(AppDestination.ACCOUNT, "Account", Icons.Default.Person to Icons.Outlined.Person)
    )

    items.forEach { (destination, label, iconPair) ->
      val selected = currentScreen == destination
      NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(destination) },
        icon = {
          BadgedBox(
            badge = {
              if (destination == AppDestination.CART && cartCount > 0) {
                Badge(
                  containerColor = WarmCaramel,
                  contentColor = PureWhite
                ) {
                  Text(text = cartCount.toString(), fontSize = 10.sp)
                }
              }
            }
          ) {
            Icon(
              imageVector = if (selected) iconPair.first else iconPair.second,
              contentDescription = label
            )
          }
        },
        label = {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = SoftGold.copy(alpha = 0.25f),
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
      )
    }
  }
}
