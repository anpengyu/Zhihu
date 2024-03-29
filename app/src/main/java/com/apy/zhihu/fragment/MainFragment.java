package com.apy.zhihu.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.apy.zhihu.R;
import com.apy.zhihu.activity.LatestContentActivity;
import com.apy.zhihu.activity.MainActivity;
import com.apy.zhihu.adapter.MainNewsItemAdapter;
import com.apy.zhihu.constant.Constant;
import com.apy.zhihu.mode.Before;
import com.apy.zhihu.mode.Latest;
import com.apy.zhihu.mode.StoriesEntity;
import com.apy.zhihu.utils.HttpUtils;
import com.apy.zhihu.utils.PreUtils;
import com.apy.zhihu.view.Kanner;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.List;

/**
 * Created by wwjun.wang on 2015/8/12.
 */
public class MainFragment extends BaseFragment {
    private FloatingActionButton mFloatingActionButton;
    private ListView lv_news;
    private MainNewsItemAdapter mAdapter;
    private Latest latest;
    private Before before;
    private Kanner kanner;
    private String date;
    private boolean isLoading = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ((MainActivity) mActivity).replaceMainFragment();
                    ((MainActivity) mActivity).setRefresh().setRefreshing(false);
                    break;
            }
        }
    };

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) mActivity).setToolbarTitle("今日热闻");
        View view = inflater.inflate(R.layout.main_news_layout, container, false);
        initRefreshBar(view);
        lv_news = (ListView) view.findViewById(R.id.lv_news);
        View header = inflater.inflate(R.layout.kanner, lv_news, false);
        initKanner(header);
        lv_news.addHeaderView(header);
        mAdapter = new MainNewsItemAdapter(mActivity);
        lv_news.setAdapter(mAdapter);
        initListViewItem();
        return view;
    }

    private void initListViewItem() {
        lv_news.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lv_news != null && lv_news.getChildCount() > 0) {
                    boolean enable = (firstVisibleItem == 0) && (view.getChildAt(firstVisibleItem).getTop() == 0);
                    ((MainActivity) mActivity).setRefreshEnable(enable);

                    if (firstVisibleItem + visibleItemCount == totalItemCount && !isLoading) {
                        loadMore(Constant.BEFORE + date);
                    }
                }

            }
        });
        lv_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int[] startingLocation = new int[2];
                view.getLocationOnScreen(startingLocation);
                startingLocation[0] += view.getWidth() / 2;
                StoriesEntity entity = (StoriesEntity) parent.getAdapter().getItem(position);
                Intent intent = new Intent(mActivity, LatestContentActivity.class);
                intent.putExtra(Constant.START_LOCATION, startingLocation);
                intent.putExtra("entity", entity);
                intent.putExtra("isLight", ((MainActivity) mActivity).isLight());
                String readSequence = PreUtils.getStringFromDefault(mActivity, "read", "");
                String[] splits = readSequence.split(",");
                StringBuffer sb = new StringBuffer();
                if (splits.length >= 200) {
                    for (int i = 100; i < splits.length; i++) {
                        sb.append(splits[i] + ",");
                    }
                    readSequence = sb.toString();
                }

                if (!readSequence.contains(entity.getId() + "")) {
                    readSequence = readSequence + entity.getId() + ",";
                }
                PreUtils.putStringToDefault(mActivity, "read", readSequence);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                tv_title.setTextColor(getResources().getColor(R.color.clicked_tv_textcolor));
                startActivity(intent);
