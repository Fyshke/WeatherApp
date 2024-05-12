import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class API {
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=Europe%2FBerlin";

        try {
            HttpURLConnection connection = fetchApiResponse(url);
            if (connection.getResponseCode() != 200) {
                System.out.println("Error: can't connect to API");
                return null;
            }

            StringBuilder resultJSON = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while(scanner.hasNext()) {
                resultJSON.append(scanner.nextLine());
            }
            scanner.close();
            connection.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultJSONObject = (JSONObject) parser.parse(String.valueOf(resultJSON));
            JSONObject hourly = (JSONObject) resultJSONObject.get("hourly"); //JSON has hourly property

            //index for the current time
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            //temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //weather code
            JSONArray weatherCode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            //humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            //gather all data
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray getLocationData(String locationName) {

        //replace white space to fit API request format
        locationName = locationName.replaceAll(" ", "+");

        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";
        try {
            HttpURLConnection connection = fetchApiResponse(url);
            if(connection.getResponseCode() != 200) { //status code 200 = OK
                System.out.println("Error: can't connect to API");
                return null;
            } else {
                StringBuilder resultJSON = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    resultJSON.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultJSONObject = (JSONObject) parser.parse(String.valueOf(resultJSON));

                JSONArray locationData = (JSONArray) resultJSONObject.get("results"); //JSON has results property
                return locationData;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String url) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private static int findIndexOfCurrentTime (JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }
        return 0;
    }

    private static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        // date is formatted this way in the API
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        return currentDateTime.format(formatter);
    }

    private static String convertWeatherCode(long weathercode) {
        String weatherCondition = "";

        // WMO Weather interpretation codes (WW)
        //Code	Description
        //0	Clear sky
        //1, 2, 3	Mainly clear, partly cloudy, and overcast
        //45, 48	Fog and depositing rime fog
        //51, 53, 55	Drizzle: Light, moderate, and dense intensity
        //56, 57	Freezing Drizzle: Light and dense intensity
        //61, 63, 65	Rain: Slight, moderate and heavy intensity
        //66, 67	Freezing Rain: Light and heavy intensity
        //71, 73, 75	Snow fall: Slight, moderate, and heavy intensity
        //77	Snow grains
        //80, 81, 82	Rain showers: Slight, moderate, and violent
        //85, 86	Snow showers slight and heavy
        //95 *	Thunderstorm: Slight or moderate
        //96, 99 *	Thunderstorm with slight and heavy hail

        if (weathercode == 0L) {
            weatherCondition = "Clear";
        } else if (weathercode > 0L && weathercode <= 3L) {
            weatherCondition = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)) {
            weatherCondition = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            weatherCondition = "Snow";
        }

        return  weatherCondition;
    }
}
