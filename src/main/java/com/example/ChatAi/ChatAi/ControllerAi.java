package com.example.ChatAi.ChatAi;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500/")
public class ControllerAi {

@Autowired
private AiService aiService;

    @PostMapping("/chat")
    public String communicateAI(@RequestBody String msg){
        JSONObject jsonObject = new JSONObject(msg);
        String userRequest = jsonObject.getString("chat");
return aiService.ReplyUserQst(userRequest);
    }

    @PostMapping(value = "/generate-form", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> generateForm(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");

        // Get AI-generated form JSON
        JSONObject aiResponse = aiService.generateForm(prompt);

        // Return JSON as Map
        return aiResponse.toMap();
    }




}
