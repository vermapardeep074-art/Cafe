package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Currency
import com.example.data.Order
import com.example.data.OrderStatus
import com.example.ui.theme.*

@Composable
fun OrderTrackingScreen(
  order: Order?,
  activeCurrency: Currency,
  onOrderAgainClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  if (order == null) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.LocalShipping,
          contentDescription = null,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No Active Order to Track",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Place an order to see live real-time preparation status.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onOrderAgainClick,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Browse Menu", fontWeight = FontWeight.Bold)
        }
      }
    }
  } else {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(16.dp)
    ) {
      // 1. Order Status Header Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("order_tracking_header")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = order.orderNumber,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = WarmCaramel
                )
                Text(
                  text = order.serviceMode.title,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Surface(
                color = when (order.status) {
                  OrderStatus.COMPLETED -> StatusOpen.copy(alpha = 0.15f)
                  OrderStatus.REFUNDED, OrderStatus.CANCELLED -> StatusClosed.copy(alpha = 0.15f)
                  else -> WarmCaramel.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = order.status.label.uppercase(),
                  color = when (order.status) {
                    OrderStatus.COMPLETED -> StatusOpen
                    OrderStatus.REFUNDED, OrderStatus.CANCELLED -> StatusClosed
                    else -> WarmCaramel
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estimated Time
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = WarmCaramel,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (order.status == OrderStatus.COMPLETED) "Order Completed" else "Estimated: ~${order.estimatedMinutes} minutes",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = order.status.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4-Step Stepper (Received -> Preparing -> Ready -> Completed)
            val steps = listOf(
              OrderStatus.RECEIVED,
              OrderStatus.PREPARING,
              OrderStatus.READY,
              OrderStatus.COMPLETED
            )
            val currentStep = order.status.stepIndex

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              steps.forEachIndexed { index, step ->
                val isPassed = currentStep >= index
                val isCurrent = currentStep == index

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                      .size(32.dp)
                      .background(
                        color = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                      )
                  ) {
                    if (isPassed) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                      )
                    } else {
                      Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (index < steps.size - 1) {
                  Box(
                    modifier = Modifier
                      .weight(0.6f)
                      .height(2.dp)
                      .background(if (currentStep > index) MaterialTheme.colorScheme.primary else DividerGold)
                  )
                }
              }
            }
          }
        }
      }

      // 2. Direct Actions (Call Cafe & Open Map)
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+12123720297")
              }
              context.startActivity(intent)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Call, contentDescription = null, tint = WarmCaramel, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call Cafe", fontWeight = FontWeight.SemiBold)
          }

          OutlinedButton(
            onClick = {
              val mapUri = Uri.parse("geo:0,0?q=90+Chambers+St+Unit+B+New+York+NY+10007")
              val intent = Intent(Intent.ACTION_VIEW, mapUri)
              context.startActivity(intent)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Directions, contentDescription = null, tint = WarmCaramel, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Directions", fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // 3. Receipt & Items
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "ORDER RECEIPT",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { cartItem ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${cartItem.quantity}x ${cartItem.item.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  if (cartItem.selectedSize != "Regular" || cartItem.selectedMilk != "Whole Milk") {
                    Text(
                      text = "${cartItem.selectedSize} · ${cartItem.selectedMilk}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                Text(
                  text = activeCurrency.format(cartItem.totalPriceUsd),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerGold)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(
                text = activeCurrency.format(order.total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmCaramel
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Paid via ${order.paymentMethod} (Ref: ${order.paymentRef})",
              style = MaterialTheme.typography.labelSmall,
              color = LeafGreenDark
            )

            if (order.refundReason != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Refund Note: ${order.refundReason}",
                style = MaterialTheme.typography.labelSmall,
                color = StatusClosed,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
