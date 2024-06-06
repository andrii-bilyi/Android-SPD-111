package step.learning.android_spd_111;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;

    private boolean aOperand;
    private static Double num1;
    private static Double num2;
    private Character operand;

    private Animation clickAnimation;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        clickAnimation = AnimationUtils.loadAnimation(this,R.anim.calc);
        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
        if(savedInstanceState == null) { //немає збереженого стану - перший запуск
            tvResult.setText("0");
        }

        /* Задача: циклом перебрати ресурсні кнопки calc_btn_{i} і для
           кожної з них поставити один обробник onDigitButtonClick */
        for (int i = 0; i < 10; i++) {
            findViewById( // на заміну R.calc_btn_0 приходить наступний вираз
                    getResources().getIdentifier(
                                    "calc_btn_" + i,
                                    "id",
                                    getPackageName()
                            )

            ).setOnClickListener(this::onDigitButtonClick);
        }
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::onInverseClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::onSquareClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::onSqrtClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::onCEClick);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::onCClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::onPercentClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_point).setOnClickListener(this::onPointClick);
        findViewById(R.id.calc_btn_opposite).setOnClickListener(this::onOppositeClick);

        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onDivideClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onMultiplyClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::onMinusClick);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::onPlusClick);

        findViewById(R.id.calc_btn_equality).setOnClickListener(this::onEqualityClick);

        findViewById(R.id.calc_tv_result).setOnTouchListener(new OnSwipeListener(this){

            @Override
            public void onSwipeLeft() {
                Toast.makeText(CalcActivity.this, "Left", Toast.LENGTH_SHORT).show();
                onBackspaceClick(findViewById(R.id.calc_tv_result)); // Вызываем onBackspaceClick с параметром
            }

        });

    }

    private void onEqualityClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        String history = (String) tvHistory.getText();
        history += result + " =";

        // Разбиваем строку на подстроки по знаку =
        String[] parts = history.split("=");
        // Если количество подстрок больше одной, оставляем только первую
        if (parts.length > 1) {
            history = history.substring(history.indexOf('=') + 1);;
        }

        if(result==""){
            num2 = num1;
        }else {
            num2 = Double.parseDouble(result);
        }

        if (operand != null) {
            double x = 0;
            switch (operand) {
                case '+':
                    x = num1 + num2;
                    break;
                case '-':
                    x = num1 - num2;
                    break;
                case '*':
                    x = num1 * num2;
                    break;
                case '/':
                    if (num2 != 0) {
                        x = num1 / num2;
                    } else {
                        Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                default:
                    // Обработка неверного операнда
                    System.out.println("Ошибка: неверный операнд");
                    break;
            }
            result = (x == (int)x)
                    ? String.valueOf((int)x)
                    : String.valueOf(x);
        }
        aOperand = true;
        tvHistory.setText(history);
        tvResult.setText(result);
    }

    private String isOperand(){
        String result = tvResult.getText().toString();
        String history = (String) tvHistory.getText();

        // Проверяем наличие символа ")"
        int closingParenthesisIndex = history.indexOf(')');
        if (closingParenthesisIndex != -1) { // Если ")" найдена
            // Очищаем строку до ")"
            history = history.substring(closingParenthesisIndex + 1);
        }

        int indexOfUnicodeSymbol = history.indexOf('\u00F7'); // Ищем позицию символа ÷
        if (indexOfUnicodeSymbol == -1) { // Если символ ÷ не найден, ищем символ ✕
            indexOfUnicodeSymbol = history.indexOf('\u2715');
        }
        if (indexOfUnicodeSymbol == -1) { // Если символ ✕ не найден, ищем символ -
            indexOfUnicodeSymbol = history.indexOf('\u2014');
        }
        if (indexOfUnicodeSymbol == -1) { // Если символ - не найден, ищем символ +
            indexOfUnicodeSymbol = history.indexOf('\uFF0B');
        }
        if (indexOfUnicodeSymbol != -1) { // Если найден хотя бы один из символов
            history = history.substring(indexOfUnicodeSymbol + 1);

        } else {
            System.out.println("В строке нет символов ÷ или ✕.");
        }
        int equalsIndex = history.indexOf('=');
        if (equalsIndex != -1) { // Проверяем, найден ли знак равенства
            history = history.substring(equalsIndex + 1);
        }
        aOperand = true;
        if(result!=""){
            num1 = Double.parseDouble(result);
        }
        return history;
    }
    private void onPlusClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        String history = isOperand();
        history += result + " \uFF0B ";
        operand = '+';
        tvHistory.setText(history);
    }

    private void onMinusClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        String history = isOperand();
        history += result + " \u2014 ";
        operand = '-';
        tvHistory.setText(history);
    }

    private void onMultiplyClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        String history = isOperand();
        history += result + " \u2715 ";
        operand = '*';
        tvHistory.setText(history);
    }

    private void onDivideClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        String history = isOperand();
        history += result + " \u00F7 ";
        operand = '/';
        tvHistory.setText(history);
    }


    private void onSquareClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x *= x;
        String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);
        if (str.length() > 13){
            str = str.substring(0,13);
        }
        tvResult.setText(str);

        String history = (String) tvHistory.getText();
        history += "sqr(" + result + ")";
        tvHistory.setText(history);
    }

    private void onSqrtClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        double num = 1;
        double num2 = 1;
        do {
            num2 = num;
            num = (num + x / num) / 2;
            if(num == num2) break;
        } while (num * num > x);
        String str = (num == (int)num)
                ? String.valueOf((int)num)
                : String.valueOf(num);
        if (str.length() > 13){
            str = str.substring(0,13);
        }
        tvResult.setText(str);

        String history = (String) tvHistory.getText();
        history += "\u221A(" + result + ")";
        tvHistory.setText(history);
    }
    private void onOppositeClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        if(result.startsWith("-")){
            // Если строка начинается с минуса, убираем его
            result = result.substring(1);
        }else {
            // Если строка не начинается с минуса, добавляем его
            if(!result.equals("0")){
                result = "-" + result;
            }
        }
        tvResult.setText(result);
    }
    private void onPointClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        if (result.contains(".")) {
            return;
        } else {
            result += ".";
            tvResult.setText(result);
        }
    }

    private void onBackspaceClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        if(!result.isEmpty()){
            result = result.substring(0, result.length() - 1);
        }
        if(result.isEmpty() || result.equals("-")){
            result = "0";
        }
        tvResult.setText(result);
    }

    private void onPercentClick(View view) {
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if(num1 != null){
            num2 =  x * num1 / 100;
            x = num2;
        }
        result = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);
        tvResult.setText(result);
    }

    private void onInverseClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if(x == 0){
            Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
            return;
        }
        x = 1.0/x;
        String str = (x == (int)x)
                ? String.valueOf((int)x)
                : String.valueOf(x);
        if (str.length() > 13){
            str = str.substring(0,13);
        }
        tvResult.setText(str);

        String history = (String) tvHistory.getText();
        history += "1/(" + result + ")";
        tvHistory.setText(history);
    }
    private void onCEClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        tvResult.setText("0");
    }
    private void onCClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        tvResult.setText("0");
        tvHistory.setText("");

        aOperand = false;
        num1 = null;
        num2 = null;
        operand = null;
    }

    private void onDigitButtonClick(View view){
        view.startAnimation(clickAnimation); //применение анимации
        String result = tvResult.getText().toString();
        if(result.length() >= 10){
            Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
            return;
        }
        if(result.equals("0")){
            result = "";
        }
        if(aOperand==true){
            result = "";
            aOperand = false;
//            num1 = null;
//            num2 = null;
//            operand = null;
            //tvHistory.setText("");
        }
//        if(operand!=null){
//            result = "";
//        }
        result += ((Button) view).getText();
        tvResult.setText(result);
    }
    /*
    При зміні конфігурації пристрою (поворотах, змінах налаштувань, тощо) відбувається
    перезапуск активності. При цьому подаються події життєвого циклу
    onSaveInstanceState - при виході з активності перед перезапуском
    onRestoreInstanceState - при відновленні активності після перезапуску
    До обробників передається Bundle, що є сховищем, яке дозволяє зберегти та відновити дані
    Також збережений Bundle передається до onCreate, що дозволяє визначити
     чи це перший запуск, чи перезапуск через зміну конфігурації
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState); //потрібно
        outState.putCharSequence("tvResult", tvResult.getText());
        outState.putCharSequence("tvHistory", tvHistory.getText());

        outState.putBoolean("aOperand", aOperand);
        if (num1 != null) {
            outState.putDouble("num1", num1);
        }
        if (num2 != null) {
            outState.putDouble("num2", num2);
        }
        if (operand != null) {
            outState.putChar("operand", operand);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
        tvHistory.setText(savedInstanceState.getCharSequence("tvHistory"));

        aOperand = savedInstanceState.getBoolean("aOperand");
        if (savedInstanceState.containsKey("num1")) {
            num1 = savedInstanceState.getDouble("num1");
        }
        if (savedInstanceState.containsKey("num2")) {
            num2 = savedInstanceState.getDouble("num2");
        }
        if (savedInstanceState.containsKey("operand")) {
            operand = savedInstanceState.getChar("operand");
        }
    }

}