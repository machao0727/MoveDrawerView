package com.machao.xiaoaiview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((XiaoAiView) findViewById(R.id.xiaoaiview)).setOnClickListener(new XiaoAiView.onClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(MainActivity.this,"点击事件",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void start(View view){
        ((XiaoAiView)findViewById(R.id.xiaoaiview)).startAnimation();
    }
}
