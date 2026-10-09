package com.pakhshmahdi.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.feature.auth.LoginScreen
import com.pakhshmahdi.app.feature.cart.CartScreen
import com.pakhshmahdi.app.feature.catalog.CatalogScreen
import com.pakhshmahdi.app.feature.catalog.CategoriesScreen
import com.pakhshmahdi.app.feature.catalog.SearchScreen
import com.pakhshmahdi.app.feature.checkout.CheckoutScreen
import com.pakhshmahdi.app.feature.home.HomeScreen
import com.pakhshmahdi.app.feature.product.ProductDetailScreen
import com.pakhshmahdi.app.feature.profile.AccountEditScreen
import com.pakhshmahdi.app.feature.profile.NotificationsScreen
import com.pakhshmahdi.app.feature.profile.OrdersScreen
import com.pakhshmahdi.app.feature.profile.OrderTrackingScreen
import com.pakhshmahdi.app.feature.profile.SettingsScreen
import com.pakhshmahdi.app.feature.profile.SupportScreen
import com.pakhshmahdi.app.feature.profile.OrderDetailScreen
import com.pakhshmahdi.app.feature.profile.ProfileScreen
import com.pakhshmahdi.app.feature.splash.SplashScreen
import com.pakhshmahdi.app.feature.wishlist.WishlistScreen
import com.pakhshmahdi.app.ui.components.AppBottomBar

@Composable
fun PakhshMahdiApp(
    notificationOrderId: Long? = null,
    onNotificationOrderConsumed: () -> Unit = {}
) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route ?: "splash"

    LaunchedEffect(notificationOrderId, current, AuthStore.isLoggedIn) {
        val orderId = notificationOrderId ?: return@LaunchedEffect
        if (current == "splash") return@LaunchedEffect

        if (AuthStore.isLoggedIn) {
            nav.navigate("order/$orderId") {
                launchSingleTop = true
            }
        } else {
            nav.navigate("login?next=order/$orderId") {
                launchSingleTop = true
            }
        }

        onNotificationOrderConsumed()
    }

    fun goHome() {
        val returned = nav.popBackStack("home", inclusive = false)
        if (!returned && current != "home") {
            nav.navigate("home") {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun goTopLevel(route: String) {
        if (route == "home") {
            goHome()
            return
        }
        nav.navigate(route) {
            popUpTo("home") { inclusive = false }
            launchSingleTop = true
            restoreState = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (
                current != "splash" &&
                current != "checkout" &&
                current != "account-edit" &&
                current != "orders" &&
                current != "notifications" &&
                current != "support" &&
                current != "settings" &&
                !current.startsWith("login") &&
                !current.startsWith("product/") &&
                !current.startsWith("order/") &&
                !current.startsWith("order-tracking/")
            ) {
                AppBottomBar(
                    selected = when {
                        current == "home" -> "home"
                        current.startsWith("catalog") || current == "categories" || current == "search" -> "categories"
                        current == "cart" -> "cart"
                        current == "profile" -> "profile"
                        else -> "home"
                    },
                    onHome = { goHome() },
                    onCatalog = { goTopLevel("categories") },
                    onCart = { goTopLevel("cart") },
                    onProfile = { goTopLevel("profile") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(
                    onDone = {
                        nav.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    onProduct = { nav.navigate("product/$it") },
                    onCatalog = { nav.navigate("catalog") },
                    onCategory = { categoryId -> nav.navigate("catalog/$categoryId") },
                    onSearch = { nav.navigate("search") },
                    onCategories = { goTopLevel("categories") },
                    onNotifications = { nav.navigate("notifications") },
                    onWishlist = { nav.navigate("wishlist") },
                    onCart = { goTopLevel("cart") }
                )
            }
            composable("categories") {
                CategoriesScreen(
                    onCategory = { categoryId -> nav.navigate("catalog/$categoryId") }
                )
            }
            composable("search") {
                SearchScreen(
                    onProduct = { nav.navigate("product/$it") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("catalog") {
                CatalogScreen(
                    onProduct = { nav.navigate("product/$it") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                "catalog/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
            ) { backStack ->
                CatalogScreen(
                    initialCategoryId = backStack.arguments?.getLong("categoryId"),
                    onProduct = { nav.navigate("product/$it") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("wishlist") {
                WishlistScreen(onProduct = { nav.navigate("product/$it") })
            }
            composable("cart") {
                CartScreen(
                    onCheckout = {
                        if (AuthStore.isLoggedIn) {
                            nav.navigate("checkout")
                        } else {
                            nav.navigate("login?next=checkout")
                        }
                    }
                )
            }
            composable("checkout") { CheckoutScreen(onBack = { nav.popBackStack() }) }
            composable("profile") {
                ProfileScreen(
                    onLogin = { nav.navigate("login?next=profile") },
                    onEdit = { nav.navigate("account-edit") },
                    onOrders = { nav.navigate("orders") },
                    onNotifications = { nav.navigate("notifications") },
                    onSupport = { nav.navigate("support") },
                    onSettings = { nav.navigate("settings") }
                )
            }
            composable("account-edit") {
                AccountEditScreen(
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.popBackStack() }
                )
            }
            composable("orders") {
                OrdersScreen(
                    onBack = { nav.popBackStack() },
                    onOrder = { orderId -> nav.navigate("order/$orderId") },
                    onLogin = { nav.navigate("login?next=orders") }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onBack = { nav.popBackStack() },
                    onOrder = { orderId -> nav.navigate("order/$orderId") },
                    onLogin = { nav.navigate("login?next=notifications") }
                )
            }
            composable("support") {
                SupportScreen(onBack = { nav.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable(
                "order/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val orderId = backStack.arguments?.getLong("id") ?: 0L
                OrderDetailScreen(
                    orderId = orderId,
                    onBack = { nav.popBackStack() },
                    onTrack = { nav.navigate("order-tracking/$orderId") },
                    onLogin = { nav.navigate("login?next=order/$orderId") }
                )
            }
            composable(
                "order-tracking/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val orderId = backStack.arguments?.getLong("id") ?: 0L
                OrderTrackingScreen(
                    orderId = orderId,
                    onBack = { nav.popBackStack() },
                    onLogin = { nav.navigate("login?next=order-tracking/$orderId") }
                )
            }
            composable(
                route = "login?next={next}",
                arguments = listOf(
                    navArgument("next") {
                        type = NavType.StringType
                        defaultValue = "profile"
                    }
                )
            ) { backStack ->
                val next = backStack.arguments?.getString("next") ?: "profile"
                LoginScreen(
                    onBack = { nav.popBackStack() },
                    onSuccess = {
                        nav.popBackStack()
                        when {
                            next == "checkout" -> nav.navigate("checkout")
                            next == "orders" -> nav.navigate("orders")
                            next == "notifications" -> nav.navigate("notifications")
                            next.startsWith("order/") -> nav.navigate(next)
                            next.startsWith("order-tracking/") -> nav.navigate(next)
                        }
                    }
                )
            }
            composable(
                "product/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                ProductDetailScreen(
                    id = backStack.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onBuy = { nav.navigate("cart") }
                )
            }
        }
    }
}
