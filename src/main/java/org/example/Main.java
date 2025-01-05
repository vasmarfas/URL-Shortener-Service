package org.example;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import static org.example.UrlHandler.*;

public class Main {
    private static String uuid = "";
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String outText = "";
        System.out.println("\n\n\n");
        if (Objects.equals(uuid, "")) {
            outText = ("""
                Добро пожаловать в сервис сокращения ссылок!
                Отправьте короткую ссылку для перехода на исходный ресурс либо введите номер пункта меню для выбора действия:
                1 -> Сократить ссылку
                2 -> Редактировать параметры ссылки
                3 -> Удалить ссылку
                
                00 -> Войти в систему по UUID
                01 -> Зарегистрироваться в системе
                # -> Завершить программу
                """);
        } else outText = String.format("""
                Добро пожаловать в сервис сокращения ссылок!
                Ваш UUID: %s
                Отправьте короткую ссылку для перехода на исходный ресурс либо введите номер пункта меню для выбора действия:
                1 -> Сократить ссылку
                2 -> Редактировать параметры ссылки
                3 -> Удалить ссылку
                4 -> Отобразить все ссылки, созданные вами
                
                0 -> Выйти из системы
                # -> Завершить программу
                """, uuid);
        System.out.println(outText);
        String userChoice = scanner.nextLine();

        switch (userChoice) {
            //выход
            case "0": {
                uuid = "";
                System.out.println("Вы успешно вышли из системы!");
                break;
            }
            //вход
            case "00": {
                System.out.println("Введите ваш UUID");
                String curuuid = scanner.nextLine();
                DB db = DB.createDB();
                if (db.authUser(curuuid)) {
                    uuid = curuuid;
                    System.out.println("Вы успешно вошли в систему!");
                } else {
                    System.out.println("Вход в систему не удался: неверный UUID");
                }
                break;
            }
            //регистрация
            case "01": {
                registerUser();
                break;
            }
            //добавление
            case "1": {
                if (Objects.equals(uuid, "")) {
                    System.out.println("Для использования данной функции необходимо зарегистрироваться в системе. Регистрация пройдёт в автоматическом режиме.");
                    sleepDelay(500);
                    registerUser();
                }
                shortenUrl(uuid);
                break;
            }
            //изменение
            case "2": {
                if (Objects.equals(uuid, "")) {
                    System.out.println("Для использования данной функции необходимо войти в систему.");
                    sleepDelay(500);
                } else {
                    editUrl(uuid);
                }
                break;

            }
            //удаление
            case "3": {
                if (Objects.equals(uuid, "")) {
                    System.out.println("Для использования данной функции необходимо войти в систему.");
                    sleepDelay(500);
                    break;
                }
                removeUrl(uuid);
                break;
            }
            case "4": {
                if (Objects.equals(uuid, "")) {
                    System.out.println("Для использования данной функции необходимо войти в систему.");
                    sleepDelay(500);
                    break;
                }
                showAllUserLinks(uuid);
                break;
            }
            //завершение
            case "#": {
                System.out.flush();
                System.out.println("Завершение программы.");
                sleepDelay(500);
                System.out.flush();
                System.out.println("Завершение программы..");
                sleepDelay(500);
                System.out.flush();
                System.out.println("Завершение программы...");
                sleepDelay(500);
                System.out.println("Сохранение данных....");
                sleepDelay(500);
                return;

            }
            //открытие ссылки
            default:
                if (userChoice.startsWith("clck.ru/")) {
                    getSourceLink(userChoice);
                } else {
                    System.out.println("Похоже, вы ввели неверную команду, попробуйте ещё раз.");
                }
                break;
        }
        main(args);
    }

    private static void registerUser() {
        String curuuid = "";
        while (true) {
            UUID uuidd = UUID.randomUUID();
            String uuidAsString = uuidd.toString();
            DB db = DB.createDB();
            boolean isAdded = db.addUser(uuidAsString);
            if (isAdded) {
                curuuid = uuidAsString;
                break;
            }
            db.saveToFile();
        }
        System.out.println("Регистрация прошла успешно!\n" +
                "Запомните ваш UUID для возможности последующего входа:\n" +
                curuuid);
        uuid = curuuid;
        sleepDelay(2000);
    }
    private static void sleepDelay(int timeMillis) {
        try {
            Thread.sleep(timeMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}