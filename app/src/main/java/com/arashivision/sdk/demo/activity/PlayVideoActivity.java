package com.arashivision.sdk.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import com.arashivision.sdk.demo.MyApp;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.util.TimeFormat;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.player.image.ImageParamsBuilder;
import com.arashivision.sdkmedia.player.image.InstaImagePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.work.WorkUtils;
import com.arashivision.sdkmedia.work.WorkWrapper;

import java.util.List;


public class PlayVideoActivity extends BaseObserveCameraActivity implements IExportCallback {

    private static final String TAG = "点位";
    private static final String WORK_URLS = "CAMERA_FILE_PATH";

    private InstaImagePlayerView mImagePlayerView;
    private InstaVideoPlayerView mVideoPlayerView;
    private RadioButton mRbFisheye;
    private RadioButton mRbPerspective;
    private Group mGroupProgress;
    private TextView mTvCurrent;
    private TextView mTvTotal;
    private SeekBar mSeekBar;

    private WorkWrapper mWorkWrapper;


    public static void launchActivity(Context context, String[] urls) {
        Intent intent = new Intent(context, PlayVideoActivity.class);
        intent.putExtra(WORK_URLS, urls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        setTitle(R.string.play_toolbar_title);

        //String[] urls = getIntent().getStringArrayExtra(WORK_URLS);
        String[] urls = {/*"http://qibaitongweb.host5.liuniukeji.net/264.mp4"*/"/storage/emulated/0/Pictures/360demo_video.mp4"};
        Log.i(TAG,"urls:  ---  " + urls);
        Log.i(TAG,"urls.toString:  ---  " + urls.toString());
        if (urls == null) {
            finish();
            Toast.makeText(this, R.string.play_toast_empty_path, Toast.LENGTH_SHORT).show();
            return;
        }

        mWorkWrapper = new WorkWrapper(urls);
        bindViews();
        Log.i(TAG,"是否视频：  " + mWorkWrapper.isVideo());
        if (mWorkWrapper.isVideo()) {
            playVideo(false);
        } else {
            Log.i(TAG,"不处理图片");
        }


        /*List<WorkWrapper> list = WorkUtils.getAllLocalWorks("/storage/emulated/0/Pictures/360demo_video.mp4");
        Log.i(TAG,"list:  ---  " + list);*/

    }

    private void bindViews() {
        mVideoPlayerView = findViewById(R.id.player_video);
        mImagePlayerView = findViewById(R.id.player_image);
        mGroupProgress = findViewById(R.id.group_progress);
        mTvCurrent = findViewById(R.id.tv_current);
        mTvTotal = findViewById(R.id.tv_total);
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.seekTo(seekBar.getProgress());
            }
        });

        mRbFisheye = findViewById(R.id.rb_fisheye);
        mRbPerspective = findViewById(R.id.rb_perspective);
        RadioGroup radioGroup = findViewById(R.id.rg_image_mode);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Need to restart the preview stream when switching between normal and plane
            if (checkedId == R.id.rb_plane) {
                if (mWorkWrapper.isVideo()) {
                    playVideo(true);
                } else {
                    //playImage(true);
                }
                mRbFisheye.setEnabled(false);
                mRbPerspective.setEnabled(false);
            } else if (checkedId == R.id.rb_normal) {
                if (!mRbFisheye.isEnabled() || !mRbPerspective.isEnabled()) {
                    if (mWorkWrapper.isVideo()) {
                        playVideo(false);
                    } else {
                        //playImage(false);
                    }
                    mRbFisheye.setEnabled(true);
                    mRbPerspective.setEnabled(true);
                } else {
                    // Switch to Normal Mode
                    mImagePlayerView.switchNormalMode();
                    mVideoPlayerView.switchNormalMode();
                }
            } else if (checkedId == R.id.rb_fisheye) {
                // Switch to Fisheye Mode
                mImagePlayerView.switchFisheyeMode();
                mVideoPlayerView.switchFisheyeMode();
            } else if (checkedId == R.id.rb_perspective) {
                // Switch to Perspective Mode
                mImagePlayerView.switchPerspectiveMode();
                mVideoPlayerView.switchPerspectiveMode();
            }
        });
    }

    private void playVideo(boolean isPlaneMode) {
        mGroupProgress.setVisibility(View.VISIBLE);
        mVideoPlayerView.setVisibility(View.VISIBLE);
        mVideoPlayerView.setLifecycle(getLifecycle());
        mVideoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(PlayVideoActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(PlayVideoActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        mVideoPlayerView.setVideoStatusListener(new VideoStatusListener() {
            @Override
            public void onProgressChanged(long position, long length) {
                mSeekBar.setMax((int) length);
                mSeekBar.setProgress((int) position);
                mTvCurrent.setText(TimeFormat.durationFormat(position));
                mTvTotal.setText(TimeFormat.durationFormat(length));
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
            }

            @Override
            public void onSeekComplete() {
                mVideoPlayerView.resume();
            }

            @Override
            public void onCompletion() {
            }
        });
        mVideoPlayerView.setGestureListener(new PlayerGestureListener() {
            @Override
            public boolean onTap(MotionEvent e) {
                if (mVideoPlayerView.isPlaying()) {
                    mVideoPlayerView.pause();
                } else if (!mVideoPlayerView.isLoading() && !mVideoPlayerView.isSeeking()) {
                    mVideoPlayerView.resume();
                }
                return false;
            }
        });
        VideoParamsBuilder builder = new VideoParamsBuilder();
        builder.setWithSwitchingAnimation(true);
        if (isPlaneMode) {
            builder.setRenderModelType(VideoParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }
        mVideoPlayerView.prepare(mWorkWrapper, builder);
        mVideoPlayerView.play();
    }


    @Override
    public void onSuccess() {
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        // 如果 GPU 不支持，errorCode 为 -10003 或 -10005 或 -13020
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onProgress(float progress) {
        // 仅在导出视频时有进度回调
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagePlayerView != null) {
            mImagePlayerView.destroy();
        }
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }
    }

}
