package com.apy.zhihu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apy.zhihu.R;
import com.apy.zhihu.activity.MainActivity;
import com.apy.zhihu.constant.Constant;
import com.apy.zhihu.mode.NewsListItem;
import com.apy.zhihu.utils.ConnectUtil;
import com.apy.zhihu.utils.PreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends BaseFragment implements View.OnClickListener {
    private ListView lv_item;
    private TextView tv_download, tv_main, tv_backup, tv_login;
    private LinearLayout ll_menu;
    // private static String[] ITEMS = { "日常心理学", "用户推荐日报", "电影日报", "不许无聊",
    // "设计日报", "大公司日报", "财经日报", "互联网安全", "开始游戏", "音乐日报", "动漫日报", "体育日报" };
    private List<NewsListItem> items;
    private boolean isLight;
    private NewsTypeAdapter mAdapter;

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu, container, false);
        ll_menu = (LinearLayout) view.findViewById(R.id.ll_menu);
        tv_login = (TextView) view.findViewById(R.id.tv_login);
        tv_backup = (TextView) view.findViewById(R.id.tv_backup);
        tv_download = (TextView) view.findViewById(R.id.tv_download);
        tv_download.setOnClickListener(this);
        tv_main = (TextView) view.findViewById(R.id.tv_main);
        tv_main.setOnClickListener(this);
        lv_item = (ListView) view.findViewById(R.id.lv_item);
        lv_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                getFragmentManager()
                        .beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        .replace(
                                R.id.main_content,
                                new NewsFragment(items.get(position).getId(), items.get(position).getTitle()), "news").commit();
                ((MainActivity) mActivity).setCurId(items.get(position).getId());
                ((MainActivity) mActivity).closeMenu();
            }
        });
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        isLight = ((MainActivity) mActivity).isLight();
        items = new ArrayList<NewsListItem>();
        if (ConnectUtil.isNetworkConnected(mActivity)) {
            final RequestQueue queue = Volley.newRequestQueue(getContext());
            StringRequest request = new StringRequest(Constant.BASEURL + Constant.LATESTNEWS, new Response.Listener<String>() {

                @Override
                public void onResponse(String s) {
                    PreUtils.putStringToDefault(mActivity, Constant.THEMES, s);
                    try {
                        JSONObject obj = new JSONObject(s);
                        parseJson(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    String json = PreUtils.getStringFromDefault(mActivity, Constant.BASEURL + Constant.THEMES, "");
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        parseJson(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            queue.add(request);
        } else {
            String json = PreUtils.getStringFromDefault(mActivity, Constant.THEMES, "");
            try {
                JSONObject jsonObject = new JSONObject(json);
                parseJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private void parseJson(JSONObject response) {
        try {
            JSONArray itemsArray = response.getJSONArray("others");
            for (int i = 0; i < itemsArray.length(); i++) {
                NewsListItem newsListItem = new NewsListItem();
                JSONObject itemObject = itemsArray.getJSONObject(i);
                newsListItem.setTitle(itemObject.getString("name"));
                newsListItem.setId(itemObject.getString("id"));
                items.add(newsListItem);
            }
            mAdapter = new NewsTypeAdapter();
            lv_item.setAdapter(mAdapter);
            updateTheme();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class NewsTypeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.menu_item, parent, false);
            }
            TextView tv_item = (TextView) convertView
                    .findViewById(R.id.tv_item);
            tv_item.setTextColor(getResources().getColor(isLight ? R.color.light_menu_listview_textcolor : R.color.dark_menu_listview_textcolor));
            tv_item.setText(items.get(position).getTitle());
            return convertView;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_main:
                ((MainActivity) mActivity).replaceMainFragment();
                ((MainActivity) mActivity).closeMenu();
                break;
        }
    }

    public void updateTheme() {
        isLight = ((MainActivity) mActivity).isLight();
        ll_menu.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_header : R.color.dark_menu_header));
        tv_login.setTextColor(getResources().getColor(isLight ? R.color.light_menu_header_tv : R.color.dark_menu_header_tv));
        tv_backup.setTextColor(getResources().getColor(isLight ? R.color.light_menu_header_tv : R.color.dark_menu_header_tv));
        tv_download.setTextColor(getResources().getColor(isLight ? R.color.light_menu_header_tv : R.color.dark_menu_header_tv));
        tv_main.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_index_background : R.color.dark_menu_index_background));
        lv_item.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_listview_background : R.color.dark_menu_listview_background));
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }

    }
}
