# Pakhsh Mahdi native AI assistant — customer Android app

This is a **source-only feature branch**, not an APK, not merged, and not
connected to a live paid AI provider.

## Delivered in Kotlin / Jetpack Compose

- A native Persian RTL conversation screen, opened from the home screen CTA
  and signed-in profile, using the existing YekanBakh theme and black/gold brand.
- One conversational state per authenticated account, cleared immediately
  when the auth token changes; no disk or analytics transcript persistence.
- Real API replies only. No scripted/fake AI messages. If the secure server
  is absent, show a clear unavailable state and a deliberate retry control.
- Login redirect back to assistant after the existing customer login flow.
- Explicit opt-in before sending chat messages. No private orders,
  addresses, phone/email or any model/provider key in APK payloads.
- The system Android fa-IR speech recognizer inserts text into the compose
  input; optional user-tapped TTS speaks the returned answer. No silent
  microphone recording or background audio stream.
- The feature flag is enabled for PM only; other white-label clients default
  off unless individually approved.

## Server contract and dependency

Uses existing IAMIR app REST host:
`https://pakhshemahdi.com/wp-json/iamir-app/v1/`

- Authenticated `GET ai/status` → `enabled:boolean`
- Authenticated `POST ai/chat` → `answer:string`
- Body: `{question, conversation_id, history}` (last six turns)
- Existing mobile Bearer token is validated **server-side**. The native app
  cannot call the HMAC Telegram/Bale service, WordPress admin or OpenAI.

The matching customer-only gateway source is in
`AB-Softwares/ab-core` PR #21 (AB Smart Suite 0.8), under
`plugins/ab-smart-suite/includes/class-ab-smart-mobile-gateway.php`.
**Do not install an outdated AB Core 0.3.1 or the standalone AB Chat Bridge.**
The currently installed AB Smart Suite was v0.7.7 at last check.
Server compatibility with the actual IAMIR token issuer remains an
acceptance gate: filter `ab_smart_mobile_verified_user` must return an
authenticated WP_User; merely seeing a Bearer string is not authentication.

## Security and operational gates

1. Review/test native app compile with Android SDK/JDK 17, not just source.
2. Validate mobile token verifier for the installed IAMIR App API with a
   real test customer; unknown tokens, no token, revoked login, admin
   masquerading, invalid history and contact PII must fail.
3. Enable `AB_SMART_MOBILE_ENABLED` only after privacy/provider key approval.
4. Test published and hidden products, currency codes, price/stock live,
   multi-turn context, customer-only tool allowlist, 429 and timeout behavior.
5. Verify microphone/TTS fa-IR availability and explicit consent on device.
6. Build/sign APK/AAB with same applicationId, original signing certificate
   and monotonically increasing versionCode for in-place update. **No binary
   has been created here**; changing version code or signing keys blindly
   would break updates.
7. WooSuite manager assistant is separate, in its own Flutter repository.
   Staff permissions are never inferred from this customer app.

The manager app and bot AI activation are independent decisions. No existing
site, Render webhook, WooCommerce order, product or APK was changed.
