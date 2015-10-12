package com.apy.zhihu.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.apy.zhihu.R;
import com.apy.zhihu.constant.SPConstant;
import com.apy.zhihu.db.CacheDbHelper;
import com.apy.zhihu.fragment.MainFragment;
import com.apy.zhihu.fragment.MenuFragment;
import com.apy.zhihu.fragment.NewsFragment;

/**
 * Created by apy on 2015/9/27.
 */
public class MainActivity extends AppCompatActivity {
    //白天模式 or 黑夜模式 切换判断
    private boolean isLight;
    //fragment id
    private String curId;
    //缓存数据库
    private CacheDbHelper dbHelper;
    //代替ActionBar
    private Toolbar mToolbar;
    //Google官方下拉刷新控件
    private SwipeRefreshLayout mRefreshLayout;
    //侧拉页面
    private DrawerLayout mDrawlayout;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initToolbar();
        initRefresh();
        initMainFragment();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(SPConstant.SP_CONFIG, 0);
        dbHelper = new CacheDbHelper(this);
        isLight = sp.getBoolean(SPConstant.IS_LIGHT, true);
    }

    /*初始化ToolBar*/
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //夜间模式 or 白天模式,getResources().getColor()过时
        //23版本以上使用ContextCompat.getColor()获取本地资源（它俩一样）
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, isLight ? R.color.light_toolbar : R.color.dark_toolbar));
        setSupportActionBar(mToolbar);
        setStatusBarColor(ContextCompat.getColor(this, isLight ? R.color.light_toolbar : R.color.dark_toolbar));
    }

    /* 替换主界面framelayout*/
    private void initMainFragment() {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left).
                replace(R.id.main_content, new MainFragment(), "latest").
                commit();
        curId = "latest";
    }

    public void setCurId(String id) {
        curId = id;
    }

    public void replaceMainFragment() {
        if (curId.equals("latest")) {
            initMainFragment();
        }
    }

    /*
      初始化下拉刷新
    */
    private void initRefresh() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refershlayout);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                replaceMainFragment();
                mRefreshLayout.setRefreshing(false);
            }
        });
        initLayout();
    }

    private void initLayout() {
        mDrawlayout = (DrawerLayout) findViewById(R.id.drawlayout);
        //标题栏侧拉点击按钮
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawlayout, mToolbar, R.string.zhihu, R.string.zhihu);
        mDrawlayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    /*
    返回按钮处理
     */
    @Override
    public void onBackPressed() {
        if (mDrawlayout.isDrawerOpen(Gravity.LEFT)) {
            closeMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setTitle(sp.getBoolean(SPConstant.IS_LIGHT, true) ? getString(R.string.mode_drak) : getString(R.string.mode_light));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mode) {
            isLight = !isLight;
            item.setTitle(isLight ? getString(R.string.mode_drak) : getString(R.string.mode_light));
            mToolbar.setBackgroundColor(getResources().getColor(isLight ? R.color.light_toolbar : R.color.dark_toolbar));
            setStatusBarColor(getResources().getColor(isLight ? R.color.light_toolbar : R.color.dark_toolbar));
            if (curId.equals("latest")) {
                ((MainFragment) getSupportFragmentManager().findFragmentByTag("latest")).updateTheme();
            } else {
                ((NewsFragment) getSupportFragmentManager().findFragmentByTag("news")).updateTheme();
            }
            ((MenuFragment) getSupportFragmentManager().findFragmentById(R.id.menu_fragment)).updateTheme();
            sp.edit().putBoolean(SPConstant.IS_LIGHT, isLight).apply();
        }
        return super.onOptionsItemSelected(item);
    }

    public CacheDbHelper getCacheDbHelper() {
        return dbHelper;
    }

    public boolean isLight() {
        return isLight;
    }

    /**
     * 设置下拉刷新状态（是否刷新）
     *
     * @param enable
     */
    public void setRefreshEnable(boolean enable) {
        mRefreshLayout.setEnabled(enable);
    }

    public SwipeRefreshLayout setRefresh() {
        return mRefreshLayout;
    }

    /**
     * 设置Toolbar标题  @param title
     */
    public void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }

    /**
     * 关闭侧拉页面
     */
    public void closeMenu() {
        mDrawlayout.closeDrawers();
    }

    private void setStatusBarColor(int statusBarColor) {
        //Build.VERSION_CODES.LOLLIPOP  21版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO : 不懂
            // If both system bars are black, we can remove these from our layout,
            // removing or shrinking the SurfaceFlinger overlay required for our views.
            Window window = this.getWindow();
            if (statusBarColor == Color.BLACK && window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor);
        }
    }
}
