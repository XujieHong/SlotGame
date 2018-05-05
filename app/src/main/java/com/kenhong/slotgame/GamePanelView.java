package com.kenhong.slotgame;

import android.content.Context;
import android.graphics.Color;
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

import java.util.Random;


public class GamePanelView extends FrameLayout {

    private static final int ROLLING_STOP = 1;
    private static final int ROLLING_FOCUS = 2;
    private static final int TRY_TO_STOP_ROLLING = 3;

    private ImageView[] ivArr = new ImageView[24];
    private Boolean[] isIvFocusedArr = {false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    private int[] mStake = new int[8];
    private int[] mPreStake = new int[8];

    private Handler mHandler;

    private SoundPool mSoundPool;
    private int mRollingSoundId;
    private int mStopSoundId;
    private int mInsertCoinSoundId;
    private int mErrorSoundId;

    private final int[] mOddsTable = {10, 10, 25, 50, 5, 2, 10, 20, 2, 0, 5, 2, 10, 10, 2, 20, 5, 2, 10, 20, 2, 0, 5, 2};
    private final int[] mTypeTable = {6, 4, 0, 0, 7, 7, 5, 3, 3, 8, 7, 6, 6, 4, 1, 1, 7, 5, 5, 2, 2, 9, 7, 4};
    private final int[] mProbablityTable = {10, 10, 4, 2, 20, 50, 10, 5, 50, 100, 20, 50, 10, 10, 50, 5, 20, 50, 10, 5, 50, 100, 20, 50};
    private int mSumProbablityTable = 0;

    private boolean isGameRunning = false;
    private boolean isTryToStop = false;
    private boolean isTryToStopRolling = false;
    private boolean isRolling = false;
    private boolean isBeted = false;

    private int preIndex = 0;
    private int currentIndex = 0;
    private int currentTotal = 0;
    private int stayIndex = 0;
    private int remainRollingTimes = 0;

    private int mCoins = 100;

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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void setupView(Boolean reset){
        for(int i = 0; i < 24; i++){
            if(reset){
                isIvFocusedArr[i] = false;
            }

            ivArr[i] = findViewById(R.id.image01 + i);
            if(!isIvFocusedArr[i]){
                ivArr[i].setImageAlpha(ALPHA_NO_FOCUS);
            }
        }
    }

    public boolean startGame(Boolean isClicked){
        if(isRolling){
            return false;
        }

        if(isClicked){
            setupView(true);
        }

        int stakes = 0;
        int preStakes = 0;
        for(int i = 0; i < 8; i++){
            stakes += mStake[i];
            preStakes += mPreStake[i];
        }

        if(stakes == 0){
            if(mCoins >= preStakes && preStakes > 0){
                TextView tv;
                if(remainRollingTimes <= 0){
                    mCoins -= preStakes;
                }

                for(int i = 0; i < 8; i++){
                    mStake[i] = mPreStake[i];
                    tv = findViewById(R.id.text_bet01 + i);
                    if(mStake[i] > 0){
                        tv.setText("" + mStake[i]);
                        if(remainRollingTimes <= 0) {
                            tv.setTextColor(Color.BLUE);
                        }
                    }else {
                        tv.setText("0");
                        if(remainRollingTimes <= 0) {
                            tv.setTextColor(Color.GRAY);
                            Log.d("KenHong", "Color setGRAY");
                        }
                    }
                }

                tv = findViewById(R.id.coins);
                tv.setText("" + mCoins);
            }else{
                mSoundPool.play(mErrorSoundId, 1.0f, 1.0f, 2, 0, 1.0f);
                return false;
            }
        }

        isGameRunning = true;
        isRolling = true;
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

        return true;
    }

    private void setFocus(int pre, int cur){
        Log.d("KenHong", "pre = " + pre + "; cur = " + cur);
        mSoundPool.play(mRollingSoundId, 1.0f, 1.0f, 0, 0, 2.0f);

        if(!isIvFocusedArr[pre]){
            ivArr[pre].setImageAlpha(ALPHA_NO_FOCUS);
        }

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

    public void tryToStop() {


        if(remainRollingTimes > 0){
            remainRollingTimes--;
            do {
                int position = new Random().nextInt(mSumProbablityTable);
                for(int i = 0; i < 24; i++){
                    position -= mProbablityTable[i];
                    if(position < 0){
                        stayIndex = i;
                        break;
                    }
                }
            }while (stayIndex == 9 || stayIndex == 21);

        }else{
            int position = new Random().nextInt(mSumProbablityTable);
            for(int i = 0; i < 24; i++){
                position -= mProbablityTable[i];
                if(position < 0){
                    stayIndex = i;
                    break;
                }
            }
        }

        if(stayIndex == 9){
            remainRollingTimes = 3;
        }else if(stayIndex == 21){
            remainRollingTimes = 3;
        }

        isTryToStopRolling = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isTryToStopRolling) {
                    try {
                        Thread.sleep(2000);
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
        mHandler = handler;

        setupView(true);

        for(int i = 0; i < 8; i++){
            mStake[i] = 0;
            mPreStake[i] = 0;
        }

        mSumProbablityTable = 0;
        for(int i = 0; i < 24; i++){
            mSumProbablityTable += mProbablityTable[i];
        }


        TextView tv = findViewById(R.id.coins);
        tv.setText("" + mCoins);

        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(15)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build())
                .build();

        mRollingSoundId = mSoundPool.load(context, R.raw.doo, 1);
        mStopSoundId = mSoundPool.load(context, R.raw.didong, 1);
        mInsertCoinSoundId = mSoundPool.load(context, R.raw.insertcoin, 1);
        mErrorSoundId = mSoundPool.load(context, R.raw.ding, 1);
    }

    public void release(){
        Log.d("KenHong", "Release");
        mSoundPool.release();
    }

    private void endRolling(){
        mSoundPool.play(mStopSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        isRolling = false;
        isBeted = false;

        int type = mTypeTable[stayIndex];

        if(type >= 0 && type < 8){
            for(int i = 0; i < 8; i++){
                TextView tv = findViewById(R.id.text_bet01 + i);
                if(type == i){
                    mCoins += mOddsTable[stayIndex] * mStake[type];
                    Log.d("KenHong", "Color setRED");
                    tv.setTextColor(Color.RED);

                    Log.d("KenHong", "stayIndex = " + stayIndex
                            + "; mOddsTable[stayIndex] = " + mOddsTable[stayIndex]
                            + "; type = " + type
                            + "; mStake[type] = " + mStake[type]);
                }
                mPreStake[i] = mStake[i];
                mStake[i] = 0;
                //TextView tv = findViewById(R.id.text_bet01 + i);
                //tv.setText("0");
            }
            TextView tv = findViewById(R.id.coins);
            tv.setText("" + mCoins);
        }
    }

    public void processMessage(int msg, int arg1, int arg2){
        switch (msg){
            case ROLLING_STOP:
                isIvFocusedArr[stayIndex] = true;
                endRolling();
                if(remainRollingTimes > 0){
                    //remainRollingTimes--;
                    if(startGame(false)){
                        tryToStop();
                    }
                }

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
        if(isRolling || mCoins <= 0){
            mSoundPool.play(mErrorSoundId, 1.0f, 1.0f, 2, 0, 1.0f);
        }else {
            int index = id - R.id.image_bet01;
            if(!isBeted){
                isBeted = true;
                for(int i = 0; i < 8; i++){
                    TextView tv = findViewById(R.id.text_bet01 + i);
                    tv.setText("0");
                    tv.setTextColor(Color.GRAY);
                }
            }

            mCoins--;
            mStake[index]++;
            TextView tv = findViewById(R.id.text_bet01 + index);
            tv.setText("" + mStake[index]);
            tv.setTextColor(Color.BLUE);

            tv = findViewById(R.id.coins);
            tv.setText("" + mCoins);

            mSoundPool.play(mInsertCoinSoundId, 1.0f, 1.0f, 2, 0, 1.0f);
        }
    }
}
