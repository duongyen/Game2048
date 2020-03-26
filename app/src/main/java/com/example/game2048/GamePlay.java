package com.example.game2048;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.Random;

public class GamePlay extends AppCompatActivity implements View.OnClickListener {
    private Button undo, home, reset;
    private TextView textScore, textHighScore;

    private int score = 0, highScore = 0;
    private boolean won = false;
    private int[][] boxPrevious = new int[4][4];
    private int[][] box = new int[4][4];

    private TextView[][] textView = new TextView[4][4];
    private GestureDetectorCompat gestureDetectorCompat = null;

    //    Use to save all empty boxes
    private ArrayList<Integer> arrayX = new ArrayList<>();
    private ArrayList<Integer> arrayY = new ArrayList<>();
    private ArrayList<Integer> clone = new ArrayList();

    String TAG = "my_debug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        connectView();

        Bundle b = getIntent().getExtras();
        int value = 0; // or other values
        if(b != null)
            value = b.getInt("Type");
        if (value ==0){
            startGame();
        }
        else
        {
            ContinueGame();
        }

//        demoCheckSwipe();
//        demoStuckSwipeDown();
//        demoEndGame();
//        demoWinGame();

        setClickButton();
        DetectGesture detectGesture = new DetectGesture();
        detectGesture.setActivity(this);

