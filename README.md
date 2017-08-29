# FloatViewDmo
自定义悬浮按钮
首先看效果图，截取太粗糙，严重掉帧还望见谅<br>
![image](https://github.com/18337129968/FloatViewDmo/blob/master/photo/img.gif)<br>
用到了震动所以需要震动权限，该权限已经添加到manifest但是6.0以上需要自己手动添加
```
    <uses-permission android:name="android.permission.VIBRATE" />
```
##使用方法
在layout布局文件根布局
```
<?xml version="1.0" encoding="utf-8"?>
<com.isoftstone.floatlibrary.widget.FloatViewLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout_float"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</com.isoftstone.floatlibrary.widget.FloatViewLayout>
```
其次需要悬浮按钮布局，之所以写成布局形式，是因为拓展新良好，需要任何样式都可以实现
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <ImageView
        android:layout_width="@dimen/floating_icon_size"
        android:layout_height="@dimen/floating_icon_size"
        android:src="@mipmap/float_btn" />
</LinearLayout>
```
Activity中只需要调用相关方法即可
```
public class MainActivity extends AppCompatActivity {
    private FloatViewLayout floatViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatViewLayout = (FloatViewLayout) findViewById(R.id.layout_float);
        floatViewLayout.setmFloatView(new FloatViewImpl() {
            @Override
            public View createFloatView() {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_float_view, null);
                return view;
            }

            @Override
            public int setFloatViewSideOffset() {
                return super.setFloatViewSideOffset();
            }

            @Override
            public boolean setEnableBackground() {
                return false;
            }
        });
    }
}
```
FloatViewImpl是一个静态类已经实现了setFloatViewSideOffset()和setEnableBackground()方法并且给了默认值，可自行修改。<br>
createFloatView()方法：主要是用来创建悬浮按钮View<br>
setFloatViewSideOffset()方法：是用来设置悬浮按钮与边缘偏移量主要是用于缩进一定像素<br>
setEnableBackground()方法：是用来设置是否保留滑动出现背景色并实现消失悬浮按钮<br>
