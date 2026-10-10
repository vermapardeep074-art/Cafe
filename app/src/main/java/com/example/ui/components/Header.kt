package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Currency
import com.example.data.StoreStatus
import com.example.ui.AppDestination
import com.example.ui.theme.*

@Composable
fun ArtesanoTopBar(
  currentScreen: AppDestination,
  cartCount: Int,
  storeStatus: StoreStatus,
  activeCurrency: Currency,
  darkTheme: Boolean,
  customLogoUri: String? = null,
  announcementText: String? = null,
  announcementActive: Boolean = false,
  onNavigate: (AppDestination) -> Unit,
  onToggleCurrency: () -> Unit,
  onToggleTheme: () -> Unit,
  onOpenAdmin: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp,
    shadowElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      // Top announcement banner if active
      if (announcementActive && !announcementText.isNullOrBlank()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(WarmCaramel)
            .padding(horizontal = 16.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Campaign,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = announcementText,
            color = PureWhite,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Top status indicator strip if not open
      if (storeStatus != StoreStatus.OPEN) {
        val bannerBg = if (storeStatus == StoreStatus.TEMPORARILY_CLOSED) StatusTempClosed else StatusClosed
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(bannerBg)
            .padding(horizontal = 16.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = storeStatus.badgeText,
            color = PureWhite,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Main Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Brand Title & Tagline
        Row(
          modifier = Modifier
            .clickable { onNavigate(AppDestination.HOME) }
            .testTag("brand_header"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!customLogoUri.isNullOrEmpty()) {
            coil.compose.AsyncImage(
              model = customLogoUri,
              contentDescription = "Logo",
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "ARTESANO",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = SoftGold.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "CAFE",
                  color = WarmCaramel,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Ristorante Italiano · Tribeca NYC",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.5.sp
            )
          }
        }

        // Action controls
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Currency Switcher Button (USD / INR)
          OutlinedButton(
            onClick = onToggleCurrency,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("currency_toggle")
          ) {
            Text(
              text = if (activeCurrency == Currency.USD) "$ USD" else "₹ INR",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          // Dark/Light Theme toggle
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
              .size(36.dp)
              .testTag("theme_toggle")
          ) {
            Icon(
              imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle theme",
              tint = WarmCaramel,
              modifier = Modifier.size(20.dp)
            )
          }

          // Cart Button with badge
          Box(modifier = Modifier.testTag("cart_top_button")) {
            IconButton(
              onClick = { onNavigate(AppDestination.CART) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = "Cart",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            if (cartCount > 0) {
              Badge(
                containerColor = WarmCaramel,
                contentColor = PureWhite,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .offset(x = 2.dp, y = (-2).dp)
              ) {
                Text(
                  text = cartCount.toString(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Owner/Admin Dashboard Entry
          Surface(
            onClick = onOpenAdmin,
            shape = RoundedCornerShape(16.dp),
            color = WarmCaramel.copy(alpha = 0.12f),
            modifier = Modifier.testTag("admin_icon_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Control Panel",
                tint = WarmCaramel,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Admin",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = WarmCaramel
              )
            }
          }
        }
      }

      HorizontalDivider(color = DividerGold, thickness = 1.dp)
    }
  }
}
