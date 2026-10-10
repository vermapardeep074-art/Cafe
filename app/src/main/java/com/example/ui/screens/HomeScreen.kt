package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.components.LuxuryMenuItemCard
import com.example.ui.components.MorningMenuHighlightBanner
import com.example.ui.theme.*

@Composable
fun HomeScreen(
  menuItems: List<MenuItem>,
  reviews: List<Review>,
  storeSettings: StoreSettings,
  activeCurrency: Currency,
  favorites: Set<String>,
  onOrderNowClick: () -> Unit,
  onItemClick: (MenuItem) -> Unit,
  onQuickAdd: (MenuItem) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onViewMorningMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val featuredItems: List<MenuItem> = remember(menuItems) {
    menuItems.filter { it.imageRes != R.drawable.ic_artesano_logo }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // 1. HERO SECTION
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_hero_artesano),
          contentDescription = "Artesano Cafe Tribeca",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Luxury Gradient Overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0x992A1E17),
                  Color(0xCC121212)
                )
              )
            )
        )

        // Hero Content
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
          verticalArrangement = Arrangement.Bottom,
          horizontalAlignment = Alignment.Start
        ) {
          Surface(
            color = SoftGold.copy(alpha = 0.3f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            Text(
              text = "HISTORIC TRIBECA · RISTORANTE ITALIANO",
              color = SoftGoldLight,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Text(
            text = "ARTESANO CAFE",
            style = MaterialTheme.typography.displayLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
          )

          Text(
            text = "Fresh · Bright · Plant-Filled · Homemade · Daily",
            style = MaterialTheme.typography.bodyLarge,
            color = SoftGoldLight,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onOrderNowClick,
              colors = ButtonDefaults.buttonColors(
                containerColor = WarmCaramel,
                contentColor = PureWhite
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("home_order_now_button")
            ) {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Order Now",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                  data = Uri.parse("tel:${storeSettings.phone}")
                }
                context.startActivity(intent)
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PureWhite
              ),
              border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(SoftGold, PureWhite))
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("home_call_button")
            ) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call Cafe",
                tint = SoftGold,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Call",
                color = PureWhite,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }

    // 2. QUICK STORE STRIP & STATUS
    item {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(
                    if (storeSettings.status == StoreStatus.OPEN) StatusOpen else StatusClosed,
                    CircleShape
                  )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = storeSettings.status.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Text(
              text = "Morning Menu 8–11 AM",
              style = MaterialTheme.typography.labelSmall,
              color = WarmCaramel,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.LocationOn,
              contentDescription = null,
              tint = WarmCaramel,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = storeSettings.address,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }
        }
      }
    }

    // 3. MORNING MENU HIGHLIGHT BANNER
    item {
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        MorningMenuHighlightBanner(onViewMorningMenu = onViewMorningMenu)
      }
    }

    // 4. SERVICE MODES AVAILABLE (matching listing)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "HOW WOULD YOU LIKE YOUR ORDER?",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 1.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          val modes = listOf(
            "Kerbside pickup",
            "Takeaway",
            "Delivery",
            "Dine-in",
            "On-site services",
            "No-contact delivery"
          )
          items(modes) { mode ->
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(20.dp),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DividerGold, SoftGold))),
              modifier = Modifier.clickable { onOrderNowClick() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = LeafGreenDark,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = mode,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    // 5. CHEF'S ARTISAN SELECTIONS
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Artisan Specialties",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Handcrafted with European & South American inspiration",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          TextButton(onClick = onOrderNowClick) {
            Text(
              text = "View All",
              color = WarmCaramel,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelLarge
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
          items(featuredItems) { item ->
            Box(modifier = Modifier.width(220.dp)) {
              LuxuryMenuItemCard(
                item = item,
                activeCurrency = activeCurrency,
                isFavorite = favorites.contains(item.id),
                onItemClick = { onItemClick(item) },
                onQuickAdd = { onQuickAdd(item) },
                onToggleFavorite = { onToggleFavorite(item.id) }
              )
            }
          }
        }
      }
    }

    // 6. BRAND STORY & ITALIAN HERITAGE
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Surface(
            color = LeafGreen.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "OUR PHILOSOPHY",
              color = LeafGreenDark,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Warm, Handcrafted & Italian-Influenced",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Tucked into Chambers Street in Tribeca, Artesano Cafe honors traditional Italian espresso craft alongside warm Latin-American bakery heritage. From morning sourdough breakfast sandwiches to Peruvian dulce de leche alfajores and golden Pan con Chicharrón, every creation is baked and prepared in our plant-filled atelier daily.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }
    }

    // 7. VERIFIED REVIEWS & TESTIMONIALS
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
      ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = "Tribeca Loves Artesano",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Curated reviews from our daily neighbourhood guests",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
          items(reviews) { review ->
            Card(
              modifier = Modifier.width(280.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row {
                    repeat(5) {
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SoftGold,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                  Text(
                    text = review.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "\"${review.quote}\"",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = LeafGreenDark,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = review.author,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
