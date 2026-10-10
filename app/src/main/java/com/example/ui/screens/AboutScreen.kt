package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StoreSettings
import com.example.ui.theme.*

@Composable
fun AboutScreen(
  storeSettings: StoreSettings,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showLegalDialog by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Heritage Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_artesano),
            contentDescription = "Artesano Interior",
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
          )

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            color = WarmCaramel.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "RISTORANTE ITALIANO · TRIBECA",
              color = WarmCaramel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Artesano Cafe",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Text(
            text = "\"Fresh · Bright · Plant-Filled · Homemade · Daily\"",
            style = MaterialTheme.typography.titleSmall,
            color = SoftGoldMuted,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Welcome to Artesano Cafe, an artisan neighbourhood café and bakery established at 90 Chambers Street in Tribeca, New York. Bearing the proud storefront heritage of 'Ristorante Italiano', Artesano represents a loving marriage of classical Italian coffee roasting and authentic handcrafted baking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }
    }

    // 2. Location & Contact
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "VISIT US IN NEW YORK",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WarmCaramel)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "90 Chambers St Unit B", fontWeight = FontWeight.Bold)
              Text(text = "New York, NY 10007, United States", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Call, contentDescription = null, tint = WarmCaramel)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "+1 212-372-0297", fontWeight = FontWeight.Bold)
              Text(text = "Direct Line & Barista Desk", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = WarmCaramel)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Morning Menu: 8:00 AM – 11:00 AM Daily", fontWeight = FontWeight.Bold)
              Text(text = "Store Hours: 7:30 AM – 6:00 PM Daily (EST)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                  data = Uri.parse("tel:${storeSettings.phone}")
                }
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Tap to Call")
            }

            OutlinedButton(
              onClick = {
                val mapUri = Uri.parse("geo:0,0?q=90+Chambers+St+Unit+B+New+York+NY+10007")
                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                context.startActivity(intent)
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Directions")
            }
          }
        }
      }
    }

    // 2.5 Google Maps Verified Features & Offerings (from listing)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "CAFE HIGHLIGHTS & AMENITIES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          val highlights = listOf(
            "Service Options" to "Kerbside pickup, Delivery, Takeaway, No-contact delivery, Dine-in, On-site services",
            "Highlights" to "Great coffee, Great tea selection",
            "Popular For" to "Breakfast, Lunch, Solo dining",
            "Accessibility" to "Wheelchair-accessible entrance",
            "Atmosphere" to "Casual, Cozy, Plant-filled atelier",
            "Payments" to "Credit cards, Debit cards, Apple Pay, Google Pay"
          )

          highlights.forEach { (category, details) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = LeafGreenDark,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = category,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = details,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 3. Legal & Compliance Badges
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "TRUST, PRIVACY & COMPLIANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          val legalItems = listOf(
            "Privacy Policy" to "We store only minimal guest data necessary for kitchen fulfillment. Card numbers are tokenized via PCI-DSS compliant providers.",
            "Terms of Service" to "Online orders are governed by New York State commerce regulations. Pricing and daily menu subject to kitchen availability.",
            "Refund & Cancellation" to "Orders can be modified or cancelled within 3 minutes of receipt prior to barista preparation.",
            "ADA & Accessibility Statement" to "Artesano Cafe is dedicated to digital accessibility in accordance with WCAG 2.1 AA standards."
          )

          legalItems.forEach { (title, content) ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { showLegalDialog = title },
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarmCaramel)
              }
            }
          }
        }
      }
    }
  }

  // Legal Modal Dialog
  if (showLegalDialog != null) {
    AlertDialog(
      onDismissRequest = { showLegalDialog = null },
      title = { Text(showLegalDialog ?: "") },
      text = {
        Text(
          when (showLegalDialog) {
            "Privacy Policy" -> "Artesano Cafe operates with zero-trust payment security. All payments are tokenized via Stripe / Apple Pay with 256-bit TLS encryption. We do not store raw credit card numbers. Your contact information is exclusively used for order status notifications."
            "Terms of Service" -> "By ordering through the Artesano Cafe app, you agree to our terms. Morning Menu items are served strictly from 8:00 AM – 11:00 AM daily. Pickups are held in our temperature-controlled atelier for up to 30 minutes."
            "Refund & Cancellation" -> "In case of any order discrepancy or unsatisfactory item, please notify our barista desk via phone at +1 212-372-0297 or visit 90 Chambers St. Prompt refunds are processed directly by our manager dashboard."
            else -> "We strive to ensure full accessibility for all individuals with disabilities. Our app adheres to WCAG AA guidelines with high-contrast luxury styling, scalable typography, and full TalkBack screen-reader semantics."
          }
        )
      },
      confirmButton = {
        TextButton(onClick = { showLegalDialog = null }) {
          Text("Close", color = WarmCaramel)
        }
      }
    )
  }
}
