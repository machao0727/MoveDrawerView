# MoveDrawerView
![](https://raw.githubusercontent.com/machao0727/XiaoAIView/master/simplegif/GIF.gif)
</br>
</br>
USE
====

```java

((XiaoAiView) findViewById(R.id.xiaoaiview)).setOnClickListener(new XiaoAiView.onClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(MainActivity.this,"点击事件",Toast.LENGTH_SHORT).show();
            }
        });

```

```xml
 <com.machao.xiaoaiview.XiaoAiView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:backgroundColor="@color/colorPrimary"
        app:during="3000"//动画持续时间
        app:icon="@mipmap/ic_xiaoai_main"//图标地址
        app:margin="20dp"//距离父布局边距
        app:padding="10dp"//内部边距
        app:text="测试文本"
        app:textSize="15sp"/>
```
PS

控件默认居中展示，结束动画后在右下角，如果适配自己的需求，可根据代码自己修改
