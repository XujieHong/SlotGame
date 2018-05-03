package com.kenhong.slotgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class GamePanelView extends FrameLayout {

    private static final int ROLLING_STOP = 1;
    private static final int ROLLING_FOCUS = 2;
    private static final int TRY_TO_STOP_ROLLING = 3;

    private ImageView[] ivArr = new ImageView[24];

    private int[] mStake = new int[8];

    private Context mContext;
    private Handler mHandler;

    private SoundPool mSoundPool;
    private int mRollingSoundId;
    private int mStopSoundId;
    private int mInsertCoinSoundId;
    private int mErrorSoundId;
    private boolean mIsRolling = false;
    private final int[] mOddsTable = {10, 10, 25, 50, 5, 2, 10, 20, 2, 0, 5, 2, 10, 10, 2, 20, 5, 2, 10, 20, 2, 0, 5, 2};
    private final int[] mTypeTable = {6, 4, 0, 0, 7, 7, 5, 3, 3, 8, 7, 6, 6, 4, 1, 1, 7, 5, 5, 2, 2, 9, 7, 4};
    private final int[] mProbablityTable = {};

    private boolean isGameRunning = false;
    private boolean isTryToStop = false;
    private boolean isTryToStopRolling = false;
    private int preIndex = 0;
    private int currentIndex = 0;
    private int currentTotal = 0;
    private int stayIndex = 0;

    private int mCoins = 10;

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
        mIsRolling = true;
        isTryToStop = false;
        currentSpeed = MIN_SPEED;
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
                            Log.d("KenHong", "pre = " + preIndex + "; cur = " + currentIndex);

                            Message focusMessage = new Message();
                            focusMessage.what = ROLLING_FOCUS;
                            focusMessage.arg1 = preIndex;
                            focusMessage.arg2 = currentIndex;
                            mHandler.sendMessage(focusMessage);

                            preIndex = currentIndex;
                            currentIndex++;
                            if (currentIndex >= ivArr.length) {
                                currentIndex = 0;
                            }

                            if (isTryToStop && currentSpeed == DEFAULT_SPEED && stayIndex == currentIndex) {
                                isGameRunning = false;

                                Message stopMessage = new Message();
                                stopMessage.what = ROLLING_STOP;
                                mHandler.sendMessageDelayed(stopMessage, 200);
                            }
                        }
                    });
                }
            }
        }).start();

    }

    private void setFocus(int pre, int cur){
        Log.d("KenHong", "pre = " + pre + "; cur = " + cur);
        mSoundPool.play(mRollingSoundId, 1.0f, 1.0f, 0, 0, 2.0f);
        ivArr[pre].setImageAlpha(ALPHA_NO_FOCUS);
        ivArr[cur].setImageAlpha(ALPHA_FOCUS);
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
        isTryToStopRolling = true;
        stayIndex = position;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isTryToStopRolling) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    post(new Runnable() {
                        @Override
                        public void run() {
                            isTryToStopRolling = false;
                            Message tryToStopRollingMessage = new Message();
                            tryToStopRollingMessage.what = TRY_TO_STOP_ROLLING;
                            mHandler.sendMessage(tryToStopRollingMessage);
                        }
                    });
                }
            }
        }).start();
    }

    public void init(Context context, Handler handler){
        mContext = context;
        mHandler = handler;
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build())
                .build();

        mRollingSoundId = mSoundPool.load(context, R.raw.doo, 1);
        mStopSoundId = mSoundPool.load(context, R.raw.didong, 1);
        mInsertCoinSoundId = mSoundPool.load(context, R.raw.insertcoin, 1);
        mErrorSoundId = mSoundPool.load(context, R.raw.ding, 1);

        for(int i = 0; i < 8; i++){
            mStake[i] = 0;
        }

        TextView tv = findViewById(R.id.coins);
        tv.setText("" + mCoins);
    }

    private void endRolling(){
        mSoundPool.play(mStopSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        mIsRolling = false;

        int type = mTypeTable[stayIndex];

        if(type >= 0 && type < 8){
            for(int i = 0; i < 8; i++){
                if(type == i){
                    mCoins += mOddsTable[stayIndex] * mStake[type];

                    Log.d("KenHong", "stayIndex = " + stayIndex
                            + "; mOddsTable[stayIndex] = " + mOddsTable[stayIndex]
                            + "; type = " + type
                            + "; mStake[type] = " + mStake[type]);
                }
                mStake[i] = 0;
                TextView tv = findViewById(R.id.text_bet01 + i);
                tv.setText("0");
            }
            TextView tv = findViewById(R.id.coins);
            tv.setText("" + mCoins);
        }
    }

    public void processMessage(int msg, int arg1, int arg2){
        switch (msg){
            case ROLLING_STOP:
                endRolling();
                break;
            case ROLLING_FOCUS:
                setFocus(arg1, arg2);
                break;
            case TRY_TO_STOP_ROLLING:
                isTryToStop = true;
                break;
            default:
                break;
        }
    }

    public void betOn(int id){
        if(mIsRolling || mCoins <= 0){
            mSoundPool.play(mErrorSoundId, 1.0f, 1.0f, 2, 0, 1.0f);
        }else {
            int index = id - R.id.image_bet01;

            mCoins--;
            mStake[index]++;
            TextView tv = findViewById(R.id.text_bet01 + index);
            tv.setText("" + mStake[index]);

            tv = findViewById(R.id.coins);
            tv.setText("" + mCoins);

            mSoundPool.play(mInsertCoinSoundId, 1.0f, 1.0f, 2, 0, 1.0f);
        }
    }
}
