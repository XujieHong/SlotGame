package com.kenhong.slotgame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.os.Handler;
import android.widget.TextView;


public class GamePanelView extends FrameLayout {

    private static final int ROLLING_STOP = 1;

    private ImageView[] ivArr = new ImageView[24];

    private int[] mStake = new int[8];

    private Context mContext;
    private Handler mHandler;

    private SoundPool mSoundPool;
    private int mRollingSoundId;
    private int mStopSoundId;
    private boolean mIsMute = false;

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
        mIsMute = false;
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
                                //mSoundPool.play(mStopSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                                Message message = new Message();
                                message.what = ROLLING_STOP;
                                mHandler.sendMessage(message);
                            }
                        }
                    });
                }
            }
        }).start();

    }

    private void setFocus(ImageView iv, boolean isFocused){
        if(!mIsMute){
            mSoundPool.play(mRollingSoundId, 1.0f, 1.0f, 0, 0, 2.0f);
        }

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

    public void init(Context context, Handler handler){
        mContext = context;
        mHandler = handler;
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build())
                .build();

        mRollingSoundId = mSoundPool.load(context, R.raw.doo, 1);
        mStopSoundId = mSoundPool.load(context, R.raw.didong, 1);
        for(int i = 0; i < 8; i++){
            mStake[i] = 0;
        }
    }

    private void endRolling(){
        mSoundPool.play(mStopSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        mIsMute = true;

        for(int i = 0; i < 8; i++){
            mStake[i] = 0;
            TextView tv = findViewById(R.id.text_bet01 + i);
            tv.setText("0");
        }
    }

    public void processMessage(int msg){
        switch (msg){
            case ROLLING_STOP:
                endRolling();
                break;
            default:
                break;
        }
    }

    public void betOn(int id){
        TextView tv = null;
        int index = -1;

        switch (id){
            case R.id.image_bet01:
                index = 0;
                tv = findViewById(R.id.text_bet01);
                break;
            case R.id.image_bet02:
                index = 1;
                tv = findViewById(R.id.text_bet02);
                break;
            case R.id.image_bet03:
                index = 2;
                tv = findViewById(R.id.text_bet03);
                break;
            case R.id.image_bet04:
                index = 3;
                tv = findViewById(R.id.text_bet04);
                break;
            case R.id.image_bet05:
                index = 4;
                tv = findViewById(R.id.text_bet05);
                break;
            case R.id.image_bet06:
                index = 5;
                tv = findViewById(R.id.text_bet06);
                break;
            case R.id.image_bet07:
                index = 6;
                tv = findViewById(R.id.text_bet07);
                break;
            case R.id.image_bet08:
                index = 7;
                tv = findViewById(R.id.text_bet08);
                break;
            default:
                index = -1;
                break;
        }

        if(index >= 0){
            mStake[index]++;
            if(tv != null){
                tv.setText("" + mStake[index]);
            }
        }
    }
}
