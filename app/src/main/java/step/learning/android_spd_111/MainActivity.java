package step.learning.android_spd_111;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Animation clickAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        clickAnimation = AnimationUtils.loadAnimation(this,R.anim.calc);

        findViewById(R.id.main_btn_calc).setOnClickListener(this::onCalcButtonClick);
        findViewById(R.id.main_btn_game).setOnClickListener(this::onGameButtonClick);
        findViewById(R.id.main_btn_chat).setOnClickListener(this::onChatButtonClick);
        findViewById(R.id.main_btn_anim).setOnClickListener(this::onAnimButtonClick);
    }

    private void onCalcButtonClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        Intent intent = new Intent(this, CalcActivity.class);
        startActivity(intent);
    }

    private void onGameButtonClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    private void onChatButtonClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void onAnimButtonClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        Intent intent = new Intent(this, AnimActivity.class);
        startActivity(intent);
    }

}