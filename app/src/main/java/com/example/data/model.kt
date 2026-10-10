package com.example.data

import com.example.R

enum class MenuCategory(val title: String, val iconName: String) {
  MORNING("Morning Menu", "wb_sunny"),
  COFFEE_TEA("Coffee & Tea", "coffee"),
  COLD_BEVERAGES("Cold Beverages", "local_drink"),
  SIGNATURE_DRINKS("Signature Drinks", "auto_awesome"),
  PASTRIES("Pastries & Bakery", "bakery_dining"),
  SANDWICHES_SAVOURY("Sandwiches & Savoury", "lunch_dining")
}

enum class DietaryTag(val label: String, val shortBadge: String) {
  VEGAN("Vegan", "VG"),
  VEGETARIAN("Vegetarian", "V"),
  GLUTEN_FREE("Gluten-Free", "GF"),
  CONTAINS_NUTS("Contains Nuts", "NUTS"),
  ORGANIC("100% Organic", "ORG")
}

enum class ServiceMode(val title: String, val subtitle: String, val fee: Double) {
  KERBSIDE_PICKUP("Kerbside Pickup", "Brought directly to your vehicle", 0.0),
  DELIVERY("Standard Delivery", "Door-to-door delivery in Tribeca/SoHo", 3.99),
  TAKEAWAY("Takeaway", "Quick counter pickup inside café", 0.0),
  DINE_IN("Dine-in", "Enjoy at a boutique cafe table", 0.0),
  ON_SITE("On-site Services", "Catering & event drop-off setup", 2.50),
  NO_CONTACT_DELIVERY("No-Contact Delivery", "Safe doorstep drop-off with photo", 4.49)
}

enum class StoreStatus(val title: String, val badgeText: String, val isOrderable: Boolean) {
  OPEN("Open now", "OPEN NOW · 7:30 AM – 6:00 PM", true),
  CLOSED("Closed", "CLOSED · OPENS 7:30 AM", false),
  TEMPORARILY_CLOSED("Temporarily closed", "TEMPORARILY CLOSED FOR PRIVATE TASTING", false)
}

enum class Currency(val symbol: String, val code: String, val exchangeRateFromUsd: Double) {
  USD("$", "USD", 1.0),
  INR("₹", "INR", 83.0);

  fun format(amountUsd: Double): String {
    return when (this) {
      USD -> String.format("$%.2f", amountUsd)
      INR -> String.format("₹%.0f", amountUsd * exchangeRateFromUsd)
    }
  }
}

enum class OrderStatus(val label: String, val stepIndex: Int, val description: String) {
  RECEIVED("Received", 0, "Order accepted by Artesano kitchen"),
  PREPARING("Preparing", 1, "Artisan baristas crafting your items"),
  READY("Ready / Out", 2, "Ready for pickup or out for delivery"),
  COMPLETED("Completed", 3, "Handed over · Buon appetito!"),
  REFUNDED("Refunded", -1, "Payment refunded to original method"),
  CANCELLED("Cancelled", -1, "Order cancelled")
}

data class MenuItem(
  val id: String,
  val name: String,
  val description: String,
  val priceUsd: Double,
  val priceInr: Double? = null,
  val category: MenuCategory,
  val isMorningMenu: Boolean = false,
  val inStock: Boolean = true,
  val imageRes: Int = R.drawable.ic_artesano_logo,
  val dietaryTags: List<DietaryTag> = emptyList(),
  val isPopular: Boolean = false,
  val isChefSpecial: Boolean = false
)

data class CartItem(
  val cartId: String,
  val item: MenuItem,
  val quantity: Int = 1,
  val selectedSize: String = "Regular",
  val sizePriceAddon: Double = 0.0,
  val selectedMilk: String = "Whole Milk",
  val milkPriceAddon: Double = 0.0,
  val extras: List<Pair<String, Double>> = emptyList(),
  val specialInstructions: String = ""
) {
  val unitPriceUsd: Double
    get() = item.priceUsd + sizePriceAddon + milkPriceAddon + extras.sumOf { it.second }

  val totalPriceUsd: Double
    get() = unitPriceUsd * quantity
}

data class Order(
  val id: String,
  val orderNumber: String,
  val timestamp: Long,
  val items: List<CartItem>,
  val status: OrderStatus,
  val serviceMode: ServiceMode,
  val customerName: String,
  val customerPhone: String,
  val deliveryAddress: String,
  val specialNotes: String,
  val subtotal: Double,
  val tax: Double,
  val tip: Double,
  val deliveryFee: Double,
  val discount: Double,
  val total: Double,
  val currency: Currency = Currency.USD,
  val paymentMethod: String,
  val paymentRef: String,
  val estimatedMinutes: Int = 15,
  val refundReason: String? = null
)

data class Review(
  val id: String,
  val author: String,
  val rating: Float,
  val quote: String,
  val date: String,
  val verified: Boolean = true
)

data class StoreSettings(
  val status: StoreStatus = StoreStatus.OPEN,
  val phone: String = "+1 212-372-0297",
  val address: String = "90 Chambers St Unit B, New York, NY 10007, United States",
  val morningHours: String = "8:00 AM – 11:00 AM",
  val storeHours: String = "Mon – Sun: 7:30 AM – 6:00 PM",
  val activeCurrency: Currency = Currency.USD,
  val darkTheme: Boolean = false
)
