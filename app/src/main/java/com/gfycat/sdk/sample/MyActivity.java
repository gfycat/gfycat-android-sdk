package com.gfycat.sdk.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.picker.GfycatPickerFragment;

public class MyActivity extends AppCompatActivity {

    private static boolean SOUND_ONLY_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfycat_sdk_sample);
    }

    public boolean gfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position) {
        startActivity(GfycatWebpViewActivity.createIntent(this, gfycat));
        return false;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof GfycatPickerFragment) {
            if (SOUND_ONLY_MODE) {
                ((GfycatPickerFragment) fragment).setFeedSelectionResolver(new SoundOnlyFeedResolver());
            }
        }
    }
}