        gestureDetectorCompat = new GestureDetectorCompat(this, detectGesture);
    }

    private void connectView() {
        textView[0][0] = findViewById(R.id.box00);
        textView[0][1] = findViewById(R.id.box01);
        textView[0][2] = findViewById(R.id.box02);
        textView[0][3] = findViewById(R.id.box03);
        textView[1][0] = findViewById(R.id.box10);
        textView[1][1] = findViewById(R.id.box11);
        textView[1][2] = findViewById(R.id.box12);
        textView[1][3] = findViewById(R.id.box13);
        textView[2][0] = findViewById(R.id.box20);
        textView[2][1] = findViewById(R.id.box21);
        textView[2][2] = findViewById(R.id.box22);
        textView[2][3] = findViewById(R.id.box23);
        textView[3][0] = findViewById(R.id.box30);
        textView[3][1] = findViewById(R.id.box31);
        textView[3][2] = findViewById(R.id.box32);
        textView[3][3] = findViewById(R.id.box33);

        undo = findViewById(R.id.undo);
        home = findViewById(R.id.home);
        reset = findViewById(R.id.reset);

        textScore = findViewById(R.id.score);
        textHighScore = findViewById(R.id.highScore);
    }

    private void setClickButton(){
        undo.setOnClickListener(this);
        home.setOnClickListener(this);
        reset.setOnClickListener(this);
    }

    Database database;
    private void startGame() {
        database = new Database(this);
        highScore = database.getHighScore();
        score =0;
        textHighScore.setText(String.valueOf(highScore));
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                box[i][j] = 0;
            }
        }

        Random random = new Random();
        int x = random.nextInt(4), y = random.nextInt(4);
        box[x][y] = ranValue();

        do {
            x = random.nextInt(4);
            y = random.nextInt(4);
            if(box[x][y] == 0) {
                box[x][y] = ranValue();
                break;
            }
        } while(true);

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                boxPrevious[i][j] = box[i][j];
            }
        }
        display();
    }
    private void ContinueGame() {
        database = new Database(this);
        highScore = database.getHighScore();
        score =database.getScore();
        textHighScore.setText(String.valueOf(highScore));
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                box[i][j] = database.getBox(i,j);
            }
        }

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                boxPrevious[i][j] = box[i][j];
            }
        }
        display();
    }

    private void display() {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(box[i][j] != 0)
                {
                    textView[i][j].setText(String.valueOf(box[i][j]));
                }
                else {
                    textView[i][j].setText("");
                }

            }
        }

        textScore.setText(String.valueOf(score));


        Log.d(TAG, "display: " + score);

        // save high score
        highScore = database.getHighScore();
        if(highScore < score)
        {
            highScore = score;
            textHighScore.setText(String.valueOf(highScore));
            database.saveHighScore(highScore);
        }
        //

        if(winGame() && !won){
            WinGameDialog winGameDialog = new WinGameDialog();
            winGameDialog.show(getSupportFragmentManager(), "VICTORY");
            won = true;

            // save high score
            highScore = database.getHighScore();
            if(highScore < score)
            {
                highScore = score;
                textHighScore.setText(String.valueOf(highScore));
                database.saveHighScore(highScore);
            }
            //
        }

        if(endGame()){

            EndGameDialog endGameDialog = new EndGameDialog();
            endGameDialog.show(getSupportFragmentManager(), "DO IT!");
        }

    }

    private int ranValue() {
        if(new Random().nextInt(10) <= 7) return 2;
        return 4;
    }

    private void seekEmpty() {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(box[i][j] == 0) {
                    arrayX.add(i);
                    arrayY.add(j);
                }
            }
        }
    }

    private void addValue() {
        seekEmpty();
        Random random = new Random();
        int x = random.nextInt(arrayX.size());

        box[arrayX.get(x)][arrayY.get(x)] = ranValue();
        arrayX.clear();
        arrayY.clear();
    }

    public void swipeLeft() {
//        Toast.makeText(GamePlay.this, "Left", Toast.LENGTH_SHORT).show();
        int i, j;
        int[][] temp = new int[4][4];

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) temp[i][j] = box[i][j];
        }

        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) {
                if(temp[i][j] != 0) clone.add(temp[i][j]);
            }

            for(j = 1; j < clone.size(); j++) {
                if(clone.get(j - 1).equals(clone.get(j))) {
                    clone.set(j - 1, clone.get(j) * 2);
                    score += clone.get(j) * 2;
                    clone.remove(j);
                }
            }

            for(j = 0; j < 4; j++) {
                if(j < clone.size()) temp[i][j] = clone.get(j);
                else temp[i][j] = 0;
            }
            clone.clear();
        }

        boolean canMove = false;
        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) {
                if(temp[i][j] != box[i][j]) {
                    canMove = true;
                    break;
                }
            }
        }

        if(canMove) {
            for(i = 0; i < 4; i++){
                for(j = 0; j < 4; j++){
                    boxPrevious[i][j] = box[i][j];
                    box[i][j] = temp[i][j];
                }
            }

            addValue();
            display();
        }
    }

    public void swipeRight() {
//        Toast.makeText(GamePlay.this, "Right", Toast.LENGTH_SHORT).show();

        int i, j;
        int[][] temp = new int[4][4];

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) temp[i][j] = box[i][j];
        }

        for(i = 0; i < 4; i++) {
            for(j = 3; j >= 0; j--) {
                if(temp[i][j] != 0) clone.add(temp[i][j]);
            }

            for(j = 1; j < clone.size(); j++) {
                if(clone.get(j - 1).equals(clone.get(j))) {
                    clone.set(j - 1, clone.get(j) * 2);
                    score += clone.get(j) * 2;
                    clone.remove(j);
                }
            }

            for(j = 3; j >= 0; j--) {
                if(3 - j < clone.size()) temp[i][j] = clone.get(3 - j);
                else temp[i][j] = 0;
            }
            clone.clear();
        }

        boolean canMove = false;
        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) {
                if(temp[i][j] != box[i][j]) {
                    canMove = true;
                    break;
                }
            }
        }

        if(canMove) {
            for(i = 0; i < 4; i++){
                for(j = 0; j < 4; j++){
                    boxPrevious[i][j] = box[i][j];
                    box[i][j] = temp[i][j];
                }
            }

            addValue();
            display();
        }
    }

    public void swipeDown() {
//        Toast.makeText(GamePlay.this, "Down", Toast.LENGTH_SHORT).show();

        int i, j;
        int[][] temp = new int[4][4];

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) temp[i][j] = box[i][j];
        }

        for(j = 0; j < 4; j++) {
            for(i = 3; i >= 0; i--){
                if(temp[i][j] != 0) clone.add(temp[i][j]);
            }

            for(i = 1; i < clone.size(); i++) {
                if(clone.get(i - 1).equals(clone.get(i))) {
                    clone.set(i - 1, clone.get(i) * 2);
                    score += clone.get(i) * 2;
                    clone.remove(i);
                }
            }

            for(i = 3; i >= 0; i--) {
                if(3 - i < clone.size()) temp[i][j] = clone.get(3 - i);
                else temp[i][j] = 0;
            }
            clone.clear();
        }

        boolean canMove = false;
        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) {
                if(temp[i][j] != box[i][j]) {
                    canMove = true;
                    break;
                }
            }
        }

        if(canMove) {
            for(i = 0; i < 4; i++){
                for(j = 0; j < 4; j++){
                    boxPrevious[i][j] = box[i][j];
                    box[i][j] = temp[i][j];
                }
            }

            addValue();
            display();
        }
    }

    public void swipeUp() {
//        Toast.makeText(GamePlay.this, "Up", Toast.LENGTH_SHORT).show();

        int i, j;
        int[][] temp = new int[4][4];

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) temp[i][j] = box[i][j];
        }

        for(j = 0; j < 4; j++) {
            for(i = 0; i < 4; i++){
                if(temp[i][j] != 0) clone.add(temp[i][j]);
            }

            for(i = 1; i < clone.size(); i++) {
                if(clone.get(i - 1).equals(clone.get(i))) {
                    clone.set(i - 1, clone.get(i) * 2);
                    score += clone.get(i) * 2;
                    clone.remove(i);
                }
            }

            for(i = 0; i < 4; i++) {
                if(i < clone.size()) temp[i][j] = clone.get(i);
                else temp[i][j] = 0;
            }
            clone.clear();
        }

        boolean canMove = false;
        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) if(temp[i][j] != box[i][j]) canMove = true;
        }

        if(canMove) {
            for(i = 0; i < 4; i++){
                for(j = 0; j < 4; j++){
                    boxPrevious[i][j] = box[i][j];
                    box[i][j] = temp[i][j];
                }
            }

            seekEmpty();
            addValue();
            display();
        }
    }

    private void demoCheckSwipe() {
        int[][] demo = new int[4][4];
        int i, j;
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) demo[i][j] = 0;
        }
        demo[1][3] = 2;
        demo[2][3] = 2;
        demo[3][0] = 2;
        demo[3][1] = 2;
        demo[3][2] = 4;
        demo[3][3] = 8;

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) box[i][j] = demo[i][j];
        }
        display();
    }

    private void demoStuckSwipeDown(){
        int[][] demo = new int[4][4];
        int i, j;
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) demo[i][j] = 0;
        }

        demo[3][0] = 2;
        demo[3][1] = 2;
        demo[3][2] = 4;
        demo[3][3] = 8;

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) box[i][j] = demo[i][j];
        }
        display();
    }

    private void demoEndGame(){
        int[][] demo = new int[4][4];
        int i, j;
        for(i = 0; i < 4; i++) {
            for(j = 0; j < 4; j++) {
                if(i % 2 == 0) {
                    if(j % 2 == 0) demo[i][j] = 2;
                    else demo[i][j] = 4;
                } else {
                    if(j % 2 == 0) demo[i][j] = 4;
                    else demo[i][j] = 2;
                }
            }
        }

        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++) box[i][j] = demo[i][j];
        }
        display();
    }

    private void demoWinGame(){
        startGame();
        box[3][3] = 2048;

        display();
    }

    private boolean endGame(){
        int i, j;
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){
                if(box[i][j] == 0) return false;

                if(i == 0) {
                    if(j == 0){
                        if(box[i][j] == box[i + 1][j] || box[i][j] == box[i][j + 1]) return false;
                    } else if(j == 3) {
                        if(box[i][j] == box[i + 1][j] || box[i][j] == box[i][j - 1]) return false;
                    } else {
                        if(box[i][j] == box[i + 1][j] || box[i][j] == box[i][j - 1] || box[i][j] == box[i][j + 1]) return false;
                    }
                } else if(i == 3) {
                    if(j == 0){
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i][j + 1]) return false;
                    } else if(j == 3) {
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i][j - 1]) return false;
                    } else {
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i][j - 1] || box[i][j] == box[i][j + 1]) return false;
                    }
                } else {
                    if(j == 0){
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i + 1][j] || box[i][j] == box[i][j + 1]) return false;
                    } else if(j == 3) {
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i + 1][j] || box[i][j] == box[i][j - 1]) return false;
                    } else {
                        if(box[i][j] == box[i - 1][j] || box[i][j] == box[i + 1][j] || box[i][j] == box[i][j - 1] || box[i][j] == box[i][j + 1]) return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean winGame(){
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++) if(box[i][j] == 2048) return true;
        }

        return false;
    }

    // set event in this activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if(v == undo) {
            for(int i = 0; i < 4; i++){
                for(int j = 0; j < 4; j++){
                    box[i][j] = boxPrevious[i][j];
                }
            }
            display();
        }

        if(v == reset) {
            // save high score
            highScore = database.getHighScore();
            if(highScore < score)
            {
                highScore = score;
                textHighScore.setText(String.valueOf(highScore));
                database.saveHighScore(highScore);
            }
            //
            startGame();
        }

        if(v == home){
            // save high score
            highScore = database.getHighScore();
            if(highScore < score)
            {
                highScore = score;
                database.saveHighScore(highScore);
            }
            //save box
            for(int i = 0; i < 4; i++){
                for(int j = 0; j < 4; j++){
                   database.saveBox(i,j,box[i][j]);
                }
            }
            //save score
            database.saveScore(score);
            //
            Intent intent = new Intent(GamePlay.this, MainActivity.class);
            startActivity(intent);
        }
    }
}