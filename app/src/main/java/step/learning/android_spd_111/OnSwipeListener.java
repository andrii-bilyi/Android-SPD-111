package step.learning.android_spd_111;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
         /*
        Детектор жестів створюється у контексті - він обмежений цим
        контекстом та передає події до його обробників. У якості контексту
        має бути View, свайпи по якому аналізуються.
         */
        this.gestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // передаємо аналіз жесту на наш детектор
        return gestureDetector.onTouchEvent(event);
    }
    // оголошуємо інтерфейсні методи для переозначення у класах-слухачах
    public void onSwipeBottom(){}
    public void onSwipeLeft(){}
    public void onSwipeRight(){}
    public void onSwipeTop(){}

    // створюємо аналізатор для детектора жестів
    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener{
        private static final int MIN_DISTANCE = 70;
        private static final int MIN_VELOCITY = 80;
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true; //ознака оброблення - запобігаємо Click
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isDispatched = false;
            if (e1 != null){
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                // abs(x) - модуль числа
                // відстань (без урахування знаку) по горизонталі більша, ніж
                // по вертикалі, значить це або лівий, або правий свайп
                if(Math.abs(distanceX) > Math.abs(distanceY)){
                    // горизонтальний свайп - цікавить тільки горизонтальні
                    // відстань та швидкість
                    if(Math.abs(distanceX) > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY){
                        if(distanceX > 0){ // e1 ----- e2
                            onSwipeRight();
                        }
                        else { //  e2 ------ e1
                            onSwipeLeft();
                        }
                        isDispatched = true;
                    }
                }
                else { // або верхній, або нижній
                    if(Math.abs(distanceY) > MIN_DISTANCE && Math.abs(velocityY) > MIN_VELOCITY){
                        if(distanceY > 0){ // e1
                           onSwipeBottom(); // e2
                        }
                        else {
                            onSwipeTop();
                        }
                        isDispatched = true;
                    }
                }
            }


            return isDispatched;
        }
    }
}
/*
Оброблення swipe - жестів проведення по екрану.
Базові детектори жестів не налаштовані на розрізнення
свайпів, для них є узагальнений жест - Fling -
торкання у точці е1, проведення та завершення жесту у т. е2
Визначаються координати точок та швидкість проведення.
Задача: визначити до якого з 4х свайпів належатиме жест
за умови, що ідеально вертикальних чи горизонтальних
жестів фактично не існує. Також бажано встановити граничні
значення як до відстані проведення, так і до швидкості.

Д.З. Проєкт "Калькулятор" - завершити роботу
* реалізувати стирання останнього символу (Backspace)
  за допомогою свайпу ліворуч (по вікну результатів)
 */
