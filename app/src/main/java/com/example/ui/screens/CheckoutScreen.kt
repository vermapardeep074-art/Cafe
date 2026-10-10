package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.CartItem
import com.example.data.Currency
import com.example.data.ServiceMode
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
  cartItems: List<CartItem>,
  serviceMode: ServiceMode,
  activeCurrency: Currency,
  appliedPromoCode: String?,
  promoDiscountPercent: Double,
  selectedTipPercent: Double,
  customerName: String,
  customerPhone: String,
  deliveryAddress: String,
  specialNotes: String,
  selectedPaymentMethod: String,
  onUpdateDetails: (
    mode: ServiceMode,
    name: String,
    phone: String,
    address: String,
    notes: String,
    tip: Double,
    paymentMethod: String
  ) -> Unit,
  onBackToCart: () -> Unit,
  onOrderSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  var nameInput by remember { mutableStateOf(customerName) }
  var phoneInput by remember { mutableStateOf(customerPhone) }
  var addressInput by remember { mutableStateOf(deliveryAddress) }
  var notesInput by remember { mutableStateOf(specialNotes) }
  var paymentMethodInput by remember { mutableStateOf(selectedPaymentMethod) }

  // Card fields for tokenized element
  var cardNumber by remember { mutableStateOf("•••• •••• •••• 4242") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvc by remember { mutableStateOf("888") }
  var isProcessingPayment by remember { mutableStateOf(false) }
  var paymentError by remember { mutableStateOf<String?>(null) }
  var showPolicyDialog by remember { mutableStateOf(false) }

  val subtotal = cartItems.sumOf { it.totalPriceUsd }
  val discount = subtotal * promoDiscountPercent
  val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
  val tax = discountedSubtotal * 0.08875
  val tip = discountedSubtotal * selectedTipPercent
  val deliveryFee = serviceMode.fee
  val grandTotal = discountedSubtotal + tax + tip + deliveryFee

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
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackToCart,
          modifier = Modifier.testTag("checkout_back_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to cart",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Secure Checkout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "256-bit TLS Encrypted · Idempotent Processing",
            style = MaterialTheme.typography.labelSmall,
            color = LeafGreenDark
          )
        }
      }
    }

    // Scrollable Form
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Customer & Delivery/Pickup Details
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = WarmCaramel)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "1. GUEST & FULFILLMENT DETAILS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_name_input"),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Mobile Phone (For Order Notifications)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_phone_input"),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          val addressLabel = when (serviceMode) {
            ServiceMode.KERBSIDE_PICKUP -> "Vehicle Description & Parking Spot"
            ServiceMode.DELIVERY, ServiceMode.NO_CONTACT_DELIVERY -> "Delivery Address (Tribeca / Manhattan)"
            ServiceMode.DINE_IN -> "Table Number or Preferred Seating"
            ServiceMode.ON_SITE -> "Event Location / Venue Address"
            ServiceMode.TAKEAWAY -> "Pickup Name Confirmation"
          }

          OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            label = { Text(addressLabel) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_address_input"),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            label = { Text("Kitchen Notes / Dietary Requests (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      // 2. Payment Method Selector (Stripe / Apple Pay / Google Pay / Card Tokenization)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = WarmCaramel)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "2. PAYMENT METHOD",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          val methods = listOf("Apple Pay", "Google Pay", "Credit / Debit Card")
          methods.forEach { method ->
            val isSelected = paymentMethodInput == method
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) WarmCaramel.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { paymentMethodInput = method }
                .testTag("payment_method_${method.replace(" ", "_").lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { paymentMethodInput = method },
                  colors = RadioButtonDefaults.colors(selectedColor = WarmCaramel)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = method,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = when (method) {
                      "Apple Pay" -> "Biometric one-touch encrypted checkout"
                      "Google Pay" -> "Fast, secure payment with Google account"
                      else -> "Tokenized card via Stripe PCI-DSS Level 1"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Icon(
                  imageVector = when (method) {
                    "Apple Pay" -> Icons.Default.Fingerprint
                    "Google Pay" -> Icons.Default.AccountBalanceWallet
                    else -> Icons.Default.CreditCard
                  },
                  contentDescription = null,
                  tint = if (isSelected) WarmCaramel else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Card Fields if Card chosen
          AnimatedVisibility(visible = paymentMethodInput == "Credit / Debit Card") {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
            ) {
              OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = cardExpiry,
                  onValueChange = { cardExpiry = it },
                  label = { Text("MM/YY") },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                  value = cardCvc,
                  onValueChange = { cardCvc = it },
                  label = { Text("CVC") },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Trust Signals & Compliance notice
          Surface(
            color = LeafGreen.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = LeafGreenDark,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "PCI-DSS Level 1 & TLS Protected",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = LeafGreenDark
                )
                Text(
                  text = "Card data is tokenized directly with provider; no raw card data ever touches our servers.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          TextButton(
            onClick = { showPolicyDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
          ) {
            Text(
              text = "Read Refund & Cancellation Policy",
              style = MaterialTheme.typography.labelSmall,
              color = WarmCaramel
            )
          }
        }
      }

      // 3. Error Banner if any
      if (paymentError != null) {
        Surface(
          color = StatusClosed.copy(alpha = 0.15f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Error, contentDescription = null, tint = StatusClosed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = paymentError ?: "",
              color = StatusClosed,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    }

    // Bottom Pay Button
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      shadowElevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Total Due",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = activeCurrency.format(grandTotal),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = WarmCaramel
            )
          }

          Button(
            onClick = {
              if (nameInput.isBlank()) {
                paymentError = "Please enter your name."
                return@Button
              }
              if (phoneInput.isBlank()) {
                paymentError = "Please enter your phone number."
                return@Button
              }
              paymentError = null
              isProcessingPayment = true
              onUpdateDetails(
                serviceMode,
                nameInput,
                phoneInput,
                addressInput,
                notesInput,
                selectedTipPercent,
                paymentMethodInput
              )
              coroutineScope.launch {
                // Simulate server-side cryptographic tokenization and webhook validation
                delay(1200)
                isProcessingPayment = false
                onOrderSuccess()
              }
            },
            enabled = !isProcessingPayment,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = PureWhite
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .height(52.dp)
              .testTag("pay_and_place_order_btn")
          ) {
            if (isProcessingPayment) {
              CircularProgressIndicator(
                color = PureWhite,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Securing Payment...", fontWeight = FontWeight.Bold)
            } else {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pay & Place Order", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Refund Policy Dialog
  if (showPolicyDialog) {
    AlertDialog(
      onDismissRequest = { showPolicyDialog = false },
      title = { Text("Refund & Cancellation Policy") },
      text = {
        Text(
          "At Artesano Cafe, each pastry, beverage, and sourdough sandwich is freshly prepared to order. Cancellations can be made within 3 minutes of ordering before preparation commences. If an item does not meet your highest satisfaction, the manager will issue a prompt full or partial refund to your original payment method via our admin portal."
        )
      },
      confirmButton = {
        TextButton(onClick = { showPolicyDialog = false }) {
          Text("Understood", color = WarmCaramel)
        }
      }
    )
  }
}
