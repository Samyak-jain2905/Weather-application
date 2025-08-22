
package mypackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date; // used for simple date string like "2025-08-22"
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/myservlett")
public class myservlett extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Accept both param names for safety
        String city = request.getParameter("userInput");
        if (city == null || city.trim().isEmpty()) {
            city = request.getParameter("city");
        }

        if (city == null || city.trim().isEmpty()) {
            request.setAttribute("error", "City name cannot be empty!");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
        String apiKey = "da7c7c6ed7306d05c535946f750459f1";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                + encodedCity + "&appid=" + apiKey;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            InputStream inputStream;

            if (status >= 200 && status < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
                // Read error details
                StringBuilder errorContent = new StringBuilder();
                try (Scanner errScanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    while (errScanner.hasNextLine()) {
                        errorContent.append(errScanner.nextLine());
                    }
                }
                request.setAttribute("error", "API error: " + errorContent);
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            // Read success response
            StringBuilder responseContent = new StringBuilder();
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 Scanner scanner = new Scanner(reader)) {
                while (scanner.hasNextLine()) {
                    responseContent.append(scanner.nextLine());
                }
            }

            // Parse JSON
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseContent.toString(), JsonObject.class);

            // Date (epoch seconds -> ms)
            long datetimeMs = json.get("dt").getAsLong() * 1000L;
            String date = new Date(datetimeMs).toString(); // simple yyyy-mm-dd

            // Temperature (Kelvin -> °C)
            double tempKelvin = json.getAsJsonObject("main").get("temp").getAsDouble();
            int tempCelsius = (int) Math.round(tempKelvin - 273.15);

            // Humidity
            int humidity = json.getAsJsonObject("main").get("humidity").getAsInt();

            // Wind speed (m/s -> km/h)
            double windSpeedMs = json.getAsJsonObject("wind").get("speed").getAsDouble();
            double windSpeedKmh = windSpeedMs * 3.6;
            String windSpeedFormatted = String.format("%.1f", windSpeedKmh);

            // Weather condition text
            String weatherCondition = json.getAsJsonArray("weather")
                    .get(0).getAsJsonObject().get("main").getAsString();

            // Set attributes for JSP
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", tempCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeedFormatted);

        } catch (Exception e) {
            request.setAttribute("error", "Failed to fetch weather data: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Forward to JSP (shows either data or error)
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
