package com.posim.dclub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** D Club: üyelik ve kart sayfasını uygulama olarak açar. */
public class MainActivity extends Activity {
    // İşletme adresin sonundaki ?i= ile seçilir
    private static final String ADRES = "https://iyemez1987-cmd.github.io/dclub/?i=zztest-dara";
    private static final String ALAN = "iyemez1987-cmd.github.io";
    private static final String SURUM = "1.1";
    private WebView web;
    private long arkaPlanaGecis = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#1B0A0D"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " DClubApp/" + SURUM);

        web.setWebChromeClient(new WebChromeClient()); // onay pencereleri (confirm) için
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                // APK güncellemesi ve dış bağlantılar tarayıcıda açılır
                if (ALAN.equals(u.getHost()) && !u.getPath().endsWith(".apk")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { /* yoksay */ }
                return true;
            }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) {
                    v.loadDataWithBaseURL(null,
                        "<html><body style='background:#1B0A0D;color:#F6EBDD;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;height:90vh;text-align:center;padding:24px'>"
                        + "<h2 style='color:#D8B57A'>İnternet bağlantısı yok</h2><p>Bağlantı gelince tekrar dene.</p>"
                        + "<button onclick=\"location.href='" + ADRES + "'\" style='margin-top:16px;padding:14px 28px;border:0;border-radius:14px;background:#D8B57A;font-size:16px'>Tekrar dene</button></body></html>",
                        "text/html", "utf-8", null);
                }
            }
        });

        web.setDownloadListener((url, ua, cd, mime, len) -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { /* yoksay */ }
        });

        if (b != null) web.restoreState(b); else web.loadUrl(ADRES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // QR kasada kolay okunsun diye ekran açıkken kapanmasın
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 5 dakikadan uzun arka planda kaldıysa sayfayı tazele (yeni sürüm ve güncel kampanyalar)
        if (arkaPlanaGecis > 0 && System.currentTimeMillis() - arkaPlanaGecis > 5 * 60 * 1000) web.reload();
        arkaPlanaGecis = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        arkaPlanaGecis = System.currentTimeMillis();
    }

    @Override
    protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
