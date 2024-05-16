package step.learning.android_spd_111;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int FIELD_WIDTH = 16;
    private static final int FIELD_HEIGHT = 24;

    private TextView[][] gameField;
    private final LinkedList<Vector2> snake = new LinkedList<>();
    private final Handler handler = new Handler();
    private int fieldColor;
    private int snakeColor;
    private Direction moveDirection;
    private boolean isPlaying;
    private static final String food = new String(Character.toChars(0x1F34E)); //"\u1F34E" символ яблука
    private Vector2 foodPosition;
    private static final String bonus = new String(Character.toChars(0x1F370));
    private Vector2 bonusPosition;
    private static final Random _random = new Random();

    private TextView gameScore;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // додаємо аналізатор (слухач) свайпів на всю активність (R.id.main)
        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this){
            @Override
            public void onSwipeBottom() {
                //Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                if(moveDirection != Direction.top){
                    moveDirection = Direction.bottom;
                }
            }

            @Override
            public void onSwipeLeft() {
                //Toast.makeText(GameActivity.this, "Left", Toast.LENGTH_SHORT).show();
                if(moveDirection != Direction.right){
                    moveDirection = Direction.left;
                }
            }

            @Override
            public void onSwipeRight() {
                //Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
                if(moveDirection != Direction.left){
                    moveDirection = Direction.right;
                }
            }

            @Override
            public void onSwipeTop() {
                //Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                if(moveDirection != Direction.bottom){
                    moveDirection = Direction.top;
                }
            }
        });
        gameScore = findViewById(R.id.game_score);
        if(savedInstanceState == null) { //немає збереженого стану - перший запуск
            gameScore.setText("0");
        }
        fieldColor = getResources().getColor(R.color.game_field, getTheme());
        snakeColor = getResources().getColor(R.color.game_snake, getTheme());
        findViewById(R.id.game_btn_pause).setOnClickListener(this::onPauseClick);
        findViewById(R.id.game_btn_record).setOnClickListener(this::onRecordClick);
        initField();
        newGame();
    }

    private void onRecordClick(View view) {
        String str;
        //-- Открытие приватного файла приложения --------
        try {
            FileInputStream FIS = this.openFileInput("test.txt");
            //-- Чтение данных из файла ----------------------
            byte[] b = new byte[1024];
            int cnt = FIS.read(b, 0, b.length);
            FIS.close();
            str = new String(b, 0, cnt, "UTF8");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new AlertDialog.Builder(this)
                .setTitle("Record")
                .setMessage(str)
                .setIcon(android.R.drawable.star_on)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> step())
                .setNegativeButton("Cansel", (dialog, which) -> finish())
                .show();


//        public FileOutputStream openFileOutput (String Record, Context.MODE_APPEND);
    }
    public void writeScoreToFile() throws IOException {
        String scoreString = gameScore.getText().toString();
        int currentScore = Integer.parseInt(scoreString);
        if(currentScore < 6) return;

        String currentTime; // Получаем текущее время и дату
        currentTime = java.text.DateFormat.getDateTimeInstance().format(new Date());
        String dataToWrite = "Score: " + scoreString + ", Date: " + currentTime + "\n";
        FileOutputStream fos = this.openFileOutput("test.txt", Context.MODE_APPEND);
        byte[] a = dataToWrite.getBytes(StandardCharsets.UTF_8);
        fos.write(a, 0, a.length); // Записываем данные в файл
        fos.close(); // Закрываем поток вывода
        Toast.makeText(this, "Дані успішно збережено", Toast.LENGTH_SHORT).show();
    }

    private void onPauseClick(View view) {
        Button myButton = findViewById(R.id.game_btn_pause);
        if(isPlaying) {
            myButton.setText("Continue");
            super.onPause();
            isPlaying = false;
        }
        else {
            myButton.setText("Pause");
            super.onResume();
            isPlaying = true;
            step();
        }
    }

    private void step(){
        if(! isPlaying) return;

        Vector2 head = snake.getFirst();
        Vector2 newHead = new Vector2(head.x,head.y);
        switch (moveDirection){
            case bottom: newHead.y += 1; break;
            case left: newHead.x -= 1; break;
            case right: newHead.x += 1; break;
            case top: newHead.y -= 1; break;
        }
        //newHead.y -= 1;
        if( newHead.x < 0 || newHead.x >= FIELD_WIDTH ||
            newHead.y < 0 || newHead.y >= FIELD_HEIGHT) { // перевірка на вихід за екран
            gameOver();
            return;
        }
        //////////
//        for (int i = 0; i < snake.size() - 1; i++) { // виключення поїдання саму себе
//            Vector2 current = snake.get(i);
//            for (int j = i + 1; j < snake.size(); j++) {
//                Vector2 other = snake.get(j);
//                if (current.x == other.x && current.y == other.y) {
//                    gameOver();
//                    return;
//                }
//            }
//        }
        ///////////////////

        for (Vector2 v: snake) { // виключення поїдання саму себе
            if(newHead.x == v.x && newHead.y == v.y ){
                gameOver();
                return;
            }
        }

        if(newHead.x == foodPosition.x && newHead.y == foodPosition.y ){
            String scoreString = gameScore.getText().toString();
            int currentScore = Integer.parseInt(scoreString);
            currentScore++;
            String updatedScore = String.valueOf(currentScore);
            gameScore.setText(updatedScore);

            // видовження - не прибирати хвіст
            // перенести їжу але не не змійку
            gameField[foodPosition.x][foodPosition.y].setText("");
            do{
                foodPosition = Vector2.random();
            }while (isCellInSnake(foodPosition));
            gameField[foodPosition.x][foodPosition.y].setText(food);

            if (currentScore % 4 == 0) {
                do{
                    bonusPosition = Vector2.random();
                }while (isCellInSnake(bonusPosition));
                gameField[bonusPosition.x][bonusPosition.y].setText(bonus);
                handler.postDelayed(this::noBonus, 10000);
            }
        }
        else {
            Vector2 tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
            //перевірка чи не бонус
            if(bonusPosition!=null){
                if (newHead.x == bonusPosition.x && newHead.y == bonusPosition.y) {
                    gameField[bonusPosition.x][bonusPosition.y].setText(""); //стерти з поля бонус
                    tail = snake.getLast();
                    snake.remove(tail);
                    gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
                }
            }

        }
        snake.addFirst(newHead);
        gameField[newHead.x][newHead.y].setBackgroundColor(snakeColor);

        if(snake.size() <= 8){
            handler.postDelayed(this::step, 700);
        }
        if(snake.size() > 8 && snake.size() <= 12){
            handler.postDelayed(this::step, 500);
        }
        if(snake.size() > 12 && snake.size() <= 16) {
            handler.postDelayed(this::step, 300);
        }
        if(snake.size() > 16) {
            handler.postDelayed(this::step, 150);
        }

    }

    private void noBonus() { // зникнення бонусу
        gameField[bonusPosition.x][bonusPosition.y].setText("");
        bonusPosition = null;
    }

    private boolean isCellInSnake(Vector2 cell){
        for (Vector2 v: snake){
            if(v.x == cell.x && v.y == cell.y) return true;
        }
        return false;
    }
    private void initField(){ // Поле инициализации
        LinearLayout field = findViewById(R.id.game_field);

        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tvLayoutParams.weight = 1f;
        tvLayoutParams.setMargins(4,4,4,4);

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
        );
        rowLayoutParams.weight = 1f;

        gameField = new TextView[FIELD_WIDTH][FIELD_HEIGHT];

        for (int j = 0; j < FIELD_HEIGHT; j++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams( rowLayoutParams );
            for (int i = 0; i < FIELD_WIDTH; i++) {
                TextView tv = new TextView(this);
                tv.setBackgroundColor(fieldColor);
                // tv.setText( "0" );
                tv.setLayoutParams( tvLayoutParams );
                row.addView( tv );
                gameField[i][j] = tv;
            }
            field.addView( row );
        }
    }

    private void newGame(){
        String scoreString = gameScore.getText().toString();
        scoreString = "0";
        gameScore.setText(scoreString);

        for (Vector2 v: snake){
            gameField[v.x][v.y].setBackgroundColor(fieldColor);
        }
        snake.clear();
        if(foodPosition != null){
            gameField[foodPosition.x][foodPosition.y].setText("");
        }

        snake.add( new Vector2(8,10));
        snake.add( new Vector2(8,11));
        snake.add( new Vector2(8,12));
        snake.add( new Vector2(8,13));
        snake.add( new Vector2(8,14));
        for (Vector2 v: snake){
            gameField[v.x][v.y].setBackgroundColor(snakeColor);
        }
        foodPosition = new Vector2(3, 14);
        gameField[foodPosition.x][foodPosition.y].setText(food);

        moveDirection = Direction.top;
        isPlaying = true;
        step();

    }

    private void gameOver(){
        isPlaying = false;
        try{
            writeScoreToFile();
        } catch (IOException e) {
            Toast.makeText(this, "Помилка запису", Toast.LENGTH_SHORT).show();
        }
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Play one more time?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> newGame())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onPause() { // подія деактивації
        super.onPause();
        isPlaying = false;
    }
    @Override
    protected void onResume() { // подія активації
        super.onResume();
        if(!isPlaying){
            Button myButton = findViewById(R.id.game_btn_pause);
            myButton.setText("Pause");
            isPlaying = true;
            step();
        }
    }

    static class Vector2 {
        int x;
        int y;

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public static Vector2 random(){
            return new Vector2(_random.nextInt(FIELD_WIDTH), _random.nextInt(FIELD_HEIGHT));
        }
    }

    enum Direction {
        bottom,
        left,
        right,
        top
    }
}