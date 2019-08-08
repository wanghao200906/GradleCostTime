package com.example.gradlecosttime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.annotation.Cost;
import com.example.common.TimeCost;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        method();
    }

    @Cost
    public void method() {
        try {
            Thread.sleep(1000);
            System.out.println("睡了1s");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
