package com.gfycat.sdk.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.player.GfycatPlayer;

import java.util.Date;

public class GfycatViewActivity extends AppCompatActivity {
    private static final String EXTRA_GFYCAT = "EXTRA_GFYCAT";

    public static Intent createIntent(Context context, Gfycat gfycat) {
        Intent intent = new Intent(context, GfycatViewActivity.class);
        // Gfycat object is Parcelable out of the box
        intent.putExtra(EXTRA_GFYCAT, gfycat);
        return intent;
    }

    private GfycatPlayer playerView;
    private Gfycat gfycat;
    private View tapToPlayHint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gfycat_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        gfycat = (Gfycat) getIntent().getSerializableExtra(EXTRA_GFYCAT);
        if (gfycat == null) {
            Toast.makeText(this, "Incorrect Gfycat object", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tapToPlayHint = findViewById(R.id.tap_to_play);
        playerView = findViewById(R.id.player_view);
        playerView.setupGfycat(gfycat);
        playerView.getView().setOnClickListener(v -> {
            playerView.play();
            tapToPlayHint.setVisibility(View.INVISIBLE);
        });

        TextView title = findViewById(R.id.gfycat_title);
        TextView created = findViewById(R.id.gfycat_created);
        TextView widthHeight = findViewById(R.id.gfycat_width_height);
        TextView tags = findViewById(R.id.gfycat_tags);

        title.setText(gfycat.getGfyName());
        created.setText(new Date(Long.parseLong(gfycat.getCreateDate()) * 1000).toString());
        widthHeight.setText(String.format("%d x %d", gfycat.getWidth(), gfycat.getHeight()));
        tags.setText(gfycat.getTags().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
