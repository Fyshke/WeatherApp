import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherApp extends JFrame {

    private JSONObject weatherData;

    private boolean convert = false;

    public WeatherApp(){
        super("Weather App");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){

        //search
        JTextField searchText = new JTextField();
        searchText.setBounds(15,15,250,45);
        searchText.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchText);

        //datetime
        JLabel dateTimeLabel = new JLabel(WeatherApp.getCurrentTime());
        dateTimeLabel.setBounds(15, 60, 420, 30);
        dateTimeLabel.setFont(new Font("Dialog", Font.PLAIN, 18));
        add(dateTimeLabel);

        //weather
        JLabel weatherImage = new JLabel(loadImage("src/images/clear.png"));
        weatherImage.setBounds(0, 125, 450, 220);
        add(weatherImage);

        //temperature
        JLabel temperatureText = new JLabel("shows °C");
        temperatureText.setBounds(0, 350, 450, 55);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //weather condition
        JLabel weatherDesc = new JLabel("shows condition");
        weatherDesc.setBounds(0, 405, 450, 40);
        weatherDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherDesc);

        //humidity
        JLabel humidityImage = new JLabel(loadImage("src/images/humidity.png"));
        humidityImage.setBounds(15, 500, 75, 65);
        add(humidityImage);
        JLabel humidityText = new JLabel("<html><b>Humidity</b> shows %</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //windspeed
        JLabel windspeedImage = new JLabel(loadImage("src/images/windspeed.png"));
        windspeedImage.setBounds(220, 500, 75, 65);
        add(windspeedImage);
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> shows km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        //search button
        JButton searchButton = new JButton(loadImage("src/images/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(270, 15, 45, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String userInput = searchText.getText();
                weatherData = API.getWeatherData(userInput);

                //update the gui
                String weatherCondition = (String) weatherData.get("weather_condition");
                switch (weatherCondition) {
                    case "Clear":
                        weatherImage.setIcon(loadImage("src/images/clear.png"));
                        getContentPane().setBackground(new Color(245, 241, 194));
                        break;
                    case "Cloudy":
                        weatherImage.setIcon(loadImage("src/images/cloudy.png"));
                        getContentPane().setBackground(new Color(153, 153, 155));
                        break;
                    case "Rain":
                        weatherImage.setIcon(loadImage("src/images/rain.png"));
                        getContentPane().setBackground(new Color(141, 175, 239));
                        break;
                    case "Snow":
                        weatherImage.setIcon(loadImage("src/images/snow.png"));
                        getContentPane().setBackground(new Color(116, 156, 241, 255));
                        break;
                }

                //update the rest
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " °C");

                weatherDesc.setText(weatherCondition);

                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);

        //convert °C to °F and km/h to mph
        JButton convertButton = new JButton("Convert");
        convertButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        convertButton.setBounds(320, 15, 100, 45);
        convertButton.setFont(new Font("Dialog", Font.BOLD, 16));
        convertButton.setHorizontalAlignment(SwingConstants.CENTER);
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (weatherData != null) {
                    if (convert) {

                        double cTemp = (double) weatherData.get("temperature");
                        temperatureText.setText(cTemp + " °C");
                        double kmh = (double) weatherData.get("windspeed");
                        windspeedText.setText("<html><b>Windspeed</b> " + kmh + "km/h</html>");

                    } else {

                        double cTemp = (double) weatherData.get("temperature");
                        double fTemp = cTemp * 9 / 5 + 32;
                        temperatureText.setText(String.format("%.1f °F", fTemp));
                        double kmh = (double) weatherData.get("windspeed");
                        double mph = kmh / 1.609344;
                        windspeedText.setText("<html><b>Windspeed</b> " + String.format("%.1f", mph) + "mph</html>");

                    }
                    convert = !convert;
                }
            }
        });
        add(convertButton);
    }

    private ImageIcon loadImage(String path){
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find path");
        return null;
    }

    private static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

        return currentDateTime.format(formatter);
    }
}