//                mActivity.overridePendingTransition(0, 0);
            }
        });
    }

    private void initKanner(View header) {
        kanner = (Kanner) header.findViewById(R.id.kanner);
        kanner.setOnItemClickListener(new Kanner.OnItemClickListener() {
            @Override
            public void click(View v, Latest.TopStoriesEntity entity) {
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                startingLocation[0] += v.getWidth() / 2;
                StoriesEntity storiesEntity = new StoriesEntity();
                storiesEntity.setId(entity.getId());
                storiesEntity.setTitle(entity.getTitle());
                Intent intent = new Intent(mActivity, LatestContentActivity.class);
                intent.putExtra(Constant.START_LOCATION, startingLocation);
                intent.putExtra("entity", storiesEntity);
                intent.putExtra("isLight", ((MainActivity) mActivity).isLight());
                startActivity(intent);
                mActivity.overridePendingTransition(0, 0);
            }
        });
    }

    private void initRefreshBar(View view) {
        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.refreshbutton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mActivity).setRefresh().setRefreshing(true);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Message msg = Message.obtain();
                            msg.what = 1;
                            handler.sendMessageDelayed(msg, 2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    private void loadFirst() {
        isLoading = true;
            if (HttpUtils.isNetworkConnected(mActivity)) {
                HttpUtils.get(Constant.LATESTNEWS, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int i, org.apache.http.Header[] headers, String s, Throwable throwable) {
//                    Log.i("fail","111");
                    }
                    @Override
                    public void onSuccess(int i, org.apache.http.Header[] headers, String s) {
                        SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getWritableDatabase();
                        db.execSQL("replace into CacheList(date,json) values(" + Constant.LATEST_COLUMN + ",' " + s + "')");
                        db.close();
                        parseLatestJson(s);
                    }
                });
            } else {
                SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from CacheList where date = " + Constant.LATEST_COLUMN, null);
                if (cursor.moveToFirst()) {
                    String json = cursor.getString(cursor.getColumnIndex("json"));
                    parseLatestJson(json);
                } else {
                    isLoading = false;
                }
                cursor.close();
                db.close();
            }
            SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from CacheList where date = " + Constant.LATEST_COLUMN, null);
            if (cursor.moveToFirst()) {
                String json = cursor.getString(cursor.getColumnIndex("json"));
                parseLatestJson(json);
            } else {
                isLoading = false;
            }
            cursor.close();
            db.close();
    }

    private void parseLatestJson(String responseString) {
        Gson gson = new Gson();
        latest = gson.fromJson(responseString, Latest.class);
        date = latest.getDate();
        kanner.setTopEntities(latest.getTop_stories());
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<StoriesEntity> storiesEntities = latest.getStories();
                StoriesEntity topic = new StoriesEntity();
                topic.setType(Constant.TOPIC);
                topic.setTitle("今日热闻");
                storiesEntities.add(0, topic);
                mAdapter.addList(storiesEntities);
                isLoading = false;
            }
        });
    }

    private void loadMore(final String url) {
        isLoading = true;
        if (HttpUtils.isNetworkConnected(mActivity)) {
            HttpUtils.get(url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                    SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getReadableDatabase();
                    Cursor cursor = db.rawQuery("select * from CacheList where date = " + date, null);
                    if (cursor.moveToFirst()) {
                        String json = cursor.getString(cursor.getColumnIndex("json"));
                        parseBeforeJson(json);
                    } else {
                        db.delete("CacheList", "date < " + date, null);
                        isLoading = false;
                        Snackbar sb = Snackbar.make(lv_news, "没有更多的离线内容了~", Snackbar.LENGTH_SHORT);
                        sb.getView().setBackgroundColor(getResources().getColor(((MainActivity) mActivity).isLight() ? android.R.color.holo_blue_dark : android.R.color.black));
                        sb.show();
                    }
                    cursor.close();
                    db.close();
                }

                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, String responseString) {
//                    PreUtils.putStringTo(Constant.CACHE, mActivity, url, responseString);
                    SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getWritableDatabase();
                    db.execSQL("replace into CacheList(date,json) values(" + date + ",' " + responseString + "')");
                    db.close();
                    parseBeforeJson(responseString);

                }

            });
        } else {
            SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from CacheList where date = " + date, null);
            if (cursor.moveToFirst()) {
                String json = cursor.getString(cursor.getColumnIndex("json"));
                parseBeforeJson(json);
            } else {
                db.delete("CacheList", "date < " + date, null);
                isLoading = false;
                Snackbar sb = Snackbar.make(lv_news, "没有更多的离线内容了~", Snackbar.LENGTH_SHORT);
                sb.getView().setBackgroundColor(getResources().getColor(((MainActivity) mActivity).isLight() ? android.R.color.holo_blue_dark : android.R.color.black));
                sb.show();
            }
            cursor.close();
            db.close();

        }
    }

    private void parseBeforeJson(String responseString) {
        Gson gson = new Gson();
        before = gson.fromJson(responseString, Before.class);
        if (before == null) {
            isLoading = false;
            return;
        }
        date = before.getDate();
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<StoriesEntity> storiesEntities = before.getStories();
                StoriesEntity topic = new StoriesEntity();
                topic.setType(Constant.TOPIC);
                topic.setTitle(convertDate(date));
                storiesEntities.add(0, topic);
                mAdapter.addList(storiesEntities);
                isLoading = false;
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        loadFirst();
    }

    private String convertDate(String date) {
        String result = date.substring(0, 4);
        result += "年";
        result += date.substring(4, 6);
        result += "月";
        result += date.substring(6, 8);
        result += "日";
        return result;
    }

    public void updateTheme() {
        mAdapter.updateTheme();
    }
}
