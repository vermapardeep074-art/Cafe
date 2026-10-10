package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val icon: String) {
  HOME("Home", "home"),
  MENU("Menu", "restaurant_menu"),
  CART("Cart", "shopping_bag"),
  CHECKOUT("Checkout", "payment"),
  TRACKING("Track", "local_shipping"),
  ABOUT("About", "storefront"),
  ACCOUNT("Account", "person"),
  ADMIN("Admin", "admin_panel_settings")
}

data class UiNotification(
  val message: String,
  val isError: Boolean = false,
  val id: Long = System.currentTimeMillis()
)

class ArtesanoViewModel(
  private val repository: ArtesanoRepository = ArtesanoRepository()
) : ViewModel() {

  val menuItems: StateFlow<List<MenuItem>> = repository.menuItems
  val cartItems: StateFlow<List<CartItem>> = repository.cartItems
  val orders: StateFlow<List<Order>> = repository.orders
  val settings: StateFlow<StoreSettings> = repository.settings
  val reviews: StateFlow<List<Review>> = repository.reviews
  val favorites: StateFlow<Set<String>> = repository.favorites
  val appliedPromoCode: StateFlow<String?> = repository.appliedPromoCode
  val promoDiscountPercent: StateFlow<Double> = repository.promoDiscountPercent
  val auditLogs: StateFlow<List<String>> = repository.auditLogs

  // Navigation State
  private val _currentScreen = MutableStateFlow(AppDestination.HOME)
  val currentScreen: StateFlow<AppDestination> = _currentScreen.asStateFlow()

  // Menu filtering
  private val _selectedCategory = MutableStateFlow<MenuCategory?>(null)
  val selectedCategory: StateFlow<MenuCategory?> = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedDietaryTag = MutableStateFlow<DietaryTag?>(null)
  val selectedDietaryTag: StateFlow<DietaryTag?> = _selectedDietaryTag.asStateFlow()

  // Detail / Customization Dialog
  private val _detailItem = MutableStateFlow<MenuItem?>(null)
  val detailItem: StateFlow<MenuItem?> = _detailItem.asStateFlow()

  // Active tracked order
  private val _trackingOrder = MutableStateFlow<Order?>(null)
  val trackingOrder: StateFlow<Order?> = _trackingOrder.asStateFlow()

  // Checkout State
  private val _serviceMode = MutableStateFlow(ServiceMode.KERBSIDE_PICKUP)
  val serviceMode: StateFlow<ServiceMode> = _serviceMode.asStateFlow()

  private val _customerName = MutableStateFlow("Sarah Jenkins")
  val customerName: StateFlow<String> = _customerName.asStateFlow()

  private val _customerPhone = MutableStateFlow("+1 212-372-0297")
  val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

  private val _deliveryAddress = MutableStateFlow("90 Chambers St, Apt 4B, New York, NY 10007")
  val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

  private val _specialNotes = MutableStateFlow("")
  val specialNotes: StateFlow<String> = _specialNotes.asStateFlow()

  private val _selectedTipPercent = MutableStateFlow(0.15)
  val selectedTipPercent: StateFlow<Double> = _selectedTipPercent.asStateFlow()

  private val _selectedPaymentMethod = MutableStateFlow("Apple Pay")
  val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

  // Admin authentication
  private val _isAdminAuthenticated = MutableStateFlow(false)
  val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

  // Target item ID when navigating directly to edit a dish
  private val _targetEditItemId = MutableStateFlow<String?>(null)
  val targetEditItemId: StateFlow<String?> = _targetEditItemId.asStateFlow()

  // Notification / Toast
  private val _notification = MutableStateFlow<UiNotification?>(null)
  val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

  init {
    // Select first order for tracking if available
    viewModelScope.launch {
      orders.collect { orderList ->
        if (_trackingOrder.value == null && orderList.isNotEmpty()) {
          _trackingOrder.value = orderList.first()
        }
      }
    }
  }

  fun navigateTo(dest: AppDestination) {
    _currentScreen.value = dest
  }

  fun selectCategory(category: MenuCategory?) {
    _selectedCategory.value = category
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectDietaryTag(tag: DietaryTag?) {
    _selectedDietaryTag.value = tag
  }

  fun openDetail(item: MenuItem) {
    _detailItem.value = item
  }

  fun closeDetail() {
    _detailItem.value = null
  }

  fun trackOrder(order: Order) {
    _trackingOrder.value = order
    _currentScreen.value = AppDestination.TRACKING
  }

  // Cart operations
  fun addToCart(
    item: MenuItem,
    quantity: Int = 1,
    size: String = "Regular",
    sizeAddon: Double = 0.0,
    milk: String = "Whole Milk",
    milkAddon: Double = 0.0,
    extras: List<Pair<String, Double>> = emptyList(),
    notes: String = ""
  ) {
    if (!settings.value.status.isOrderable) {
      showNotification("The café is currently ${settings.value.status.title.lowercase()}. Online orders temporarily paused.", true)
      return
    }
    repository.addToCart(item, quantity, size, sizeAddon, milk, milkAddon, extras, notes)
    showNotification("Added ${item.name} to cart")
    closeDetail()
  }

  fun updateCartQuantity(cartId: String, delta: Int) {
    repository.updateCartQuantity(cartId, delta)
  }

  fun removeFromCart(cartId: String) {
    repository.removeFromCart(cartId)
    showNotification("Item removed from cart")
  }

  fun applyPromoCode(code: String) {
    val success = repository.applyPromoCode(code)
    if (success) {
      showNotification("Promo code applied! Discount activated.")
    } else {
      showNotification("Invalid code. Try ARTESANO10 or TRIBECA", true)
    }
  }

  fun removePromoCode() {
    repository.removePromoCode()
    showNotification("Promo code removed")
  }

  // Checkout operations
  fun updateCheckoutDetails(
    mode: ServiceMode = _serviceMode.value,
    name: String = _customerName.value,
    phone: String = _customerPhone.value,
    address: String = _deliveryAddress.value,
    notes: String = _specialNotes.value,
    tip: Double = _selectedTipPercent.value,
    paymentMethod: String = _selectedPaymentMethod.value
  ) {
    _serviceMode.value = mode
    _customerName.value = name
    _customerPhone.value = phone
    _deliveryAddress.value = address
    _specialNotes.value = notes
    _selectedTipPercent.value = tip
    _selectedPaymentMethod.value = paymentMethod
  }

  fun submitOrder(): Order? {
    if (cartItems.value.isEmpty()) {
      showNotification("Your cart is empty", true)
      return null
    }
    if (!settings.value.status.isOrderable) {
      showNotification("Café is currently ${settings.value.status.title}. Cannot process order.", true)
      return null
    }

    val placed = repository.placeOrder(
      serviceMode = _serviceMode.value,
      customerName = _customerName.value,
      customerPhone = _customerPhone.value,
      deliveryAddress = _deliveryAddress.value,
      specialNotes = _specialNotes.value,
      tipPercent = _selectedTipPercent.value,
      paymentMethod = _selectedPaymentMethod.value
    )
    _trackingOrder.value = placed
    _currentScreen.value = AppDestination.TRACKING
    showNotification("Order placed! Your barista has received it.")
    return placed
  }

  // Admin controls
  fun authenticateAdmin(pin: String): Boolean {
    val valid = pin == "1234" || pin.isEmpty() // Allow quick manager demo toggle
    _isAdminAuthenticated.value = valid
    if (valid) {
      showNotification("Admin authenticated · Welcome Owner")
    } else {
      showNotification("Invalid Manager PIN", true)
    }
    return valid
  }

  fun openAdminForDish(itemId: String) {
    _targetEditItemId.value = itemId
    _isAdminAuthenticated.value = true
    _currentScreen.value = AppDestination.ADMIN
    closeDetail()
  }

  fun clearTargetEditItem() {
    _targetEditItemId.value = null
  }

  fun logoutAdmin() {
    _isAdminAuthenticated.value = false
    showNotification("Admin signed out")
  }

  fun setStoreStatus(status: StoreStatus) {
    repository.updateStoreStatus(status)
    showNotification("Store status updated to: ${status.title}")
  }

  fun updateItemPrice(itemId: String, newPrice: Double) {
    repository.updateItemPrice(itemId, newPrice)
    showNotification("Price updated successfully")
  }

  fun toggleItemStock(itemId: String) {
    repository.toggleItemStock(itemId)
    showNotification("Stock status updated")
  }

  fun updateMenuItem(item: MenuItem) {
    repository.updateMenuItem(item)
    showNotification("Item '${item.name}' updated")
  }

  fun addNewMenuItem(item: MenuItem) {
    repository.addNewMenuItem(item)
    showNotification("Created new item: ${item.name}")
  }

  fun deleteMenuItem(itemId: String) {
    repository.deleteMenuItem(itemId)
    showNotification("Menu item removed")
  }

  fun updateOrderStatus(orderId: String, nextStatus: OrderStatus) {
    repository.updateOrderStatus(orderId, nextStatus)
    showNotification("Order status updated to ${nextStatus.label}")
  }

  fun refundOrder(orderId: String, reason: String) {
    repository.refundOrder(orderId, reason)
    showNotification("Order refunded: $reason")
  }

  fun setContext(context: android.content.Context) {
    repository.setContext(context)
  }

  fun uploadDishPhoto(itemId: String, uri: android.net.Uri): Boolean {
    val path = repository.saveUploadedPhoto(itemId, uri)
    return if (path != null) {
      showNotification("Dish photograph saved & published!")
      true
    } else {
      showNotification("Failed to upload photo", isError = true)
      false
    }
  }

  fun uploadCustomLogo(uri: android.net.Uri): Boolean {
    val path = repository.saveUploadedLogo(uri)
    return if (path != null) {
      showNotification("Brand logo updated & published!")
      true
    } else {
      showNotification("Failed to save logo", isError = true)
      false
    }
  }

  fun uploadCustomBanner(uri: android.net.Uri): Boolean {
    val path = repository.saveUploadedBanner(uri)
    return if (path != null) {
      showNotification("Hero banner photo updated & published!")
      true
    } else {
      showNotification("Failed to save banner", isError = true)
      false
    }
  }

  fun reorderDish(itemId: String, moveUp: Boolean) {
    repository.reorderMenuItem(itemId, moveUp)
  }

  fun updateBannerAndTheme(
    headline: String,
    subheadline: String,
    announcement: String,
    announcementActive: Boolean,
    darkTheme: Boolean
  ) {
    repository.updateBannerAndThemeSettings(headline, subheadline, announcement, announcementActive, darkTheme)
    showNotification("Website banners and styling updated!")
  }

  fun resetMenuDefaults() {
    repository.resetMenuToDefaults()
    showNotification("Menu & settings restored to factory defaults")
  }

  fun updateStoreContact(phone: String, address: String, hours: String, morningHours: String) {
    repository.updateStoreContact(phone, address, hours, morningHours)
    showNotification("Store details updated")
  }

  // Currency & Theme
  fun toggleCurrency() {
    repository.toggleCurrency()
    showNotification("Currency switched to ${settings.value.activeCurrency.code}")
  }

  fun toggleTheme() {
    repository.toggleTheme()
  }

  fun toggleFavorite(itemId: String) {
    repository.toggleFavorite(itemId)
  }

  fun isFavorite(itemId: String): Boolean = repository.isFavorite(itemId)

  fun dismissNotification() {
    _notification.value = null
  }

  private fun showNotification(msg: String, isError: Boolean = false) {
    _notification.value = UiNotification(msg, isError)
  }
}
