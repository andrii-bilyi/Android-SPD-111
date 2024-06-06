package step.learning.android_spd_111;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation opacityAnimation;
    private Animation sizeAnimation;
    private Animation size2Animation;
    private Animation arcAnimation;
    private Animation bellAnimation;
    private Animation moveAnimation;
    private boolean isMovePlaying;

    private AnimationSet comboAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        opacityAnimation = AnimationUtils.loadAnimation(this, R.anim.opacity);
        sizeAnimation = AnimationUtils.loadAnimation(this, R.anim.size);
        size2Animation = AnimationUtils.loadAnimation(this, R.anim.size2);
        arcAnimation = AnimationUtils.loadAnimation(this, R.anim.arc);
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.bell);
        moveAnimation = AnimationUtils.loadAnimation(this, R.anim.move);
        comboAnimation = new AnimationSet(false);
        comboAnimation.addAnimation(opacityAnimation);
        comboAnimation.addAnimation(size2Animation);

        findViewById(R.id.anim_opacity_block).setOnClickListener(this::opacityClick);
        findViewById(R.id.anim_size_block).setOnClickListener(this::sizeClick);
        findViewById(R.id.anim_size2_block).setOnClickListener(this::size2Click);
        findViewById(R.id.anim_arc_block).setOnClickListener(this::arcClick);
        findViewById(R.id.anim_bell_block).setOnClickListener(this::bellClick);
        findViewById(R.id.anim_move_block).setOnClickListener(this::moveClick);
        findViewById(R.id.anim_combo_block).setOnClickListener(this::comboClick);
        isMovePlaying = false;
    }

    private void comboClick(View view) {
        view.startAnimation(comboAnimation);
    }
    private void moveClick(View view) {
        if(isMovePlaying){
            view.clearAnimation();
        }
        else {
            view.startAnimation(moveAnimation);
        }
        isMovePlaying = !isMovePlaying;
    }

    private void bellClick(View view) {
        view.startAnimation(bellAnimation);
    }

    private void arcClick(View view) {
        view.startAnimation(arcAnimation);
    }

    private void opacityClick(View view){
        view.startAnimation(opacityAnimation);
    }
    private void sizeClick(View view){
        view.startAnimation(sizeAnimation);
    }
    private void size2Click(View view) {
        view.startAnimation(size2Animation);
    }
}