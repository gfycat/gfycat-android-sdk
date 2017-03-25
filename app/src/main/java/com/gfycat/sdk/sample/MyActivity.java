package com.gfycat.sdk.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfycat_sdk_sample);
    }

    public boolean gfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position) {
        startActivity(GfycatWebpViewActivity.createIntent(this, gfycat));
        return false;
    }
}
