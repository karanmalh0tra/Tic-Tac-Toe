package com.example.project4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView status;

    //set opcodes
    public static final int THREAD_ONE_UPDATE = 1;
    public static final int THREAD_TWO_UPDATE = 2;
    public static final int THREAD_ONE_HANDLER = 3;
    public static final int THREAD_TWO_HANDLER = 4;
    public static final int GAME_OVER = 5;

    //declaring global variables
    public static String[] isOccupied = new String[9];
    public static int[] txtArray = {R.id.text00, R.id.text01, R.id.text02,
                            R.id.text10, R.id.text11, R.id.text12,
                            R.id.text20, R.id.text21, R.id.text22};
    public static int chances = 0; //can go upto 9

    //for switch cases while placing X or 0
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;

    //Ui Thread Handler
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            Log.i(TAG, "handleMessage: mHandler has msg.what="+msg.what+"&msg.arg1="+msg.arg1+"&msg.arg2="+msg.arg2);
            int what = msg.what;
            int index = msg.arg1;
            int player = msg.arg2;
            int getStatus = placeXor0(index, player);
            switch(what){
                case THREAD_ONE_UPDATE:
                    if (getStatus == 0) {
                        //game continues
                        chances++;
                        if (chances <= 9) {
                            Message msg1 = PlayerTwoHandler.obtainMessage(THREAD_TWO_HANDLER);
                            PlayerTwoHandler.sendMessage(msg1);
                        } else {
                            //send message of game over to both handler threads
                            status = findViewById(R.id.status);
                            status.setText("Draw Game");
                            Message msg1 = PlayerOneHandler.obtainMessage(GAME_OVER);
                            PlayerOneHandler.sendMessage(msg1);
                            Message msg2 = PlayerTwoHandler.obtainMessage(GAME_OVER);
                            PlayerTwoHandler.sendMessage(msg2);
                        }
                    } else {
                        //send message of game over to both handler threads
                        status = findViewById(R.id.status);
                        status.setText("Player 1 Won!");
                        Message msg1 = PlayerOneHandler.obtainMessage(GAME_OVER);
                        PlayerOneHandler.sendMessage(msg1);
                        Message msg2 = PlayerTwoHandler.obtainMessage(GAME_OVER);
                        PlayerTwoHandler.sendMessage(msg2);
                    }
                    break;
                case THREAD_TWO_UPDATE:
                    if (getStatus == 0) {
                        //game continues
                        chances++;
                        if (chances <= 9) {
                            Message msg1 = PlayerOneHandler.obtainMessage(THREAD_ONE_HANDLER);
                            PlayerOneHandler.sendMessage(msg1);
                        } else {
                            //send message of game over to both handler threads
                            status = findViewById(R.id.status);
                            status.setText("Draw Game");
                            Message msg1 = PlayerOneHandler.obtainMessage(GAME_OVER);
                            PlayerOneHandler.sendMessage(msg1);
                            Message msg2 = PlayerTwoHandler.obtainMessage(GAME_OVER);
                            PlayerTwoHandler.sendMessage(msg2);
                        }
                    } else {
                        //send message of game over to both handler threads
                        status = findViewById(R.id.status);
                        status.setText("Player 2 Won!");
                        Message msg1 = PlayerOneHandler.obtainMessage(GAME_OVER);
                        PlayerOneHandler.sendMessage(msg1);
                        Message msg2 = PlayerTwoHandler.obtainMessage(GAME_OVER);
                        PlayerTwoHandler.sendMessage(msg2);
                    }
                    break;
            }
        }
    };

    //Worker Threads
    Thread t1 = new Thread(new ThreadOneRunnable());
    Thread t2 = new Thread(new ThreadTwoRunnable());

    //Worker Handlers
    private Handler PlayerOneHandler;
    private Handler PlayerTwoHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.new_game);
        status = findViewById(R.id.status);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeGame();
                if(t1.isAlive()){
                    PlayerOneHandler.removeCallbacksAndMessages(null);
                    PlayerTwoHandler.removeCallbacksAndMessages(null);
                    mHandler.removeCallbacksAndMessages(null);
                    t1 = new Thread(new ThreadOneRunnable());
                    t1.start();
                }else{
                    t1 = new Thread(new ThreadOneRunnable());
                    t1.start();
                }
                if(t2.isAlive()){
                    PlayerOneHandler.removeCallbacksAndMessages(null);
                    PlayerTwoHandler.removeCallbacksAndMessages(null);
                    mHandler.removeCallbacksAndMessages(null);
                    t2 = new Thread(new ThreadTwoRunnable());
                    t2.start();
                } else {
                    t2 = new Thread(new ThreadTwoRunnable());
                    t2.start();
                }

            }
        });
    }
    
    public class ThreadOneRunnable implements Runnable {

        public void run(){
            Log.i(TAG, "run: inside ThreadOneRunnable");
            // looper condition to avoid another creation
//            if(Looper.myLooper() == null){
//                //pass condition check
//            }
            Looper.prepare();

            int index = getPlayer1Index();
            Message msg = mHandler.obtainMessage(THREAD_ONE_UPDATE);
            msg.arg1 = index;
            msg.arg2 = PLAYER1;
            mHandler.sendMessageDelayed(msg,2000);

            PlayerOneHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    int what = msg.what;
                    switch(what){
                        case THREAD_ONE_HANDLER:
                            int index = getPlayer1Index();
                            Message message = mHandler.obtainMessage(THREAD_ONE_UPDATE);
                            message.arg1 = index;
                            message.arg2 = PLAYER1;
                            mHandler.sendMessageDelayed(message,2000);
                            break;
                        case GAME_OVER:
                            getLooper().quit();
                            break;
                    }
                    Log.i(TAG, "handleMessage: PlayerOneHandler has msg.what="+msg.what+"&msg.arg1="+msg.arg1+"&msg.arg2="+msg.arg2);
                }
            };
            Looper.loop();
        }
    }

    public class ThreadTwoRunnable implements Runnable {

        public void run(){
            Log.i(TAG, "run: inside ThreadTwoRunnable");

            Looper.prepare();
            PlayerTwoHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    int what = msg.what;
                    switch(what){
                        case THREAD_TWO_HANDLER:
                            int index = getPlayer2Index();
                            Message message = mHandler.obtainMessage(THREAD_TWO_UPDATE);
                            message.arg1 = index;
                            message.arg2 = PLAYER2;
                            mHandler.sendMessageDelayed(message,2000);
                            break;
                        case GAME_OVER:
                            getLooper().quit();
                            break;
                    }
                    Log.i(TAG, "handleMessage: PlayerTwoHandler has msg.what="+msg.what+"&msg.arg1="+msg.arg1+"&msg.arg2="+msg.arg2);
                }
            };

            Looper.loop();
        }
    }

    public void initializeGame(){
        Log.i(TAG, "initializeGame: initializing Game");
        TextView t;
        TextView status;
        for(int i=0;i<9;i++){
            t = findViewById(txtArray[i]);
            t.setText("");
            isOccupied[i]="";
        }
        status = findViewById(R.id.status);
        status.setText("");
        chances = 0;
    }

    //Place the provided item at the position mentioned
    public int placeXor0(int index, int thread_number){
        TextView t;
        Log.i(TAG, "placeXor0: Thread value is "+thread_number+" and index value is "+index);
        switch(thread_number){
            case PLAYER1: {
                Log.i(TAG, "placeXor0: inside Player1 case");
                t = findViewById(txtArray[index]);
                t.setText("X");
                isOccupied[index] = "X";
                break;
            }
            case PLAYER2: {
                t = findViewById(txtArray[index]);
                t.setText("0");
                isOccupied[index] = "0";
                break;
            }
        }
        return checkGameStatus();
    }

    //Return 1 if there's a winner and 0 if there isn't
    public int checkGameStatus() {
        Log.i(TAG, "checkGameStatus: inside");
        //rows
        if(isOccupied[0].equals(isOccupied[1]) && isOccupied[0].equals(isOccupied[2])
        && !isOccupied[0].equals("")){
            return 1;
        }
        if(isOccupied[3].equals(isOccupied[4]) && isOccupied[3].equals(isOccupied[5])
                && !isOccupied[3].equals("")){
            return 1;
        }
        if(isOccupied[6].equals(isOccupied[7]) && isOccupied[6].equals(isOccupied[8])
                && !isOccupied[6].equals("")){
            return 1;
        }

        //columns
        if(isOccupied[0].equals(isOccupied[3]) && isOccupied[0].equals(isOccupied[6])
                && !isOccupied[0].equals("")){
            return 1;
        }
        if(isOccupied[1].equals(isOccupied[4]) && isOccupied[1].equals(isOccupied[7])
                && !isOccupied[1].equals("")){
            return 1;
        }
        if(isOccupied[2].equals(isOccupied[5]) && isOccupied[2].equals(isOccupied[8])
                && !isOccupied[2].equals("")){
            return 1;
        }


        //diagonals
        if(isOccupied[0].equals(isOccupied[4]) && isOccupied[0].equals(isOccupied[8])
                && !isOccupied[0].equals("")){
            return 1;
        }
        if(isOccupied[2].equals(isOccupied[4]) && isOccupied[2].equals(isOccupied[6])
                && !isOccupied[2].equals("")){
            return 1;
        }
        Log.i(TAG, "checkGameStatus: returned value is 0");
        return 0;
    }

    //Player 1 Logic
    public int getPlayer1Index()
    {
        int index=9;
        for(int i=0;i<9;i++){
            if(isOccupied[i].equals("")){
                index = i;
                break;
            }
        }
        return index;
    }

    //Player 2 Logic
    public int getPlayer2Index()
    {
        int index;
        while(true)
        {
            Random r = new Random();
            index = r.nextInt(9-0)+0; // gets a random value between 0 and 9

            if(isOccupied[index].equals(""))
            {
                break;
            }

        }
        return index;
    }
}