package com.pakhshmahdi.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.auth.AuthStore
import com.pakhshmahdi.app.data.cart.CartStore
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.notifications.OrderStatusWorker
import com.pakhshmahdi.app.data.recent.RecentlyViewedStore
import com.pakhshmahdi.app.data.wishlist.WishlistStore
import com.pakhshmahdi.app.ui.PakhshMahdiApp
import com.pakhshmahdi.app.ui.theme.PakhshMahdiTheme
import com.pakhshmahdi.app.ui.theme.PMThemeController

class MainActivity : ComponentActivity() {
    private val notificationOrderId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationOrderId.value = intent.notificationOrderId()

        LocalStore.init(this)
        PMThemeController.init(this)
        AuthStore.restore()
        CartStore.restore()
        WishlistStore.restore()
        RecentlyViewedStore.restore()
        if (AppConfig.FEATURE_NOTIFICATIONS) {
            OrderStatusWorker.schedule(this)
        }

        enableEdgeToEdge()
        setContent {
            PakhshMahdiTheme {
                PakhshMahdiApp(
                    notificationOrderId = notificationOrderId.value,
                    onNotificationOrderConsumed = {
                        notificationOrderId.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationOrderId.value = intent.notificationOrderId()
    }

    private fun Intent.notificationOrderId(): Long? =
        getLongExtra(EXTRA_ORDER_ID, -1L).takeIf { it > 0L }

    companion object {
        const val EXTRA_ORDER_ID = "notification_order_id"
    }
}
