package com.kenhong.slotgame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;



public class GamePanelView extends FrameLayout {

    private ImageView[] ivArr = new ImageView[24];

    Context mContext;

    SoundPool mSoundPool;
    private int mSoundId;

    private boolean isMarqueeRunning = false;
    private boolean isGameRunning = false;
    private boolean isTryToStop = false;
    private int currentIndex = 0;
    private int currentTotal = 0;
    private int stayIndex = 0;

    private static final int DEFAULT_SPEED = 150;
    private static final int MIN_SPEED = 50;
    private static final int ALPHA_FOCUS = 255;
    private static final int ALPHA_NO_FOCUS = 150;
    private int currentSpeed = DEFAULT_SPEED;

    public GamePanelView(@NonNull Context context) {
        this(context, null);
    }

    public GamePanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GamePanelView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.game_panel_view, this);
        setupView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    private void setupView(){
        for(int i = 0; i < 24; i++){
            ivArr[i] = findViewById(R.id.image01 + i);
            ivArr[i].setImageAlpha(ALPHA_NO_FOCUS);
        }
    }

    public void startGame(){
        isGameRunning = true;
        isTryToStop = false;
        currentSpeed = DEFAULT_SPEED;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGameRunning) {
                    try {
                        Thread.sleep(getInterruptTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    post(new Runnable() {
                        @Override
                        public void run() {
                            int preIndex = currentIndex;
                            currentIndex++;
                            if (currentIndex >= ivArr.length) {
                                currentIndex = 0;
                            }

                            //ivArr[preIndex].setFocus(false);
                            //ivArr[currentIndex].setFocus(true);
                            setFocus(ivArr[preIndex], false);
                            setFocus(ivArr[currentIndex], true);

                            if (isTryToStop && currentSpeed == DEFAULT_SPEED && stayIndex == currentIndex) {
                                isGameRunning = false;
                            }
                        }
                    });
                }
            }
        }).start();

    }

    private void setFocus(ImageView iv, boolean isFocused){

        mSoundPool.play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        iv.setImageAlpha(isFocused ? ALPHA_FOCUS : ALPHA_NO_FOCUS);
    }

    private long getInterruptTime() {
        currentTotal++;
        if (isTryToStop) {
            currentSpeed += 10;
            if (currentSpeed > DEFAULT_SPEED) {
                currentSpeed = DEFAULT_SPEED;
            }
        } else {
            if (currentTotal / ivArr.length > 0) {
                currentSpeed -= 10;
            }
            if (currentSpeed < MIN_SPEED) {
                currentSpeed = MIN_SPEED;
            }
        }
        return currentSpeed;
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void tryToStop(int position) {
        stayIndex = position;
        isTryToStop = true;
    }

    public void init(Context context){
        mContext = context;
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build())
                .build();

        mSoundId = mSoundPool.load(context, R.raw.di, 1);
    }
}
