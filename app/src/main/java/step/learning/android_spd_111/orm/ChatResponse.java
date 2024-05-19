package step.learning.android_spd_111.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    private int status;
    private List<ChatMessage> data;

    public static ChatResponse fromJsonString(String jsonString){
        try {
            JSONObject root = new JSONObject(jsonString);
            int status = root.getInt("status");
            JSONArray arr = root.getJSONArray("data");
            List<ChatMessage> data = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                data.add(ChatMessage.fromJson(arr.getJSONObject(i)));
            }
            ChatResponse res = new ChatResponse();
            res.setStatus(status);
            res.setData(data);
            return res;
        }
        catch (Exception ex){
            throw new IllegalArgumentException(ex.getMessage());
        }

    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ChatMessage> getData() {
        return data;
    }

    public void setData(List<ChatMessage> data) {
        this.data = data;
    }
}
