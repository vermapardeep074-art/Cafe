package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Currency
import com.example.data.MenuCategory
import com.example.data.MenuItem
import com.example.ui.theme.*

@Composable
fun ItemDetailDialog(
  item: MenuItem,
  activeCurrency: Currency,
  onDismiss: () -> Unit,
  onAddToCart: (
    quantity: Int,
    size: String,
    sizeAddon: Double,
    milk: String,
    milkAddon: Double,
    extras: List<Pair<String, Double>>,
    notes: String
  ) -> Unit
) {
  var quantity by remember { mutableIntStateOf(1) }
  var selectedSize by remember { mutableStateOf("Regular") }
  val sizeAddon = if (selectedSize == "Large") 0.75 else 0.0

  val isBeverage = item.category in listOf(
    MenuCategory.COFFEE_TEA,
    MenuCategory.MORNING,
    MenuCategory.SIGNATURE_DRINKS
  ) && !item.name.contains("Sandwich", ignoreCase = true)
      && !item.name.contains("Brownie", ignoreCase = true)
      && !item.name.contains("Croissant", ignoreCase = true)
      && !item.name.contains("Tots", ignoreCase = true)
      && !item.name.contains("Jam", ignoreCase = true)
      && !item.name.contains("Cheese", ignoreCase = true)

  var selectedMilk by remember { mutableStateOf("Whole Milk") }
  val milkAddon = when (selectedMilk) {
    "Oat Milk" -> 0.80
    "Almond Milk" -> 0.80
    else -> 0.0
  }

  val availableExtras = remember(item) {
    if (isBeverage) {
      listOf(
        "Extra Espresso Shot" to 1.00,
        "Vanilla Syrup" to 0.75,
        "Caramel Drizzle" to 0.75
      )
    } else {
      listOf(
        "Side Fig Jam" to 2.00,
        "Extra Avocado" to 2.50,
        "Chive Cream Spread" to 1.50
      )
    }
  }

  val selectedExtras = remember { mutableStateMapOf<String, Double>() }
  var specialInstructions by remember { mutableStateOf("") }

  val unitPrice = item.priceUsd + sizeAddon + milkAddon + selectedExtras.values.sum()
  val totalPrice = unitPrice * quantity

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.88f)
        .testTag("item_detail_dialog")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
          // Image Header
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
          ) {
            Image(
              painter = painterResource(id = item.imageRes),
              contentDescription = item.name,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(PureWhite.copy(alpha = 0.85f), CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = EspressoBrown
              )
            }
          }

          // Item Info
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = item.name,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = activeCurrency.format(item.priceUsd),
              style = MaterialTheme.typography.titleLarge,
              color = WarmCaramel,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = item.description,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 22.sp
            )

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 14.dp),
              color = DividerGold
            )

            // Size Selector
            Text(
              text = "SELECT SIZE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              listOf("Regular" to 0.0, "Large" to 0.75).forEach { (size, cost) ->
                val isSelected = selectedSize == size
                OutlinedButton(
                  onClick = { selectedSize = size },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) WarmCaramel.copy(alpha = 0.15f) else Color.Transparent
                  ),
                  border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                      if (isSelected) WarmCaramel else DividerGold
                    )
                  )
                ) {
                  Text(
                    text = if (cost > 0) "$size (+${activeCurrency.format(cost)})" else size,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            // Milk Choice if beverage
            if (isBeverage) {
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "MILK PREFERENCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              listOf(
                "Whole Milk" to 0.0,
                "Oat Milk" to 0.80,
                "Almond Milk" to 0.80,
                "Skim Milk" to 0.0
              ).forEach { (milk, addon) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = selectedMilk == milk,
                    onClick = { selectedMilk = milk },
                    colors = RadioButtonDefaults.colors(selectedColor = WarmCaramel)
                  )
                  Text(
                    text = if (addon > 0) "$milk (+${activeCurrency.format(addon)})" else milk,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }

            // Extras
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "ENHANCE YOUR ORDER",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            availableExtras.forEach { (extraName, extraCost) ->
              val isChecked = selectedExtras.containsKey(extraName)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Checkbox(
                  checked = isChecked,
                  onCheckedChange = { checked ->
                    if (checked) {
                      selectedExtras[extraName] = extraCost
                    } else {
                      selectedExtras.remove(extraName)
                    }
                  },
                  colors = CheckboxDefaults.colors(checkedColor = WarmCaramel)
                )
                Text(
                  text = "$extraName (+${activeCurrency.format(extraCost)})",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            // Special notes
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "SPECIAL INSTRUCTIONS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = specialInstructions,
              onValueChange = { specialInstructions = it },
              placeholder = { Text("E.g., extra hot, light ice, dressing on the side...") },
              modifier = Modifier.fillMaxWidth(),
              maxLines = 3,
              shape = RoundedCornerShape(10.dp)
            )
          }
        }

        // Bottom Action Bar: Quantity & Add Button
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp,
          shadowElevation = 6.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Quantity Picker
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .border(1.dp, DividerGold, RoundedCornerShape(12.dp))
                .padding(4.dp)
            ) {
              IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = EspressoBrown)
              }
              Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
              )
              IconButton(
                onClick = { quantity++ },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = EspressoBrown)
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Add Button
            Button(
              onClick = {
                val extrasList = selectedExtras.map { it.key to it.value }
                onAddToCart(
                  quantity,
                  selectedSize,
                  sizeAddon,
                  selectedMilk,
                  milkAddon,
                  extrasList,
                  specialInstructions
                )
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = PureWhite
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("dialog_add_to_cart_btn")
            ) {
              Text(
                text = "Add · ${activeCurrency.format(totalPrice)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
