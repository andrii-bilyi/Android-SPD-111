package step.learning.android_spd_111;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.android_spd_111.orm.ChatMessage;
import step.learning.android_spd_111.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private final byte[] buffer = new byte[8096];
    //паралельні запити до кількох ресурсів не працюють, виконується  лише один
    //це обмежує вибір виконання сервісу.
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean isEditTextDisabled = false;
    private EditText etNik;
    private EditText etMessage;
    private ScrollView chatScroller;
    private LinearLayout container;
    private MediaPlayer newMessageSound;

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private Handler handler = new Handler();


    private ImageButton toggleSoundButton;
    private boolean isMuted = false;

    private Animation clickAnimation;
    private Animation clickAnimation2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clickAnimation = AnimationUtils.loadAnimation(this,R.anim.chat_move);
        clickAnimation2 = AnimationUtils.loadAnimation(this,R.anim.chat_opacity);
        updateChat();

        urlToImageView(
                "https://cdn-icons-png.flaticon.com/512/5962/5962463.png",
                findViewById(R.id.chat_iv_logo)
        );

        etNik = findViewById(R.id.chat_et_nik);
        etMessage = findViewById(R.id.chat_et_message);
        chatScroller = findViewById(R.id.chat_scroller);
        container = findViewById(R.id.chat_container);

        newMessageSound = MediaPlayer.create(this, R.raw.pickup);
        //newMessageSound.setLooping(true);
        //newMessageSound.start();
        // Найдите ImageButton в макете
        toggleSoundButton = findViewById(R.id.toggle_sound_button);
        // Установите слушатель кликов для ImageButton
        toggleSoundButton.setOnClickListener(this::onToggleSoundClick);
        // Обновите изображение кнопки в зависимости от текущего состояния звука
        updateButtonImage();

        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        container.setOnClickListener( (v) -> hideSoftInput() );
    }
    private void onToggleSoundClick(View view) {
        // Переключаем состояние звука
        if (isMuted) { unmute(); }
        else { mute(); }
        // Обновляем флаг состояния
        isMuted = !isMuted;
        // Обновление изображения кнопки после изменения состояния
        updateButtonImage();
    }
    private void mute() {
        // Выключаем звук медиаплеера
        newMessageSound.setVolume(0, 0);
    }
    private void unmute() {
        // Включаем звук медиаплеера
        newMessageSound.setVolume(1, 1);
    }
    private void updateButtonImage() {
        // Обновление изображения кнопки в зависимости от текущего состояния звука
        if (isMuted) {
            toggleSoundButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
        } else {
            toggleSoundButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
        }
    }





    private void hideSoftInput(){
        //клавіатура з'являється автоматично через фокус введення, прибрати її - це прибрати фокус
        //шукаємо елемент що має фокус введення
        View focusView = getCurrentFocus();
        if(focusView != null){
            // запитуємо систему щодо засобів управління клавіатурою
            InputMethodManager manager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            //прибираємо клавіатуру з фокусованого елемента
            manager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            //прибираємо фокус з елемента
            focusView.clearFocus();

        }
    }

    private void updateChat(){
        if(executorService.isShutdown()) return;
        CompletableFuture
                .supplyAsync(this::loadChat, executorService)
                .thenApplyAsync(this::processChatResponse)
                .thenAcceptAsync(this::displayChatMessages);

        handler.postDelayed(this::updateChat, 3000);
    }
    private void onSendClick(View v){
        v.startAnimation(clickAnimation);
        clickAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(clickAnimation2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // Проверяем, отключен ли уже EditText
        if (!isEditTextDisabled) {
            // Сделать EditText неактивным
            etNik.setEnabled(false);
            // Обновить флаг состояния
            isEditTextDisabled = true;
        }
        String author = etNik.getText().toString();
        String message = etMessage.getText().toString();
        etMessage.setText("");
        if(author.isEmpty()){
            Toast.makeText(this, "Заповніть 'Нік'", Toast.LENGTH_SHORT).show();
            return;
        }
        if(message.isEmpty()){
            Toast.makeText(this, "Введіть повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor(author);
        chatMessage.setText(message);

        CompletableFuture
                .runAsync(() -> sendChatMessage(chatMessage), executorService);
    }

    private void sendChatMessage(ChatMessage chatMessage){
        /*
        Необхідно сформувати POST_запит на URL чату та передати дані форми з
        полями author та msg з відповідними значеннями з chatMessage
        дані форми:
        - заголовок Content-Type: application/x-www-form-urlencoded
        - тіло у вигляді: author=TheAuthor&msg=The%20Message
         */
        try {
            //1. Готуємо підключення та налаштовуємо його
            URL url = new URL(CHAT_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode(0); //не ділити на фрагменти(чанки)
            connection.setDoOutput(true); //запис у підключення - передача тіла
            connection.setDoInput(true); //читання - одержання тіла відповіді від сервера
            connection.setRequestMethod("POST");
            //заголовки передаються через setRequestProperty()
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Connection", "close");

            //2. Запис тіла (DoOutput)
            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format( //author=TheAuthor&msg=The%20Message
                    "author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(chatMessage.getText(), StandardCharsets.UTF_8.name())
            );
            connectionOutput.write(body.getBytes(StandardCharsets.UTF_8));

            //3. Надсилаємо - "виштовхуємо" буфер
            connectionOutput.flush();
            //3.1 Звільняємо ресурс [якщо не вживати форму try]
            connectionOutput.close();

            //4. Одержуємо відповідь
            int statusCode = connection.getResponseCode();

            if(statusCode == 201){
                //якщо потрібне тіло відповіді, то воно у потоці .getInputStream()
                //запустити оновлення чату
                updateChat();
            }
            else {
                //хоча при помилці тіло таке ж, але воно вилучається getErrorStream()
                InputStream connectionInput = connection.getErrorStream();
                body = readString(connectionInput);
                connectionInput.close();
                Log.e("sendChatMessage", body);
            }

            //5. Закриваємо підключення
            connection.disconnect();
        }
        catch (Exception ex){
            Log.e("sendMessage", ex.getMessage());
        }
    }
    private String loadChat(){
        try(InputStream chatStream = new URL( CHAT_URL ).openStream()) {
            //          String response = readString(chatStream);
//            runOnUiThread( () ->
//                    ((TextView)findViewById(R.id.chat_tv_title)).setText(response)
//            );
            return readString(chatStream);
        }
        catch (Exception ex){
            Log.e("ChatActivity::loadChat()", ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }
    private boolean processChatResponse(String response){
        boolean wasNewMessage = false;
        boolean isFirstProcess = this.chatMessages.isEmpty();
        try {
            ChatResponse chatResponse = ChatResponse.fromJsonString(response);
            for (ChatMessage message : chatResponse.getData()){
                if(this.chatMessages.stream().noneMatch(
                        m -> m.getId().equals(message.getId()))){
                    //немає жодного повідомлення з таким id, як у message - це нове повідомлення
                    this.chatMessages.add(message);
                    wasNewMessage = true;
                }
            }
            if(isFirstProcess){
                this.chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
            }
            else  if(wasNewMessage){
                if (!chatMessages.isEmpty()) {
                    ChatMessage message = chatMessages.get(chatMessages.size() - 1);
                    //ChatMessage message = chatMessages.get(0);
                    if(!etNik.getText().toString().equals(message.getAuthor())){
                        newMessageSound.start();
                    }
                }

            }
        }
        catch (IllegalArgumentException ex){
            Log.e("ChatActivity::processChatResponse",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return wasNewMessage;
    }
    private void displayChatMessages(boolean wasNewMessage){
        if(!wasNewMessage) return;
        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my);
        Drawable otherBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_other);
        LinearLayout.LayoutParams msgDateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgDateParams.setMargins(10,10,10,10);
        msgDateParams.gravity = Gravity.END;

        LinearLayout.LayoutParams msgMyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgMyParams.setMargins(0,10,10,10);
        msgMyParams.gravity = Gravity.END;

        LinearLayout.LayoutParams msgOtherParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgOtherParams.setMargins(10,10,0,10);
        msgOtherParams.gravity = Gravity.START;

        runOnUiThread(() -> {
//            ((TextView)findViewById(R.id.chat_tv_title)).setText(sb.toString())



//            for (ChatMessage message : this.chatMessages){
//                //LinearLayout messageLayout
//                TextView tv = new TextView(this);
//                tv.setText(message.getAuthor() + ": " + message.getText() + "\n");
//                tv.setBackground(myBackground);
//                tv.setGravity(Gravity.END);
//                tv.setPadding(15,5,15,5);
//                tv.setLayoutParams(msgParams);
//                container.addView(tv);
//            }

            for (ChatMessage message : this.chatMessages){
                if(message.getView() != null){ //вже показане
                    continue;
                }
                // Создание LinearLayout
                LinearLayout messageLayout = new LinearLayout(this);
                messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                messageLayout.setOrientation(LinearLayout.VERTICAL);
                messageLayout.setPadding(10,10,10,10);
                if(etNik.getText().toString().equals(message.getAuthor()) ){
                    messageLayout.setBackground(myBackground);
                    messageLayout.setLayoutParams(msgMyParams);
                }
                else {
                    messageLayout.setBackground(otherBackground);
                    messageLayout.setLayoutParams(msgOtherParams);
                }
                // Создание TextView для автора
                TextView authorTextView = new TextView(this);
                authorTextView.setText(message.getAuthor() + ": ");

                // Создание TextView для сообщения
                TextView messageTextView = new TextView(this);
                messageTextView.setText(message.getText());
                messageTextView.setTypeface(null, Typeface.BOLD);
                messageTextView.setTextSize(22);

                // Создание TextView для даты
                TextView dateTextView = new TextView(this);
                dateTextView.setText(message.getMoment().toString());
                dateTextView.setTypeface(null, Typeface.ITALIC);
                dateTextView.setLayoutParams(msgDateParams);

                // Добавление TextView в LinearLayout
                if(!etNik.getText().toString().equals(message.getAuthor())){ // на своих сообщениях отображать автора нет необходимости
                    messageLayout.addView(authorTextView);
                }
                messageLayout.addView(messageTextView);
                messageLayout.addView(dateTextView);

                container.addView(messageLayout);
                message.setView(messageLayout);
            }
            /*
            chatScroller.fullScroll(View.FOCUS_DOWN);
            Асинхронність Android призводить до того, що на момент команди не всі представлення,
            додані до контейнера, вже сформовані. Прокрутка діятиме лише на поточне наповнення контейнера.
             */
            chatScroller.post( //передача дії яка виконається після поточної черги
                    () -> chatScroller.fullScroll(View.FOCUS_DOWN)
            );
        });
    }


    private String readString( InputStream stream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while ((len = stream.read(buffer)) != -1){
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
        // Освобождаем ресурсы медиаплеера при уничтожении активности
        if (newMessageSound != null) {
            newMessageSound.release();
            newMessageSound = null;
        }
    }

    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture.supplyAsync( () -> {
            try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
                return BitmapFactory.decodeStream( is );
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            }, executorService ).thenAccept( imageView::setImageBitmap );
    }
}
/*
Робота з мережею Інтернет
основу складає клас java.net.URL
традиційно для Java створення об'єкту не призводить до якоїсь активності,
лише створюється програмний об'єкт.
Підключення та передача даних здійснюється при певних командах, зокрема,
відкриття потоку.
Читання даних з потоку має особливості
 - мульти-байтове кодування: різні символи мають різну байтову довжину. Це
     формує пораду спочатку одержати всі дані у бінарному вигляді і потім
     декодувати як рядок (замість одержання фрагментів даних і їх перетворення)
 - запити до мережі не можуть виконуватись з основного (UI) потоку. Це
     спричинює виняток (android.os.NetworkOnMainThreadException).
     Варіанти рішень
     = запустити в окремому потоці
        + простіше і наочніше
        - складність завершення різних потоків, особливо, якщо їх багато.
     = запустити у фоновому виконавці
        + централізоване завершення
        - не забути завершення
 - Для того щоб застосунок міг звертатись до мережі йому потрібні
      відповідні дозволи. Без них виняток (Permission denied (missing INTERNET permission?))
      Дозволи зазначаються у маніфесті
       <uses-permission android:name="android.permission.INTERNET"/>
 - Необхідність запуску мережних запитів у окремих потоках часто призводить до
     того, що з них обмежено доступ до елементів UI
     (Only the original thread that created a view hierarchy can touch its views.)
     Перехід до UI потоку здійснюється або викликом runOnUiThread або переходом
     до синхронного режиму.
 */