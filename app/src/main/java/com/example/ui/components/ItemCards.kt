package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Currency
import com.example.data.DietaryTag
import com.example.data.MenuItem
import com.example.ui.theme.*

@Composable
fun LuxuryMenuItemCard(
  item: MenuItem,
  activeCurrency: Currency,
  isFavorite: Boolean,
  onItemClick: () -> Unit,
  onQuickAdd: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onItemClick() }
      .testTag("menu_card_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Image container with gradient and badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Image(
          painter = painterResource(id = item.imageRes),
          contentDescription = item.name,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x66121212)),
                startY = 60f
              )
            )
        )

        // Top badges: Chef Special / Popular / Out of Stock
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!item.inStock) {
              Surface(
                color = StatusClosed,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "OUT OF STOCK",
                  color = PureWhite,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            } else if (item.isChefSpecial) {
              Surface(
                color = WarmCaramel,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "ARTISAN SPECIAL",
                  color = PureWhite,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            } else if (item.isPopular) {
              Surface(
                color = LeafGreenDark,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "POPULAR",
                  color = PureWhite,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          // Favorite button
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
              .size(32.dp)
              .background(PureWhite.copy(alpha = 0.85f), CircleShape)
              .testTag("fav_btn_${item.id}")
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (isFavorite) StatusClosed else EspressoBrown,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Dietary tags badge at bottom-left of image
        if (item.dietaryTags.isNotEmpty()) {
          Row(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            item.dietaryTags.take(3).forEach { tag ->
              Surface(
                color = PureWhite.copy(alpha = 0.92f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = tag.label,
                  color = EspressoBrown,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }

      // Card Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = item.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Price and Add Action
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = activeCurrency.format(item.priceUsd),
              style = MaterialTheme.typography.titleMedium,
              color = WarmCaramel,
              fontWeight = FontWeight.Bold
            )
            // Optional alternate currency preview
            if (activeCurrency == Currency.USD && item.priceInr != null) {
              Text(
                text = "approx. ₹${item.priceInr.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
            }
          }

          FilledTonalButton(
            onClick = onQuickAdd,
            enabled = item.inStock,
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = PureWhite
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("add_button_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Add",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
