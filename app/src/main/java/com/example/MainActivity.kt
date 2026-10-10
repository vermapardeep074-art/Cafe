package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.ArtesanoTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = androidx.compose.ui.platform.LocalContext.current
      val viewModel: ArtesanoViewModel = viewModel()
      LaunchedEffect(Unit) {
        viewModel.setContext(context)
      }
      val settings by viewModel.settings.collectAsStateWithLifecycle()

      ArtesanoTheme(darkTheme = settings.darkTheme) {
        ArtesanoApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun ArtesanoApp(viewModel: ArtesanoViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
  val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
  val orders by viewModel.orders.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val reviews by viewModel.reviews.collectAsStateWithLifecycle()
  val favorites by viewModel.favorites.collectAsStateWithLifecycle()
  val appliedPromoCode by viewModel.appliedPromoCode.collectAsStateWithLifecycle()
  val promoDiscountPercent by viewModel.promoDiscountPercent.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedDietaryTag by viewModel.selectedDietaryTag.collectAsStateWithLifecycle()
  val detailItem by viewModel.detailItem.collectAsStateWithLifecycle()
  val trackingOrder by viewModel.trackingOrder.collectAsStateWithLifecycle()

  val serviceMode by viewModel.serviceMode.collectAsStateWithLifecycle()
  val customerName by viewModel.customerName.collectAsStateWithLifecycle()
  val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()
  val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
  val specialNotes by viewModel.specialNotes.collectAsStateWithLifecycle()
  val selectedTipPercent by viewModel.selectedTipPercent.collectAsStateWithLifecycle()
  val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
  val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
  val targetEditItemId by viewModel.targetEditItemId.collectAsStateWithLifecycle()
  val notification by viewModel.notification.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  // Handle system back navigation
  BackHandler(enabled = currentScreen != AppDestination.HOME || detailItem != null) {
    if (detailItem != null) {
      viewModel.closeDetail()
    } else {
      viewModel.navigateTo(AppDestination.HOME)
    }
  }

  // Show notifications
  LaunchedEffect(notification) {
    notification?.let {
      snackbarHostState.showSnackbar(
        message = it.message,
        duration = SnackbarDuration.Short
      )
      viewModel.dismissNotification()
    }
  }

  val cartCount = cartItems.sumOf { it.quantity }
  val cartSubtotal = cartItems.sumOf { it.totalPriceUsd }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      ArtesanoTopBar(
        currentScreen = currentScreen,
        cartCount = cartCount,
        storeStatus = settings.status,
        activeCurrency = settings.activeCurrency,
        darkTheme = settings.darkTheme,
        customLogoUri = settings.customLogoUri,
        announcementText = settings.announcementText,
        announcementActive = settings.announcementActive,
        onNavigate = { viewModel.navigateTo(it) },
        onToggleCurrency = { viewModel.toggleCurrency() },
        onToggleTheme = { viewModel.toggleTheme() },
        onOpenAdmin = { viewModel.navigateTo(AppDestination.ADMIN) }
      )
    },
    bottomBar = {
      Column {
        // Sticky KFC/McDonald's-style Quick Cart Bar when viewing Home or Menu
        if ((currentScreen == AppDestination.HOME || currentScreen == AppDestination.MENU) && cartCount > 0) {
          KfcStyleStickyCartBar(
            itemCount = cartCount,
            subtotalUsd = cartSubtotal,
            activeCurrency = settings.activeCurrency,
            onOpenCart = { viewModel.navigateTo(AppDestination.CART) }
          )
        }

        ArtesanoBottomNav(
          currentScreen = currentScreen,
          cartCount = cartCount,
          onNavigate = { viewModel.navigateTo(it) }
        )
      }
    },
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppDestination.HOME -> {
          HomeScreen(
            menuItems = menuItems,
            reviews = reviews,
            storeSettings = settings,
            activeCurrency = settings.activeCurrency,
            favorites = favorites,
            onOrderNowClick = {
              viewModel.selectCategory(null)
              viewModel.navigateTo(AppDestination.MENU)
            },
            onItemClick = { viewModel.openDetail(it) },
            onQuickAdd = { viewModel.addToCart(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onViewMorningMenu = {
              viewModel.selectCategory(MenuCategory.MORNING)
              viewModel.navigateTo(AppDestination.MENU)
            },
            onOpenAdmin = { viewModel.navigateTo(AppDestination.ADMIN) },
            onEditItem = { viewModel.openAdminForDish(it.id) }
          )
        }

        AppDestination.MENU -> {
          MenuScreen(
            menuItems = menuItems,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            selectedDietaryTag = selectedDietaryTag,
            activeCurrency = settings.activeCurrency,
            favorites = favorites,
            onSelectCategory = { viewModel.selectCategory(it) },
            onUpdateSearch = { viewModel.updateSearchQuery(it) },
            onSelectDietaryTag = { viewModel.selectDietaryTag(it) },
            onItemClick = { viewModel.openDetail(it) },
            onQuickAdd = { viewModel.addToCart(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onEditItem = { viewModel.openAdminForDish(it.id) }
          )
        }

        AppDestination.CART -> {
          val upsellItems = remember(menuItems) {
            menuItems.filter { it.isChefSpecial || it.category == MenuCategory.PASTRIES || it.category == MenuCategory.SIGNATURE_DRINKS }
          }
          CartScreen(
            cartItems = cartItems,
            upsellItems = upsellItems,
            activeCurrency = settings.activeCurrency,
            serviceMode = serviceMode,
            appliedPromoCode = appliedPromoCode,
            promoDiscountPercent = promoDiscountPercent,
            selectedTipPercent = selectedTipPercent,
            onUpdateQuantity = { id, delta -> viewModel.updateCartQuantity(id, delta) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onSelectServiceMode = { mode -> viewModel.updateCheckoutDetails(mode = mode) },
            onApplyPromo = { code -> viewModel.applyPromoCode(code) },
            onRemovePromo = { viewModel.removePromoCode() },
            onSelectTip = { tip -> viewModel.updateCheckoutDetails(tip = tip) },
            onQuickAddUpsell = { item -> viewModel.addToCart(item) },
            onProceedToCheckout = {
              viewModel.navigateTo(AppDestination.CHECKOUT)
            },
            onBrowseMenu = { viewModel.navigateTo(AppDestination.MENU) }
          )
        }

        AppDestination.CHECKOUT -> {
          CheckoutScreen(
            cartItems = cartItems,
            serviceMode = serviceMode,
            activeCurrency = settings.activeCurrency,
            appliedPromoCode = appliedPromoCode,
            promoDiscountPercent = promoDiscountPercent,
            selectedTipPercent = selectedTipPercent,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            specialNotes = specialNotes,
            selectedPaymentMethod = selectedPaymentMethod,
            onUpdateDetails = { mode, name, phone, address, notes, tip, method ->
              viewModel.updateCheckoutDetails(mode, name, phone, address, notes, tip, method)
            },
            onBackToCart = { viewModel.navigateTo(AppDestination.CART) },
            onOrderSuccess = {
              val placed = viewModel.submitOrder()
              if (placed != null) {
                viewModel.trackOrder(placed)
              }
            }
          )
        }

        AppDestination.TRACKING -> {
          OrderTrackingScreen(
            order = trackingOrder,
            activeCurrency = settings.activeCurrency,
            onOrderAgainClick = { viewModel.navigateTo(AppDestination.MENU) }
          )
        }

        AppDestination.ABOUT -> {
          AboutScreen(storeSettings = settings)
        }

        AppDestination.ACCOUNT -> {
          AccountScreen(
            customerName = customerName,
            customerPhone = customerPhone,
            orders = orders,
            favorites = favorites,
            menuItems = menuItems,
            activeCurrency = settings.activeCurrency,
            darkTheme = settings.darkTheme,
            onToggleCurrency = { viewModel.toggleCurrency() },
            onToggleTheme = { viewModel.toggleTheme() },
            onTrackOrder = { viewModel.trackOrder(it) },
            onItemClick = { viewModel.openDetail(it) },
            onQuickAdd = { viewModel.addToCart(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenAdmin = { viewModel.navigateTo(AppDestination.ADMIN) },
            onEditItem = { viewModel.openAdminForDish(it.id) }
          )
        }

        AppDestination.ADMIN -> {
          AdminScreen(
            isAdminAuthenticated = isAdminAuthenticated,
            storeSettings = settings,
            menuItems = menuItems,
            orders = orders,
            auditLogs = auditLogs,
            activeCurrency = settings.activeCurrency,
            onAuthenticate = { pin -> viewModel.authenticateAdmin(pin) },
            onLogout = { viewModel.logoutAdmin() },
            onUpdateStoreStatus = { status -> viewModel.setStoreStatus(status) },
            onUpdateItemPrice = { id, price -> viewModel.updateItemPrice(id, price) },
            onToggleItemStock = { id -> viewModel.toggleItemStock(id) },
            onUpdateMenuItem = { item -> viewModel.updateMenuItem(item) },
            onAddNewMenuItem = { item -> viewModel.addNewMenuItem(item) },
            onDeleteMenuItem = { id -> viewModel.deleteMenuItem(id) },
            onUpdateOrderStatus = { id, status -> viewModel.updateOrderStatus(id, status) },
            onRefundOrder = { id, reason -> viewModel.refundOrder(id, reason) },
            onUpdateStoreContact = { phone, address, hours, morningHours ->
              viewModel.updateStoreContact(phone, address, hours, morningHours)
            },
            onUploadDishPhoto = { itemId, uri -> viewModel.uploadDishPhoto(itemId, uri) },
            onReorderMenuItem = { itemId, moveUp -> viewModel.reorderDish(itemId, moveUp) },
            onUpdateBannerAndTheme = { headline, subheadline, announcement, announcementActive, darkTheme ->
              viewModel.updateBannerAndTheme(headline, subheadline, announcement, announcementActive, darkTheme)
            },
            onUploadCustomLogo = { uri -> viewModel.uploadCustomLogo(uri) },
            onUploadCustomBanner = { uri -> viewModel.uploadCustomBanner(uri) },
            onResetDefaults = { viewModel.resetMenuDefaults() },
            targetEditItemId = targetEditItemId,
            onClearTargetEditItem = { viewModel.clearTargetEditItem() }
          )
        }
      }

      // Customization / Detail Dialog
      detailItem?.let { item ->
        ItemDetailDialog(
          item = item,
          activeCurrency = settings.activeCurrency,
          onDismiss = { viewModel.closeDetail() },
          onEditInAdmin = { viewModel.openAdminForDish(it.id) },
          onAddToCart = { qty, size, sAddon, milk, mAddon, extras, notes ->
            viewModel.addToCart(item, qty, size, sAddon, milk, mAddon, extras, notes)
          }
        )
      }
    }
  }
}
