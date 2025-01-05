package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties properties;

    public Config() {
        String filename = "config.properties";
        properties = new Properties();
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);  // Загружаем данные из конфигурационного файла
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Получаем значение по ключу
    public String get(String key) {
        return properties.getProperty(key);
    }

}