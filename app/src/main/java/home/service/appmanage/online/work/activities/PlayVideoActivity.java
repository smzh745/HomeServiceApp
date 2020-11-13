package home.service.appmanage.online.work.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import home.service.appmanage.online.work.R;

public class PlayVideoActivity extends YouTubeFailureRecoveryActivity implements YouTubePlayer.OnFullscreenListener {
    private YouTubePlayerView playerView;
    private boolean fullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        try {
            playerView = findViewById(R.id.player);
            try {
                playerView.initialize(getString(R.string.youtube_key), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        try {
            LinearLayout.LayoutParams playerParams =
                    (LinearLayout.LayoutParams) playerView.getLayoutParams();
            if (fullscreen) {
                // When in fullscreen, the visibility of all other views than the player should be set to
                playerParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                playerParams.height = LinearLayout.LayoutParams.MATCH_PARENT;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return playerView;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        // Specify that we want to handle fullscreen behavior ourselves.
        try {
            youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
            youTubePlayer.setOnFullscreenListener(this);
            if (!b) {
                youTubePlayer.loadVideo(getIntent().getStringExtra("videoUrl"));
            }
            youTubePlayer.setFullscreen(!fullscreen);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFullscreen(boolean b) {
        try {
            fullscreen = b;
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
