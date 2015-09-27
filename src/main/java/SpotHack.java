/**
 * Created by JP on 9/26/2015.
 */

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.verbs.*;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class SpotHack {

    public static void main(String[] args) {

        get("/", (req, res) -> "Hello, World!");

        TwilioRestClient client = new TwilioRestClient("AC453b582a03479da76a746bb5e07a76c9", "22a9ef40bffef8f64e1555813a01649c");
        Account mainAccount = client.getAccount();

        post("/sms", (req, res) -> {
            String ngrokUrl = "https://6f3de0fb.ngrok.io";
            String body = req.queryParams("Body");
            String to = req.queryParams("From");
            String from = "+18452452410";

            String uri = ngrokUrl + "/call?q=" + URLEncoder.encode(body, "UTF-8");

            CallFactory callFactory = mainAccount.getCallFactory();
            Map<String, String> callParams = new HashMap<>();
            callParams.put("To", to);
            callParams.put("From", from);
            callParams.put("Url", uri);
            callParams.put("Method", "GET");
            callFactory.create(callParams);

            TwiMLResponse twiml = new TwiMLResponse();
            twiml.append(new Message("Your tunes are on the way!"));

            res.type("text/xml");
            return twiml.toXML();
        });

        get("/call", (req, res) -> {
            TwiMLResponse twiml = new TwiMLResponse();

            String query = req.queryParams("q");
            String trackUrl = getTrackUrl(query);

            if (trackUrl != null) {
                twiml.append(new Play(trackUrl));
            } else {
                twiml.append(new Say("Sorry, song not found."));
            }

            res.type("text/xml");
            return twiml.toXML();
        });
    }

    private static String getTrackUrl(String query) {
        String url = "http://api.spotify.com/v1/search";
        HttpResponse<JsonNode> jsonResponse;
        try {
            jsonResponse = Unirest.get(url)
                    .header("accept", "application/json")
                    .queryString("q", query)
                    .queryString("type", "track")
                    .asJson();
            return jsonResponse.getBody().getObject().getJSONObject("tracks")
                    .getJSONArray("items").getJSONObject(0).getString("preview_url");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}