package com.qodein.shared.model

/** Google ID token plus the raw nonce whose hash was sent to Google when requesting it. */
data class GoogleIdToken(val idToken: String, val nonce: String)
