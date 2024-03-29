package com.apy.zhihu.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apy.zhihu.R;
import com.apy.zhihu.mode.Latest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apy on 2015/9/27.
 */
public class Kanner extends FrameLayout implements View.OnClickListener{
    private List<Latest.TopStoriesEntity> topStoriesEntities;
    /** 加载图片工具类*/
    private ImageLoader mImageLoader;
    /** 加载图片工具类*/
    private DisplayImageOptions options;
    /** ViewPager页面集合*/
    private List<View> pagerViews;
    private Context context;
    private ViewPager vp;
    private boolean isAutoPlay;
    private int currentItem;
//    private int delayTime;
    /** 圆点父布局*/
    private LinearLayout ll_dot;
    /** 圆点图片集合*/
    private List<ImageView> iv_dots;
    private Handler handler = new Handler();
    private OnItemClickListener mItemClickListener;

    public Kanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mImageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        this.context = context;
        this.topStoriesEntities = new ArrayList<Latest.TopStoriesEntity>();
        initView();
    }

    private void initView() {
        pagerViews = new ArrayList<View>();
        iv_dots = new ArrayList<ImageView>();
//        delayTime = 2000;
    }

    public Kanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Kanner(Context context) {
        this(context, null);
    }

    public void  setTopEntities(List<Latest.TopStoriesEntity> topEntities) {
        this.topStoriesEntities = topEntities;
        reset();
    }

    private void reset() {
        pagerViews.clear();
        initUI();
    }

    private void initUI() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.kanner_layout, this, true);
        vp = (ViewPager) view.findViewById(R.id.vp);
        ll_dot = (LinearLayout) view.findViewById(R.id.ll_dot);
        ll_dot.removeAllViews();

        int len = topStoriesEntities.size();
        for (int i = 0; i < len; i++) {
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }

        for (int i = 0; i <= len + 1; i++) {
            View fm = LayoutInflater.from(context).inflate(
                    R.layout.kanner_content_layout, null);
            ImageView iv = (ImageView) fm.findViewById(R.id.iv_title);
            TextView tv_title = (TextView) fm.findViewById(R.id.tv_title);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            iv.setBackgroundResource(R.drawable.loading1);
            if (i == 0) {
                mImageLoader.displayImage(topStoriesEntities.get(len - 1).getImage(), iv, options);
                tv_title.setText(topStoriesEntities.get(len - 1).getTitle());
            } else if (i == len + 1) {
                mImageLoader.displayImage(topStoriesEntities.get(0).getImage(), iv, options);
                tv_title.setText(topStoriesEntities.get(0).getTitle());
            } else {
                mImageLoader.displayImage(topStoriesEntities.get(i - 1).getImage(), iv, options);
                tv_title.setText(topStoriesEntities.get(i - 1).getTitle());
            }
            fm.setOnClickListener(this);
            pagerViews.add(fm);
        }
        vp.setAdapter(new MyPagerAdapter());
        vp.setFocusable(true);
        vp.setCurrentItem(1);
        currentItem = 1;
        vp.addOnPageChangeListener(new MyOnPageChangeListener());
        startPlay();
    }

    private void startPlay() {
        isAutoPlay = true;
        handler.postDelayed(task, 3000);
    }
    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            if (isAutoPlay) {
                currentItem = currentItem % (topStoriesEntities.size() + 1) + 1;
                if (currentItem == 1) {
                    vp.setCurrentItem(currentItem, false);
                    handler.post(task);
                } else {
                    vp.setCurrentItem(currentItem);
                    handler.postDelayed(task, 5000);
                }
            } else {
                handler.postDelayed(task, 5000);
            }
        }
    };

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagerViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pagerViews.get(position));
            return pagerViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case 1:
                    isAutoPlay = false;
                    break;
                case 2:
                    isAutoPlay = true;
                    break;
                case 0:
                    if (vp.getCurrentItem() == 0) {
                        vp.setCurrentItem(topStoriesEntities.size(), false);
                    } else if (vp.getCurrentItem() == topStoriesEntities.size() + 1) {
                        vp.setCurrentItem(1, false);
                    }
                    currentItem = vp.getCurrentItem();
                    isAutoPlay = true;
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < iv_dots.size(); i++) {
                if (i == arg0 - 1) {
                    iv_dots.get(i).setImageResource(R.drawable.dot_focus);
                } else {
                    iv_dots.get(i).setImageResource(R.drawable.dot_blur);
                }
            }

        }

    }


    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        public void click(View v, Latest.TopStoriesEntity entity);
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            Latest.TopStoriesEntity entity = topStoriesEntities.get(vp.getCurrentItem() - 1);
            mItemClickListener.click(v, entity);
        }
    }
}
