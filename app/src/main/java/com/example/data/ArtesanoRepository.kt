package com.example.data

import android.content.Context
import android.net.Uri
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ArtesanoRepository(private var appContext: Context? = null) {

  private val _menuItems = MutableStateFlow<List<MenuItem>>(createInitialMenuItems())
  val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

  private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
  val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

  private val _orders = MutableStateFlow<List<Order>>(createSampleOrders())
  val orders: StateFlow<List<Order>> = _orders.asStateFlow()

  private val _settings = MutableStateFlow(StoreSettings())
  val settings: StateFlow<StoreSettings> = _settings.asStateFlow()

  private val _reviews = MutableStateFlow<List<Review>>(createInitialReviews())
  val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

  private val _favorites = MutableStateFlow<Set<String>>(setOf("sw_6", "p_1", "c_4", "p_6"))
  val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

  private val _appliedPromoCode = MutableStateFlow<String?>(null)
  val appliedPromoCode: StateFlow<String?> = _appliedPromoCode.asStateFlow()

  private val _promoDiscountPercent = MutableStateFlow(0.0)
  val promoDiscountPercent: StateFlow<Double> = _promoDiscountPercent.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<String>>(
    listOf(
      "Menu and photos synchronized with authentic Tribeca printed menu",
      "Peruvian Alfajores photo updated ($4.95)",
      "Cortado photo in black ceramic cup updated ($4.00)",
      "Classic Omelette & Caprese Sandwich added with photos",
      "Chocolate Cookie photo updated ($4.50)"
    )
  )
  val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

  init {
    loadPersistentDataIfAvailable()
  }

  fun setContext(context: Context) {
    if (this.appContext == null) {
      this.appContext = context.applicationContext
      loadPersistentDataIfAvailable()
    }
  }

  private val menuFile: File?
    get() = appContext?.let { File(it.filesDir, "artesano_menu.json") }

  private val settingsFile: File?
    get() = appContext?.let { File(it.filesDir, "artesano_settings.json") }

  private val photosDir: File?
    get() = appContext?.let {
      File(it.filesDir, "uploaded_photos").apply { if (!exists()) mkdirs() }
    }

  private fun loadPersistentDataIfAvailable() {
    val mFile = menuFile
    if (mFile != null && mFile.exists()) {
      runCatching {
        val jsonStr = mFile.readText()
        val jsonArray = JSONArray(jsonStr)
        val loadedList = mutableListOf<MenuItem>()
        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.getJSONObject(i)
          loadedList.add(menuItemFromJson(obj))
        }
        if (loadedList.isNotEmpty()) {
          _menuItems.value = loadedList
          addAudit("Loaded ${loadedList.size} menu items from persistent storage")
        }
      }.onFailure {
        it.printStackTrace()
      }
    }

    val sFile = settingsFile
    if (sFile != null && sFile.exists()) {
      runCatching {
        val jsonStr = sFile.readText()
        val obj = JSONObject(jsonStr)
        _settings.value = storeSettingsFromJson(obj)
        addAudit("Loaded store branding & settings from persistent storage")
      }.onFailure {
        it.printStackTrace()
      }
    }
  }

  private fun saveMenuToFile() {
    val mFile = menuFile ?: return
    runCatching {
      val jsonArray = JSONArray()
      _menuItems.value.forEach { item ->
        jsonArray.put(menuItemToJson(item))
      }
      mFile.writeText(jsonArray.toString(2))
    }.onFailure {
      it.printStackTrace()
    }
  }

  private fun saveSettingsToFile() {
    val sFile = settingsFile ?: return
    runCatching {
      val json = storeSettingsToJson(_settings.value)
      sFile.writeText(json.toString(2))
    }.onFailure {
      it.printStackTrace()
    }
  }

  // --- Cart operations ---
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
    val existingIndex = _cartItems.value.indexOfFirst {
      it.item.id == item.id &&
          it.selectedSize == size &&
          it.selectedMilk == milk &&
          it.extras == extras &&
          it.specialInstructions == notes
    }

    if (existingIndex >= 0) {
      val existing = _cartItems.value[existingIndex]
      val updatedList = _cartItems.value.toMutableList()
      updatedList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
      _cartItems.value = updatedList
    } else {
      val newCartItem = CartItem(
        cartId = UUID.randomUUID().toString(),
        item = item,
        quantity = quantity,
        selectedSize = size,
        sizePriceAddon = sizeAddon,
        selectedMilk = milk,
        milkPriceAddon = milkAddon,
        extras = extras,
        specialInstructions = notes
      )
      _cartItems.value = _cartItems.value + newCartItem
    }
  }

  fun updateCartQuantity(cartId: String, delta: Int) {
    val current = _cartItems.value
    val target = current.find { it.cartId == cartId } ?: return
    val newQty = target.quantity + delta
    if (newQty <= 0) {
      _cartItems.value = current.filter { it.cartId != cartId }
    } else {
      _cartItems.value = current.map {
        if (it.cartId == cartId) it.copy(quantity = newQty) else it
      }
    }
  }

  fun removeFromCart(cartId: String) {
    _cartItems.value = _cartItems.value.filter { it.cartId != cartId }
  }

  fun clearCart() {
    _cartItems.value = emptyList()
    _appliedPromoCode.value = null
    _promoDiscountPercent.value = 0.0
  }

  // --- Promo code ---
  fun applyPromoCode(code: String): Boolean {
    val trimmed = code.trim().uppercase()
    return when (trimmed) {
      "ARTESANO10" -> {
        _appliedPromoCode.value = "ARTESANO10"
        _promoDiscountPercent.value = 0.10
        addAudit("Promo code ARTESANO10 (10% off) applied")
        true
      }
      "TRIBECA" -> {
        _appliedPromoCode.value = "TRIBECA"
        _promoDiscountPercent.value = 0.15
        addAudit("Promo code TRIBECA (15% off) applied")
        true
      }
      "MORNING" -> {
        _appliedPromoCode.value = "MORNING"
        _promoDiscountPercent.value = 0.20
        addAudit("Promo code MORNING (20% off) applied")
        true
      }
      else -> false
    }
  }

  fun removePromoCode() {
    _appliedPromoCode.value = null
    _promoDiscountPercent.value = 0.0
  }

  // --- Order Placement ---
  fun placeOrder(
    serviceMode: ServiceMode,
    customerName: String,
    customerPhone: String,
    deliveryAddress: String,
    specialNotes: String,
    tipPercent: Double,
    paymentMethod: String
  ): Order {
    val items = _cartItems.value
    val subtotal = items.sumOf { it.totalPriceUsd }
    val discount = subtotal * _promoDiscountPercent.value
    val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
    val tax = discountedSubtotal * 0.08875 // NYC 8.875% sales tax
    val tip = discountedSubtotal * tipPercent
    val deliveryFee = serviceMode.fee
    val total = discountedSubtotal + tax + tip + deliveryFee

    val orderNumber = "#ART-${(1000..9999).random()}"
    val newOrder = Order(
      id = UUID.randomUUID().toString(),
      orderNumber = orderNumber,
      timestamp = System.currentTimeMillis(),
      items = items,
      status = OrderStatus.RECEIVED,
      serviceMode = serviceMode,
      customerName = customerName.ifBlank { "Artesano Guest" },
      customerPhone = customerPhone.ifBlank { "+1 212-372-0297" },
      deliveryAddress = deliveryAddress.ifBlank { "90 Chambers St, Unit B" },
      specialNotes = specialNotes,
      subtotal = subtotal,
      tax = tax,
      tip = tip,
      deliveryFee = deliveryFee,
      discount = discount,
      total = total,
      currency = _settings.value.activeCurrency,
      paymentMethod = paymentMethod,
      paymentRef = "tok_live_${UUID.randomUUID().toString().take(12)}",
      estimatedMinutes = when (serviceMode) {
        ServiceMode.TAKEAWAY, ServiceMode.KERBSIDE_PICKUP -> 12
        ServiceMode.DINE_IN -> 8
        ServiceMode.DELIVERY, ServiceMode.NO_CONTACT_DELIVERY -> 25
        ServiceMode.ON_SITE -> 35
      }
    )

    _orders.value = listOf(newOrder) + _orders.value
    clearCart()
    addAudit("New order placed: ${newOrder.orderNumber} ($${String.format("%.2f", total)}) via $paymentMethod")
    return newOrder
  }

  // --- Order Management (Admin) ---
  fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
    _orders.value = _orders.value.map {
      if (it.id == orderId) it.copy(status = newStatus) else it
    }
    addAudit("Order $orderId status changed to ${newStatus.label}")
  }

  fun refundOrder(orderId: String, reason: String) {
    _orders.value = _orders.value.map {
      if (it.id == orderId) it.copy(status = OrderStatus.REFUNDED, refundReason = reason) else it
    }
    addAudit("Order $orderId refunded: $reason")
  }

  // --- Menu Management (Admin) ---
  fun updateItemPrice(itemId: String, newPriceUsd: Double) {
    _menuItems.value = _menuItems.value.map {
      if (it.id == itemId) it.copy(priceUsd = newPriceUsd, priceInr = newPriceUsd * 83.0) else it
    }
    saveMenuToFile()
    addAudit("Item $itemId price updated to $${String.format("%.2f", newPriceUsd)}")
  }

  fun toggleItemStock(itemId: String) {
    _menuItems.value = _menuItems.value.map {
      if (it.id == itemId) {
        val nextStock = !it.inStock
        addAudit("Item '${it.name}' stock set to: $nextStock")
        it.copy(inStock = nextStock)
      } else it
    }
    saveMenuToFile()
  }

  fun updateMenuItem(updated: MenuItem) {
    _menuItems.value = _menuItems.value.map {
      if (it.id == updated.id) updated else it
    }
    saveMenuToFile()
    addAudit("Menu item '${updated.name}' updated")
  }

  fun addNewMenuItem(item: MenuItem) {
    _menuItems.value = listOf(item) + _menuItems.value
    saveMenuToFile()
    addAudit("Created new menu item '${item.name}'")
  }

  fun deleteMenuItem(itemId: String) {
    val found = _menuItems.value.find { it.id == itemId }
    _menuItems.value = _menuItems.value.filter { it.id != itemId }
    saveMenuToFile()
    addAudit("Deleted menu item '${found?.name ?: itemId}'")
  }

  fun reorderMenuItem(itemId: String, moveUp: Boolean) {
    val current = _menuItems.value.toMutableList()
    val index = current.indexOfFirst { it.id == itemId }
    if (index < 0) return
    val targetIndex = if (moveUp) index - 1 else index + 1
    if (targetIndex in 0 until current.size) {
      val item = current.removeAt(index)
      current.add(targetIndex, item)
      _menuItems.value = current
      saveMenuToFile()
      addAudit("Reordered item '${item.name}'")
    }
  }

  fun saveUploadedPhoto(itemId: String, sourceUri: Uri): String? {
    val ctx = appContext ?: return null
    return runCatching {
      val targetFile = File(photosDir, "dish_${itemId}_${System.currentTimeMillis()}.jpg")
      ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      val fileUriString = targetFile.absolutePath
      _menuItems.value = _menuItems.value.map {
        if (it.id == itemId) it.copy(imageUri = fileUriString) else it
      }
      saveMenuToFile()
      addAudit("Saved persistent photograph for dish ID $itemId")
      fileUriString
    }.getOrNull()
  }

  fun saveUploadedLogo(sourceUri: Uri): String? {
    val ctx = appContext ?: return null
    return runCatching {
      val targetFile = File(photosDir, "logo_${System.currentTimeMillis()}.jpg")
      ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      val path = targetFile.absolutePath
      _settings.value = _settings.value.copy(customLogoUri = path)
      saveSettingsToFile()
      addAudit("Custom café logo uploaded & published")
      path
    }.getOrNull()
  }

  fun saveUploadedBanner(sourceUri: Uri): String? {
    val ctx = appContext ?: return null
    return runCatching {
      val targetFile = File(photosDir, "banner_${System.currentTimeMillis()}.jpg")
      ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      val path = targetFile.absolutePath
      _settings.value = _settings.value.copy(customBannerUri = path)
      saveSettingsToFile()
      addAudit("Custom hero banner photograph uploaded & published")
      path
    }.getOrNull()
  }

  // --- Store Settings & Status ---
  fun updateStoreStatus(status: StoreStatus) {
    _settings.value = _settings.value.copy(status = status)
    saveSettingsToFile()
    addAudit("Store status changed to: ${status.title}")
  }

  fun toggleCurrency() {
    val next = if (_settings.value.activeCurrency == Currency.USD) Currency.INR else Currency.USD
    _settings.value = _settings.value.copy(activeCurrency = next)
    saveSettingsToFile()
    addAudit("Active currency toggled to: ${next.code}")
  }

  fun toggleTheme() {
    _settings.value = _settings.value.copy(darkTheme = !_settings.value.darkTheme)
    saveSettingsToFile()
  }

  fun updateStoreContact(phone: String, address: String, hours: String, morningHours: String) {
    _settings.value = _settings.value.copy(
      phone = phone,
      address = address,
      storeHours = hours,
      morningHours = morningHours
    )
    saveSettingsToFile()
    addAudit("Store contact and hours updated")
  }

  fun updateBannerAndThemeSettings(
    headline: String,
    subheadline: String,
    announcement: String,
    announcementActive: Boolean,
    darkTheme: Boolean
  ) {
    _settings.value = _settings.value.copy(
      bannerHeadline = headline,
      bannerSubheadline = subheadline,
      announcementText = announcement,
      announcementActive = announcementActive,
      darkTheme = darkTheme
    )
    saveSettingsToFile()
    addAudit("Website banners, announcements, and theme updated")
  }

  fun resetMenuToDefaults() {
    menuFile?.delete()
    settingsFile?.delete()
    _menuItems.value = createInitialMenuItems()
    _settings.value = StoreSettings()
    addAudit("Reset menu & settings to factory defaults")
  }

  private fun menuItemToJson(item: MenuItem): JSONObject {
    return JSONObject().apply {
      put("id", item.id)
      put("name", item.name)
      put("description", item.description)
      put("priceUsd", item.priceUsd)
      put("priceInr", item.priceInr ?: (item.priceUsd * 83.0))
      put("category", item.category.name)
      put("isMorningMenu", item.isMorningMenu)
      put("inStock", item.inStock)
      put("imageRes", item.imageRes)
      if (item.imageUri != null) put("imageUri", item.imageUri)
      put("isPopular", item.isPopular)
      put("isChefSpecial", item.isChefSpecial)
      val tags = JSONArray()
      item.dietaryTags.forEach { tags.put(it.name) }
      put("dietaryTags", tags)
    }
  }

  private fun menuItemFromJson(obj: JSONObject): MenuItem {
    val tags = mutableListOf<DietaryTag>()
    val tagsArray = obj.optJSONArray("dietaryTags")
    if (tagsArray != null) {
      for (i in 0 until tagsArray.length()) {
        runCatching { DietaryTag.valueOf(tagsArray.getString(i)) }.getOrNull()?.let { tags.add(it) }
      }
    }
    return MenuItem(
      id = obj.getString("id"),
      name = obj.getString("name"),
      description = obj.optString("description", ""),
      priceUsd = obj.optDouble("priceUsd", 5.0),
      priceInr = if (obj.has("priceInr")) obj.getDouble("priceInr") else null,
      category = runCatching { MenuCategory.valueOf(obj.getString("category")) }.getOrDefault(MenuCategory.PASTRIES),
      isMorningMenu = obj.optBoolean("isMorningMenu", false),
      inStock = obj.optBoolean("inStock", true),
      imageRes = obj.optInt("imageRes", R.drawable.ic_artesano_logo),
      imageUri = if (obj.has("imageUri")) obj.getString("imageUri") else null,
      dietaryTags = tags,
      isPopular = obj.optBoolean("isPopular", false),
      isChefSpecial = obj.optBoolean("isChefSpecial", false)
    )
  }

  private fun storeSettingsToJson(s: StoreSettings): JSONObject {
    return JSONObject().apply {
      put("status", s.status.name)
      put("phone", s.phone)
      put("address", s.address)
      put("morningHours", s.morningHours)
      put("storeHours", s.storeHours)
      put("activeCurrency", s.activeCurrency.name)
      put("darkTheme", s.darkTheme)
      put("bannerHeadline", s.bannerHeadline)
      put("bannerSubheadline", s.bannerSubheadline)
      put("announcementText", s.announcementText)
      put("announcementActive", s.announcementActive)
      if (s.customLogoUri != null) put("customLogoUri", s.customLogoUri)
      if (s.customBannerUri != null) put("customBannerUri", s.customBannerUri)
      put("adminPin", s.adminPin)
    }
  }

  private fun storeSettingsFromJson(obj: JSONObject): StoreSettings {
    return StoreSettings(
      status = runCatching { StoreStatus.valueOf(obj.getString("status")) }.getOrDefault(StoreStatus.OPEN),
      phone = obj.optString("phone", "+1 212-372-0297"),
      address = obj.optString("address", "90 Chambers St Unit B, New York, NY 10007, United States"),
      morningHours = obj.optString("morningHours", "8:00 AM – 11:00 AM"),
      storeHours = obj.optString("storeHours", "Mon – Sun: 7:30 AM – 6:00 PM"),
      activeCurrency = runCatching { Currency.valueOf(obj.getString("activeCurrency")) }.getOrDefault(Currency.USD),
      darkTheme = obj.optBoolean("darkTheme", false),
      bannerHeadline = obj.optString("bannerHeadline", "Fresh · Bright · Plant-Filled · Homemade · Daily"),
      bannerSubheadline = obj.optString("bannerSubheadline", "Morning Menu served daily 8:00 AM – 11:00 AM · Artisan Bakery & Specialty Coffee in Tribeca"),
      announcementText = obj.optString("announcementText", ""),
      announcementActive = obj.optBoolean("announcementActive", false),
      customLogoUri = if (obj.has("customLogoUri")) obj.getString("customLogoUri") else null,
      customBannerUri = if (obj.has("customBannerUri")) obj.getString("customBannerUri") else null,
      adminPin = obj.optString("adminPin", "1234")
    )
  }

  // --- Favorites ---
  fun toggleFavorite(itemId: String) {
    val current = _favorites.value
    _favorites.value = if (current.contains(itemId)) current - itemId else current + itemId
  }

  fun isFavorite(itemId: String): Boolean = _favorites.value.contains(itemId)

  private fun addAudit(msg: String) {
    _auditLogs.value = listOf("${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())} - $msg") + _auditLogs.value.take(25)
  }

  // --- Authentic Menu Items matching User Screenshots ---
  private fun createInitialMenuItems(): List<MenuItem> {
    return listOf(
      // =========================================================================
      // 1. SANDWICHES AND SAVORY OPTIONS (Printed Menu Screenshot 14)
      // =========================================================================
      MenuItem(
        id = "sw_1",
        name = "Avocado Toast",
        description = "Sourdough toast topped with smashed avocado, lime, chili flakes, and microgreens.",
        priceUsd = 9.95,
        priceInr = 825.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_avocado_toast,
        dietaryTags = listOf(DietaryTag.VEGAN),
        isChefSpecial = true,
        isPopular = true
      ),
      MenuItem(
        id = "sw_2",
        name = "Bacon Egg and Cheese",
        description = "Fried or scrambled eggs with your choice of cheese on fresh artisan bread.",
        priceUsd = 8.95,
        priceInr = 745.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        isPopular = true
      ),
      MenuItem(
        id = "sw_3",
        name = "Classic Omelette",
        description = "Folded golden omelette served with seasoned home fries and a fresh herb side salad.",
        priceUsd = 13.95,
        priceInr = 1160.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_omelette,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isChefSpecial = true
      ),
      MenuItem(
        id = "sw_4",
        name = "Veggie Omelette",
        description = "Farm-fresh omelette packed with sautéed peppers, spinach, and onions, served with home fries and side salad.",
        priceUsd = 15.95,
        priceInr = 1325.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_omelette,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isChefSpecial = true
      ),
      MenuItem(
        id = "sw_5",
        name = "Artesano Harvest Salad",
        description = "Arugula, raisins, quinoa, pear, orange, radish, and toasted almond with honey vinaigrette.",
        priceUsd = 14.95,
        priceInr = 1240.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.GLUTEN_FREE, DietaryTag.CONTAINS_NUTS)
      ),
      MenuItem(
        id = "sw_6",
        name = "Pan con Chicharrón",
        description = "Crispy pork belly, fried sweet potato, and salsa criolla on fresh bread with zesty dipping sauce.",
        priceUsd = 10.95,
        priceInr = 910.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_chicharron,
        isChefSpecial = true,
        isPopular = true
      ),
      MenuItem(
        id = "sw_7",
        name = "Caprese Sandwich",
        description = "Fresh Mozzarella, sliced ripe tomato, fresh basil with sweet balsamic glaze on crusty ciabatta bread.",
        priceUsd = 9.95,
        priceInr = 825.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_caprese,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "sw_8",
        name = "Turkey and Avocado Sandwich",
        description = "Sliced roasted turkey, creamy Hass avocado, arugula, and house aioli on toasted whole wheat bread.",
        priceUsd = 14.95,
        priceInr = 1240.0,
        category = MenuCategory.SANDWICHES_SAVOURY,
        imageRes = R.drawable.img_avocado_toast,
        isPopular = true
      ),
      MenuItem(
        id = "sw_9",
        name = "Pesto Chicken Sandwich",
        description = "Tender grilled chicken breast, aromatic basil pesto, ripe tomato, and fresh arugula on crusty baguette.",
        priceUsd = 12.95,
        priceInr = 1075.0,
        category = MenuCategory.SANDWICHES_SAVOURY
      ),

      // =========================================================================
      // 2. PASTRIES AND BAKED GOODS (Printed Menu Screenshot 14 & Slide 4)
      // =========================================================================
      MenuItem(
        id = "p_1",
        name = "Peruvian Alfajores (6 pcs)",
        description = "Soft shortbread cookies filled with authentic rich dulce de leche and heavily dusted with powdered sugar.",
        priceUsd = 4.95,
        priceInr = 700.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_alfajores,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isChefSpecial = true,
        isPopular = true
      ),
      MenuItem(
        id = "p_2",
        name = "Croissant",
        description = "Classic buttery and flaky French artisan croissant, baked fresh daily.",
        priceUsd = 4.95,
        priceInr = 280.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "p_3",
        name = "Lucuma Croissant",
        description = "Buttery and flaky croissant with a sweet Peruvian lucuma fruit filling, baked fresh daily.",
        priceUsd = 6.95,
        priceInr = 580.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isChefSpecial = true
      ),
      MenuItem(
        id = "p_4",
        name = "Ham and Cheese Croissant",
        description = "Savory toasted croissant filled with tender ham and melted Gruyere cheese.",
        priceUsd = 6.95,
        priceInr = 580.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        isPopular = true
      ),
      MenuItem(
        id = "p_5",
        name = "Almond Croissant",
        description = "Buttery flaky croissant filled with frangipane almond cream and topped with sliced almonds & powdered sugar.",
        priceUsd = 5.50,
        priceInr = 400.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.CONTAINS_NUTS),
        isPopular = true
      ),
      MenuItem(
        id = "p_6",
        name = "Chocolate Cookie",
        description = "Fresh baked golden chocolate chip cookie with gooey melted chocolate chunks on oval wood board.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_cookies,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "p_7",
        name = "Pain Au Chocolat",
        description = "Flaky layered croissant dough filled with two batons of French bittersweet dark chocolate.",
        priceUsd = 4.95,
        priceInr = 380.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "p_8",
        name = "Yogurt Parfait",
        description = "House-made crunchy granola layered with velvety Greek yogurt and sweet fresh fruit.",
        priceUsd = 8.95,
        priceInr = 745.0,
        category = MenuCategory.PASTRIES,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "p_9",
        name = "Walnut Brownie",
        description = "Rich Valrhona double-chocolate fudge brownie with toasted California walnut halves.",
        priceUsd = 4.00,
        priceInr = 320.0,
        category = MenuCategory.PASTRIES,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.CONTAINS_NUTS)
      ),
      MenuItem(
        id = "p_10",
        name = "Butter Croissant",
        description = "Classic European butter croissant, golden flaky layers.",
        priceUsd = 3.50,
        priceInr = 280.0,
        category = MenuCategory.PASTRIES,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),

      // =========================================================================
      // 3. COFFEE AND TEA (Printed Menu Screenshot 15)
      // =========================================================================
      MenuItem(
        id = "c_1",
        name = "Espresso",
        description = "Concentrated single shot of our Italian roast with dense golden crema.",
        priceUsd = 3.50,
        priceInr = 290.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "c_2",
        name = "Double Espresso",
        description = "Double extract of freshly ground Arabica beans, bold and aromatic.",
        priceUsd = 4.00,
        priceInr = 330.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "c_3",
        name = "Americano",
        description = "Double espresso lengthened with hot filtered water for clean, nuanced depth.",
        priceUsd = 4.00,
        priceInr = 330.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "c_4",
        name = "Cortado",
        description = "Equal parts rich espresso and silky warm milk in a black marble ceramic cup with tulip latte art.",
        priceUsd = 4.00,
        priceInr = 330.0,
        category = MenuCategory.COFFEE_TEA,
        imageRes = R.drawable.img_cortado,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.GLUTEN_FREE),
        isChefSpecial = true,
        isPopular = true
      ),
      MenuItem(
        id = "c_5",
        name = "Latte",
        description = "Smooth espresso balanced with silky steamed microfoam milk and delicate latte art.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "c_6",
        name = "Cappuccino",
        description = "Classic third-wave proportion of rich espresso, steamed milk, and a dense cloud of foam.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_7",
        name = "Flat White",
        description = "Double ristretto poured with finely micro-textured velvety milk for an intense espresso profile.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_8",
        name = "Mocha",
        description = "Espresso infused with Dutch dark cocoa ganache and silky steamed milk.",
        priceUsd = 5.00,
        priceInr = 415.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_9",
        name = "Matcha Latte",
        description = "Ceremonial Japanese matcha micro-foamed with your choice of steamed milk.",
        priceUsd = 5.00,
        priceInr = 415.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_10",
        name = "Chai Latte",
        description = "Slow-simmered spiced black tea with cinnamon, cardamom, ginger, clove, and warm honey.",
        priceUsd = 5.00,
        priceInr = 415.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_11",
        name = "Iced Coffee",
        description = "Cold brew steeped for 18 hours in Tribeca for a naturally sweet, low-acidity glass.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "c_12",
        name = "Iced Latte",
        description = "Double espresso poured over ice and cold fresh milk.",
        priceUsd = 5.50,
        priceInr = 455.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "c_13",
        name = "Tea",
        description = "Selection of organic loose-leaf teas: Earl Grey, Jasmine Green, Chamomile Blossom, or Peppermint.",
        priceUsd = 5.00,
        priceInr = 415.0,
        category = MenuCategory.COFFEE_TEA,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),

      // =========================================================================
      // 4. COLD BEVERAGES (Printed Menu Screenshot 15)
      // =========================================================================
      MenuItem(
        id = "cb_1",
        name = "Fresh Orange Juice",
        description = "Cold-pressed to order from sweet Valencia oranges, 100% pure juice with no additives.",
        priceUsd = 6.00,
        priceInr = 500.0,
        category = MenuCategory.COLD_BEVERAGES,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.ORGANIC)
      ),
      MenuItem(
        id = "cb_2",
        name = "Fresh Lemonade",
        description = "Hand-squeezed Meyer lemons balanced with a hint of organic agave and fresh mint sprig.",
        priceUsd = 6.00,
        priceInr = 500.0,
        category = MenuCategory.COLD_BEVERAGES,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "cb_3",
        name = "Ginger Shot",
        description = "Potent cold-pressed fresh Peruvian ginger root, lemon, and a dash of cayenne pepper.",
        priceUsd = 3.00,
        priceInr = 250.0,
        category = MenuCategory.COLD_BEVERAGES,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.ORGANIC)
      ),
      MenuItem(
        id = "cb_4",
        name = "Sparkling Water",
        description = "San Pellegrino Italian sparkling mineral water, crisp and effervescent.",
        priceUsd = 3.00,
        priceInr = 250.0,
        category = MenuCategory.COLD_BEVERAGES,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),

      // =========================================================================
      // 5. SIGNATURE DRINKS (Printed Menu Screenshot 15)
      // =========================================================================
      MenuItem(
        id = "sd_1",
        name = "Green Juice",
        description = "A refreshing blend of fresh greens, fruits, and veggies (kale, spinach, celery, green apple, ginger).",
        priceUsd = 6.50,
        priceInr = 540.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.ORGANIC),
        isPopular = true
      ),
      MenuItem(
        id = "sd_2",
        name = "NY Beet Juice",
        description = "A refreshing blend of fresh beet, pineapple, cucumber, and lime.",
        priceUsd = 8.00,
        priceInr = 665.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.ORGANIC)
      ),
      MenuItem(
        id = "sd_3",
        name = "Glow Juice",
        description = "A refreshing blend of carrot, orange, ginger, and golden turmeric root.",
        priceUsd = 8.00,
        priceInr = 665.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.ORGANIC),
        isChefSpecial = true
      ),
      MenuItem(
        id = "sd_4",
        name = "Lucuma Latte",
        description = "Creamy latte infused with lucuma, a Peruvian Superfruit with flavors of caramel and maple.",
        priceUsd = 5.50,
        priceInr = 455.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isChefSpecial = true,
        isPopular = true
      ),
      MenuItem(
        id = "sd_5",
        name = "Chicha Morada Iced Tea",
        description = "Sweet Peruvian purple corn and pineapple tea spiced with cinnamon and cloves, served chilled with lime.",
        priceUsd = 5.50,
        priceInr = 455.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE),
        isChefSpecial = true
      ),
      MenuItem(
        id = "sd_6",
        name = "Sodas",
        description = "Assorted artisanal sodas (San Pellegrino Aranciata, Chinotto, craft cola).",
        priceUsd = 3.00,
        priceInr = 250.0,
        category = MenuCategory.SIGNATURE_DRINKS,
        dietaryTags = listOf(DietaryTag.VEGAN)
      ),

      // =========================================================================
      // 6. MORNING MENU (Morning Menu Illustrated Screenshot 17 & 18)
      // =========================================================================
      MenuItem(
        id = "m_1",
        name = "Maple Cinnamon Latte",
        description = "Rich espresso with steamed oat milk, pure Vermont maple syrup, and freshly dusted Ceylon cinnamon.",
        priceUsd = 5.50,
        priceInr = 455.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "m_2",
        name = "Witch's Brew Matcha",
        description = "Ceremonial grade Uji matcha whisked with lavender-infused vanilla syrup and creamy pistachio milk.",
        priceUsd = 6.00,
        priceInr = 500.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.CONTAINS_NUTS),
        isChefSpecial = true
      ),
      MenuItem(
        id = "m_3",
        name = "Bacon Egg & Cheese Sourdough",
        description = "Crispy applewood smoked bacon, pasture-raised egg, melted aged cheddar, and house garlic aioli on warm toasted sourdough.",
        priceUsd = 11.00,
        priceInr = 910.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        isPopular = true
      ),
      MenuItem(
        id = "m_4",
        name = "Walnut Brownie",
        description = "Decadent dark Valrhona chocolate fudge brownie folded with toasted California walnut halves.",
        priceUsd = 4.00,
        priceInr = 320.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.CONTAINS_NUTS)
      ),
      MenuItem(
        id = "m_5",
        name = "Chives Cheddar Scones",
        description = "Flaky, buttery savory scone studded with sharp Vermont white cheddar and aromatic fresh chives.",
        priceUsd = 4.50,
        priceInr = 375.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "m_6",
        name = "Grilled Cheese with Fig Jam",
        description = "Melted French Gruyère and aged white cheddar layered with sweet Mission fig jam on golden sourdough bread.",
        priceUsd = 10.00,
        priceInr = 830.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN),
        isPopular = true
      ),
      MenuItem(
        id = "m_7",
        name = "Tater Tots",
        description = "Extra-crispy golden potato tots seasoned with smoked paprika, sea salt, and fresh herbs.",
        priceUsd = 5.00,
        priceInr = 415.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "m_8",
        name = "Fig Jam (side)",
        description = "House-made artisanal Mission fig jam simmered with lemon peel and pure cane sugar.",
        priceUsd = 2.00,
        priceInr = 165.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "m_9",
        name = "Almond Croissant",
        description = "Twice-baked buttery croissant filled with silky frangipane almond cream and topped with toasted sliced almonds.",
        priceUsd = 4.50,
        priceInr = 400.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.CONTAINS_NUTS),
        isPopular = true
      ),
      MenuItem(
        id = "m_10",
        name = "Island Matcha",
        description = "Refreshing tropical twist with ceremonial matcha shaken over iced coconut water and pineapple puree.",
        priceUsd = 6.00,
        priceInr = 500.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE)
      ),
      MenuItem(
        id = "m_11",
        name = "Butter Croissant",
        description = "Classic French artisan croissant laminated with cultured Normandy butter for delicate, honeycomb layers.",
        priceUsd = 3.50,
        priceInr = 280.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        imageRes = R.drawable.img_pastries_selection,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      ),
      MenuItem(
        id = "m_12",
        name = "Armaretti Latte",
        description = "Velvety espresso flavored with sweet Italian almond amaretto notes and topped with crushed amaretti biscuit crumb.",
        priceUsd = 5.50,
        priceInr = 455.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN, DietaryTag.CONTAINS_NUTS),
        isChefSpecial = true
      ),
      MenuItem(
        id = "m_13",
        name = "Vegan Tofu Avocado Sandwich",
        description = "Marinated grilled organic tofu, creamy Hass avocado, sliced heirloom tomatoes, crisp cucumber, and green goddess tahini on multigrain.",
        priceUsd = 12.00,
        priceInr = 995.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGAN)
      ),
      MenuItem(
        id = "m_14",
        name = "Iced Latte",
        description = "Double shot of single-origin espresso poured over cold whole milk and slow-melting crystal ice.",
        priceUsd = 4.75,
        priceInr = 395.0,
        category = MenuCategory.MORNING,
        isMorningMenu = true,
        dietaryTags = listOf(DietaryTag.VEGETARIAN)
      )
    )
  }

  private fun createInitialReviews(): List<Review> {
    return listOf(
      Review(
        id = "r_1",
        author = "Elena Vasquez",
        rating = 5.0f,
        quote = "The alfajores are an absolute sweet treat! Authentic dulce de leche, and the cappuccino is third-wave perfection right here in Tribeca.",
        date = "2 days ago"
      ),
      Review(
        id = "r_2",
        author = "Marcus Sterling",
        rating = 5.0f,
        quote = "The avocado toast is a standout choice — thick crusty sourdough with the freshest microgreens. Their Italian roast Cortado pairs wonderfully.",
        date = "1 week ago"
      ),
      Review(
        id = "r_3",
        author = "Sofia Rossi",
        rating = 5.0f,
        quote = "Endless praise for the chicharrón and the coffee. The Pan con Chicharrón bun is golden, crispy, and the orange sauce is to die for!",
        date = "2 weeks ago"
      ),
      Review(
        id = "r_4",
        author = "David Chen",
        rating = 4.8f,
        quote = "A plant-filled oasis on Chambers St with warm Italian hospitality. The Bacon Egg & Cheese Sourdough elevates classic NYC breakfast to an art form.",
        date = "3 weeks ago"
      )
    )
  }

  private fun createSampleOrders(): List<Order> {
    val sampleItem = createInitialMenuItems().first { it.id == "sw_6" } // Pan con Chicharrón $10.95
    val sampleCoffee = createInitialMenuItems().first { it.id == "c_4" } // Cortado $4.00
    val sampleAlfajores = createInitialMenuItems().first { it.id == "p_1" } // Alfajores $4.95
    val cartItems = listOf(
      CartItem(
        cartId = "sample_c1",
        item = sampleItem,
        quantity = 1,
        selectedSize = "Regular",
        specialInstructions = "Crispy chicharrón please"
      ),
      CartItem(
        cartId = "sample_c2",
        item = sampleCoffee,
        quantity = 1,
        selectedSize = "Regular",
        selectedMilk = "Whole Milk"
      ),
      CartItem(
        cartId = "sample_c3",
        item = sampleAlfajores,
        quantity = 1
      )
    )
    val subtotal = sampleItem.priceUsd + sampleCoffee.priceUsd + sampleAlfajores.priceUsd
    val tax = subtotal * 0.08875
    val tip = subtotal * 0.15
    return listOf(
      Order(
        id = "ord_recent_1",
        orderNumber = "#ART-9241",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
        items = cartItems,
        status = OrderStatus.PREPARING,
        serviceMode = ServiceMode.KERBSIDE_PICKUP,
        customerName = "Alexander Wright",
        customerPhone = "+1 212-555-0199",
        deliveryAddress = "90 Chambers St Kerbside Spot #2",
        specialNotes = "Black Tesla Model 3 outside Chambers St",
        subtotal = subtotal,
        tax = tax,
        tip = tip,
        deliveryFee = 0.0,
        discount = 0.0,
        total = subtotal + tax + tip,
        currency = Currency.USD,
        paymentMethod = "Apple Pay (Encrypted)",
        paymentRef = "tok_live_app77210x",
        estimatedMinutes = 7
      )
    )
  }
}
