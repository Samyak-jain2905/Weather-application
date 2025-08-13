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
import java.sql.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Scanner;

/**
 * Servlet implementation class myservlett
 */
@WebServlet("/myservlett")
public class myservlett extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public myservlett() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String city=request.getParameter("userInput");
		 if (city == null || city.trim().isEmpty()) {
	            request.setAttribute("error", "City name cannot be empty!");
	            request.getRequestDispatcher("index.jsp").forward(request, response);
	            return;
	        }

		String encodedcity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
		String api="da7c7c6ed7306d05c535946f750459f1";
		String apiurl="https://api.openweathermap.org/data/2.5/weather?q="+encodedcity+"&appid=" + api;

		
		
//		//API Integration
//		URL url=new URL(apiurl);
//		HttpURLConnection connection=(HttpURLConnection) url.openConnection();
//		connection.setRequestMethod("GET");
//		
		
		
		  try {
	            URL url = new URL(apiurl);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");

	            int status = connection.getResponseCode();
	            InputStream inputStream;

	            if (status >= 200 && status < 300) {
	                inputStream = connection.getInputStream();
	            } else {
	                inputStream = connection.getErrorStream();
	                Scanner errScanner = new Scanner(inputStream);
	                StringBuilder errorContent = new StringBuilder();
	                while (errScanner.hasNext()) {
	                    errorContent.append(errScanner.nextLine());
	                }
	                errScanner.close();

	                request.setAttribute("error", "API error: " + errorContent.toString());
	                request.getRequestDispatcher("index.jsp").forward(request, response);
	                return;
	            }
		  
		//reading the data from network
		InputStream inputstream=connection.getInputStream();
		InputStreamReader reader=new InputStreamReader(inputstream);
		
		//want to store in string
		StringBuilder responsecontent=new StringBuilder();
		
		//taking input from reader ,create scanner object
		java.util.Scanner scanner=new Scanner(reader);
		
        while(scanner.hasNext()) {
        	responsecontent.append(scanner.nextLine());
        }
		
		scanner.close();
		
		//parse the JSON response to extract temp and etc
		Gson gson=new Gson();
		JsonObject jsonobject=gson.fromJson(responsecontent.toString(), JsonObject.class);
		
		// Date and time
		long datetime=jsonobject.get("dt").getAsLong()*1000;
		String date =new Date(datetime).toString();
		
		//temp
		double temp_in_kel=jsonobject.getAsJsonObject("main").get("temp").getAsDouble();
		 int temperatureCelsius = (int) (temp_in_kel - 273.15);
		 
	   //humiditity
		int humidity=jsonobject.getAsJsonObject("main").get("humidity").getAsInt();
		
		//wind speed 
		double windspeed=jsonobject.getAsJsonObject("wind").get("speed").getAsDouble();
		
		//weather condition 
		String weathercondition=jsonobject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
		
		
		   request.setAttribute("date", date);
           request.setAttribute("city", city);
           request.setAttribute("temperature", temperatureCelsius);
           request.setAttribute("weatherCondition", weathercondition); 
           request.setAttribute("humidity", humidity);    
           request.setAttribute("windSpeed", windspeed);
           request.setAttribute("weatherData", responsecontent.toString());
           
		
		
		connection.disconnect();
		  }
		  catch (Exception e) {
	            request.setAttribute("error", "Failed to fetch weather data: " + e.getMessage());
	        }

		  
		
		//forward request to jsp page 
		request.getRequestDispatcher("index.jsp").forward(request,response);
		
	}

}
