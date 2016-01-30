package cn.chenyuanming.gankmeizhi.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.TreeSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.chenyuanming.gankmeizhi.R;
import cn.chenyuanming.gankmeizhi.beans.FavoriteBean;
import cn.chenyuanming.gankmeizhi.utils.DbHelper;
import cn.chenyuanming.gankmeizhi.utils.ShareUtils;

/**
 * Created by Administrator on 2016/1/28.
 */
public class WebViewActivity extends AppCompatActivity {
    @Bind(R.id.loadingFrame)
    FrameLayout loadingFrame;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.webView)
    WebView webView;
//    @Bind(R.id.progressBar)
//    ProgressBar progressBar;

    @Bind(R.id.swipeRefreshLayout)
    SwipyRefreshLayout swipeRefreshLayout;
    String objectId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        setupSwipeRefreshLayout();
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        initWebView(webView);

        String url = getIntent().getStringExtra("url");
        objectId = getIntent().getStringExtra("objectId");
//        if(!url.contains("github.com")){
//            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        }
        webView.loadUrl(url);
    }

    private void setupSwipeRefreshLayout() {
        //设置卷内的颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefreshLayout.setDirection(SwipyRefreshLayoutDirection.TOP);
        //设置下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener((direction) -> {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.reload();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    FavoriteBean favorite = DbHelper.getHelper().getData(FavoriteBean.class).get(0);

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBack();
                return true;
            case R.id.action_share:
                //TODO
                ShareUtils.share(this, "");
                return true;
            case R.id.action_favorite:
                onFavoriteClicked((ImageView) item.getActionView(), favorite.favorites, objectId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
//                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    loadingFrame.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(newProgress != 100);
            }

        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setHomeIndicator(view);
            }
        });
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        String cacheDirPath = getFilesDir().getAbsolutePath() + "/webviewcache";
        webView.getSettings().setDatabasePath(cacheDirPath);
        webView.getSettings().setAppCachePath(cacheDirPath);
        webView.getSettings().setAppCacheEnabled(true);

        webView.requestFocus();
        webView.getSettings().getAllowFileAccess();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
        }
        return true;
    }

    private void onBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            setHomeIndicator(webView);
        } else {
            finish();
        }
    }

    private void setHomeIndicator(WebView webView) {
        if (webView.canGoBack()) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }
    }

    private void onFavoriteClicked(ImageView ivFavorite, TreeSet<String> favorites, String objectId) {
        if (favorites.contains(objectId)) {
            favorites.remove(objectId);
        } else {
            favorites.add(objectId);
        }
//        changeFavoriteIcon(ivFavorite, favorites, objectId);
    }

    private void changeFavoriteIcon(ImageView ivFavorite, TreeSet<String> favorites, String objectId) {
        Drawable drawable = ivFavorite.getDrawable();
        if (favorites.contains(objectId)) {
            drawable.setColorFilter(Color.parseColor("#ff0000"), PorterDuff.Mode.SRC_IN);
            ivFavorite.setImageDrawable(drawable);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite);
        }
    }

}
