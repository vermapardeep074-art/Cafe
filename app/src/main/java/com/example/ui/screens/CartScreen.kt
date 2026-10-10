package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun CartScreen(
  cartItems: List<CartItem>,
  upsellItems: List<MenuItem>,
  activeCurrency: Currency,
  serviceMode: ServiceMode,
  appliedPromoCode: String?,
  promoDiscountPercent: Double,
  selectedTipPercent: Double,
  onUpdateQuantity: (String, Int) -> Unit,
  onRemoveItem: (String) -> Unit,
  onSelectServiceMode: (ServiceMode) -> Unit,
  onApplyPromo: (String) -> Unit,
  onRemovePromo: () -> Unit,
  onSelectTip: (Double) -> Unit,
  onQuickAddUpsell: (MenuItem) -> Unit,
  onProceedToCheckout: () -> Unit,
  onBrowseMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  var promoInput by remember { mutableStateOf("") }

  val subtotal = cartItems.sumOf { it.totalPriceUsd }
  val discount = subtotal * promoDiscountPercent
  val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
  val tax = discountedSubtotal * 0.08875 // NYC 8.875% tax
  val tip = discountedSubtotal * selectedTipPercent
  val deliveryFee = serviceMode.fee
  val total = discountedSubtotal + tax + tip + deliveryFee

  if (cartItems.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
          color = WarmCaramel.copy(alpha = 0.15f),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier.size(80.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.ShoppingBag,
              contentDescription = null,
              tint = WarmCaramel,
              modifier = Modifier.size(40.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Your Cart is Empty",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Indulge in freshly baked pastries, sourdough sandwiches, or our signature Italian coffee roast.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onBrowseMenu,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = PureWhite
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("empty_cart_browse_btn")
        ) {
          Text("Explore the Menu", fontWeight = FontWeight.Bold)
        }
      }
    }
  } else {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
    ) {
      // 1. Service Mode Chooser (KFC/McDonald's upfront fulfillment style)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "FULFILLMENT METHOD",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(ServiceMode.values()) { mode ->
                val isSelected = serviceMode == mode
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectServiceMode(mode) },
                  label = {
                    Column {
                      Text(text = mode.title, fontWeight = FontWeight.Bold)
                      if (mode.fee > 0) {
                        Text(
                          text = "+${activeCurrency.format(mode.fee)} fee",
                          style = MaterialTheme.typography.labelSmall,
                          fontSize = 9.sp
                        )
                      }
                    }
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = PureWhite
                  ),
                  modifier = Modifier.testTag("service_mode_${mode.name.lowercase()}")
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = serviceMode.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 2. Cart Items
      items(cartItems, key = { it.cartId }) { cartItem ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("cart_item_${cartItem.cartId}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(id = cartItem.item.imageRes),
              contentDescription = cartItem.item.name,
              modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(10.dp)),
              contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = cartItem.item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              if (cartItem.selectedSize != "Regular" || cartItem.selectedMilk != "Whole Milk" || cartItem.extras.isNotEmpty()) {
                val customizations = buildList {
                  if (cartItem.selectedSize != "Regular") add(cartItem.selectedSize)
                  if (cartItem.selectedMilk != "Whole Milk") add(cartItem.selectedMilk)
                  cartItem.extras.forEach { add(it.first) }
                }
                Text(
                  text = customizations.joinToString(" · "),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (cartItem.specialInstructions.isNotBlank()) {
                Text(
                  text = "\"${cartItem.specialInstructions}\"",
                  style = MaterialTheme.typography.labelSmall,
                  color = WarmCaramel,
                  maxLines = 1
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = activeCurrency.format(cartItem.totalPriceUsd),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = WarmCaramel
              )
            }

            // Stepper
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .border(1.dp, DividerGold, RoundedCornerShape(10.dp))
                .padding(2.dp)
            ) {
              IconButton(
                onClick = { onUpdateQuantity(cartItem.cartId, -1) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                  contentDescription = "Decrease",
                  tint = if (cartItem.quantity == 1) StatusClosed else EspressoBrown,
                  modifier = Modifier.size(16.dp)
                )
              }

              Text(
                text = cartItem.quantity.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
              )

              IconButton(
                onClick = { onUpdateQuantity(cartItem.cartId, 1) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = EspressoBrown, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 3. KFC/McDonald's-Style Smart Upsells / Pairing Carousel
      if (upsellItems.isNotEmpty()) {
        item {
          Column(modifier = Modifier.padding(vertical = 14.dp)) {
            Text(
              text = "PAIR WITH YOUR SELECTION",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(upsellItems.take(5)) { upsell ->
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier
                    .width(180.dp)
                    .clickable { onQuickAddUpsell(upsell) }
                ) {
                  Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Image(
                      painter = painterResource(id = upsell.imageRes),
                      contentDescription = null,
                      modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp)),
                      contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = upsell.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                      )
                      Text(
                        text = "+${activeCurrency.format(upsell.priceUsd)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmCaramel,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Add",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 4. Promo Code Strip
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            if (appliedPromoCode != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusOpen,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Code '$appliedPromoCode' applied (${(promoDiscountPercent * 100).toInt()}% off)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StatusOpen
                  )
                }
                TextButton(onClick = onRemovePromo) {
                  Text("Remove", color = StatusClosed)
                }
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = promoInput,
                  onValueChange = { promoInput = it },
                  placeholder = { Text("Enter promo code (e.g. ARTESANO10)") },
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("promo_code_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    onApplyPromo(promoInput)
                    promoInput = ""
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = WarmCaramel),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("apply_promo_btn")
                ) {
                  Text("Apply", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // 5. Gratuity / Tip Selection
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "SUPPORT YOUR ARTESANO BARISTAS",
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
              listOf(0.10 to "10%", 0.15 to "15%", 0.20 to "20%", 0.0 to "None").forEach { (percent, label) ->
                val isSelected = selectedTipPercent == percent
                FilledTonalButton(
                  onClick = { onSelectTip(percent) },
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface
                  ),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }
        }
      }

      // 6. Summary Breakdown & Checkout Button
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 20.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "ORDER SUMMARY",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow("Subtotal", activeCurrency.format(subtotal))
            if (discount > 0) {
              SummaryRow("Promo Discount", "-${activeCurrency.format(discount)}", color = StatusOpen)
            }
            if (deliveryFee > 0) {
              SummaryRow("Delivery & Service Fee", activeCurrency.format(deliveryFee))
            }
            SummaryRow("NYC Sales Tax (8.875%)", activeCurrency.format(tax))
            if (tip > 0) {
              SummaryRow("Barista Gratuity", activeCurrency.format(tip))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerGold)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = activeCurrency.format(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = WarmCaramel
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = onProceedToCheckout,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = PureWhite
              ),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("proceed_to_checkout_btn")
            ) {
              Text(
                text = "Proceed to Secure Checkout",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color = Color.Unspecified) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
    )
  }
}
