package com.example.auth

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import com.example.settings.BrowserSettingsManager

object GoogleAutoLoginHelper {

    /**
     * Inject autofill assistant script on websites containing Google Sign-In or "Continue with Google"
     * This detects login buttons and prompts or fills the synced Google Account identifier.
     */
    fun injectAutoLoginScript(context: Context, webView: WebView, url: String) {
        if (!BrowserSettingsManager.isAutoLoginGoogleEnabled(context)) return

        val user = GoogleAuthManager.currentUser.value ?: return

        // Ensure cookies are flushed
        CookieManager.getInstance().flush()

        val safeEmail = user.email.replace("'", "\\'")
        val safeName = user.displayName.replace("'", "\\'")

        val js = """
            (function() {
                try {
                    // Check if current page has Google Sign-In / Continue with Google
                    const googleButtons = document.querySelectorAll(
                        '[data-provider="google"], button:has(svg), .g_id_signin, #g_id_onload, [aria-label*="Google"], [data-testid*="google"]'
                    );
                    
                    // Also check for email input fields on sign-in pages
                    const emailInputs = document.querySelectorAll(
                        'input[type="email"], input[name*="email" i], input[id*="email" i], input[autocomplete="email"], input[autocomplete="username"]'
                    );

                    emailInputs.forEach(input => {
                        if (!input.value) {
                            input.setAttribute('data-synced-google-account', '$safeEmail');
                            // If it's empty, suggest the synced account
                            input.placeholder = 'Google Account: $safeEmail';
                        }
                    });

                    console.log('Anup Web: Synced Google Account active ($safeEmail)');
                } catch (e) {
                    console.error('Anup Web auto-login injection:', e);
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(js, null)
        }
    }
}
