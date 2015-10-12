package com.apy.zhihu.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apy.zhihu.constant.Constant;
import com.apy.zhihu.constant.SPConstant;
import com.apy.zhihu.utils.BitmapUtils;
import com.apy.zhihu.utils.ConnectUtil;
import com.apy.zhihu.utils.SplashImageUtil;
import com.apy.zhihu.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private ImageView iv_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        iv_start = (ImageView) findViewById(R.id.iv_start);
        initImage();
    }
    /**
     * 图片动画
     */
    public void initImage() {
//        Bitmap bitmap = readImage(context);
//        if (bitmap != null) {
//            iv_start.setImageBitmap(bitmap);
//        } else {
////            iv_start.setImageResource(R.mipmap.start);
//            iv_start.setImageBitmap(readBitMap(context, R.mipmap.start));
//        }
        File dir = getFilesDir();
        final File imgFile = new File(dir, "start.jpg");
        if (imgFile.exists()) {
            iv_start.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        } else {
            iv_start.setImageResource(R.mipmap.start);
        }
        /**
         * float fromX 动画起始时 X坐标上的伸缩尺寸
         float toX 动画结束时 X坐标上的伸缩尺寸
         float fromY 动画起始时Y坐标上的伸缩尺寸
         float toY 动画结束时Y坐标上的伸缩尺寸
         int pivotXType 动画在X轴相对于物件位置类型
         float pivotXValue 动画相对于物件的X坐标的开始位置
         int pivotYType 动画在Y轴相对于物件位置类型
         float pivotYValue 动画相对于物件的Y坐标的开始位置
         */
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        animation.setDuration(3000);//设置动画持续时间
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /**
                 * 这里拿到的RequestQueue是一个请求队列对象，它可以缓存所有的HTTP请求，然后按照一定的算法并发地发出这些请求。RequestQueue内部的设计就是非常合适高并发的，因此我们不必为每一次HTTP请求都创建一个RequestQueue对象，这是非常浪费资源的，基本上在每一个需要和网络交互的Activity中创建一个RequestQueue对象就足够了。
                 */

                if (ConnectUtil.isNetworkConnected(SplashActivity.this)) {
                    final RequestQueue queue = Volley.newRequestQueue(SplashActivity.this);
                    StringRequest request = new StringRequest(Constant.BASEURL + Constant.START, new Response.Listener<String>() {
                        ImageRequest request1;

                        @Override
                        public void onResponse(String s) {
                            try {
                                JSONObject obj = new JSONObject(s);
                                String img = obj.getString("img");
                                /***
                                 * 第三第四个参数分别用于指定允许图片最大的宽度和高度，如果指定的网络图片的宽度或高度大于这里的最大值，则会对图片进行压缩，指定成0的话就表示不管图片有多大，都不会进行压缩。第五个参数用于指定图片的颜色属性，Bitmap.Config下的几个常量都可以在这里使用，其中ARGB_8888可以展示最好的颜色属性，每个图片像素占据4个字节的大小，而RGB_565则表示每个图片像素占据2个字节大小
                                 */
                                request1 = new ImageRequest(img, new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        saveImage(imgFile, Bitmap2Bytes(bitmap));
                                        ;
//                                        saveImage(bitmap, context);
                                        startActivity(SplashActivity.this);
                                    }
                                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        startActivity(SplashActivity.this);
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            queue.add(request1);
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.i("SplashActivity", volleyError.getMessage(), volleyError);
                        }
                    });
                    queue.add(request);
                } else {
                    Toast.makeText(SplashActivity.this, "没有连接网络", Toast.LENGTH_SHORT).show();
                    startActivity(SplashActivity.this);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_start.startAnimation(animation);
    }

    private void startActivity(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        context.finish();

    }

    private void saveImage(Bitmap bitmap, Context context) {
        SharedPreferences sp = context.getSharedPreferences(SPConstant.SP_CONFIG,0);
        sp.edit().putString(SPConstant.SPLASH_LOAD_IMG, BitmapUtils.BitmapToString(bitmap)).commit();
    }

    private Bitmap readImage(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SPConstant.SP_CONFIG,0);
        String str = sp.getString(SPConstant.SPLASH_LOAD_IMG, "");
        if (!str.equals("")) {
            return BitmapUtils.StringToBitmap(str);
        } else {
            return null;
        }
    }

    /**
     * @param context
     * @param resId
     * @return
     */


    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
//获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public void saveImage(File file, byte[] bytes) {
        try {
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
