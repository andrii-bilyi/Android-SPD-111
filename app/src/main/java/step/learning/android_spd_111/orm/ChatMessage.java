package step.learning.android_spd_111.orm;

import android.view.View;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class ChatMessage {
    private String id;
    private String author;
    private String text;
    private Date moment;

    //не дані, ознака показу
    private View view;

    private static SimpleDateFormat apiDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.UK);
    public static ChatMessage fromJson(JSONObject jsonObject) throws IllegalArgumentException{
        try {
            String id = jsonObject.getString("id");
            String author = jsonObject.getString("author");
            String text = jsonObject.getString("text");
            Date moment = apiDateFormat.parse(
                    jsonObject.getString("moment")
            );
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(id);
            chatMessage.setAuthor(author);
            chatMessage.setText(text);
            chatMessage.setMoment(moment);
            return chatMessage;
        }
        catch (Exception ex){
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getMoment() {
        return moment;
    }

    public void setMoment(Date moment) {
        this.moment = moment;
    }
}
