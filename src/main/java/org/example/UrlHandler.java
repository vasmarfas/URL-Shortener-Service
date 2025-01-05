package org.example;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class UrlHandler {
    private static final RandomStringGenerator generator = new RandomStringGenerator();
    static Scanner scanner = new Scanner(System.in);
    static Config config = new Config();

    public static void getSourceLink(String shortLink) {
        String urlID = shortLink.replace("clck.ru/", "");
        DB db = DB.createDB();
        UserLink curLink = db.getLinkByID(urlID);
        if (curLink == null) {
            System.out.println("Ссылка не найдена! Убедитесь в правильности ввода и попробуйте снова.");
        } else {
            if (curLink.validUntil > 0) {
                if (curLink.validUntil < System.currentTimeMillis()) {
                    System.out.println("У данной ссылки истёк срок действия, ссылка будет удалена.");
                    db.removeLinkByID(curLink.id);
                    return;
                }
            }
            if (curLink.usageLimit > 0) {
                if (curLink.usages >= curLink.usageLimit) {
                    System.out.println("У данной ссылки превышен лимит переходов, ссылка будет удалена.");
                    db.removeLinkByID(curLink.id);
                    return;
                }
            }
            try {
                Desktop.getDesktop().browse(new URI(curLink.sourceURL));
                curLink.usages = curLink.usages + 1;
                db.updateLink(curLink);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
//            System.out.println(curLink.sourceURL);
        }
    }

    public static void shortenUrl(String userUuid) {
        String defaultValidMinutes = config.get("DEFAULT_VALID_MINUTES");
        String defaultUsageLimit = config.get("DEFAULT_USAGE_LIMIT");

        System.out.println("Введите ссылку для сокращения:");
        String sourceUrl = scanner.nextLine();
        System.out.println("Введите срок действия ссылки в минутах либо введите 0 для того, чтобы ссылка была бессрочной.\n" +
                "Оставьте пустым для использования значения по умолчанию (" + defaultValidMinutes + " минут):");
        String minutesString = scanner.nextLine();
        long validUntil = Long.parseLong(defaultValidMinutes)*60*1000 + System.currentTimeMillis();
        if (Objects.equals(minutesString, "0")) validUntil = 0;
        if (!Objects.equals(minutesString, "")) {
            try {
                int minutes = Integer.parseInt(minutesString);
                long timeMillis = System.currentTimeMillis();

                validUntil = timeMillis + (minutes * 60 * 1000L);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка! Ожидается целое положительное число, операция прервана. Попробуйте снова");
                return;
            }
        }
        System.out.println("Введите лимит использований (раз) ссылки либо введите 0 для того, чтобы ссылка была доступна без ограничений по количеству использований.\n" +
                "Оставьте пустым для использования значения по умолчанию (" + defaultUsageLimit + " раз):");
        String usageString = scanner.nextLine();
        int usageLimit = Integer.parseInt(defaultUsageLimit);
        if (Objects.equals(usageString, "0")) usageLimit = 0;
        if (!Objects.equals(usageString, "")) {
            try {
                usageLimit = Integer.parseInt(usageString);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка! Ожидается целое положительное число, операция прервана. Попробуйте снова");
                return;
            }
        }
        DB db = DB.createDB();
        String urlID = "";
        String shortUrl = "";
        do {
            urlID = generator.randomStringGenerator();
            shortUrl = "clck.ru/" + urlID;
        } while (!db.checkLinkAvailability(urlID));
        UserLink linkToAdd = new UserLink(urlID, userUuid, sourceUrl, shortUrl, usageLimit, 0, validUntil);
        db.addLink(linkToAdd);
        System.out.println("Ваша ссылка успешно добавлена! \nСокращённая ссылка: " + shortUrl);

    }

    public static void removeUrl(String userUuid) {
        System.out.println("Введите ссылку для удаления:");
        String shortUrl = scanner.nextLine();
        String urlID = shortUrl.replace("clck.ru/", "");
        DB db = DB.createDB();
        UserLink link = db.getLinkByID(urlID);
        if (link == null) {
            System.out.println("Такой ссылки не существует, операция прервана.");
            return;
        } else {
            if (!Objects.equals(link.creatorUserUUID, userUuid)) {
                System.out.println("Данная ссылка создана другим пользователем, удаление невозможно.");
            } else {
                boolean isRemoved = db.removeLinkByID(urlID);
                if (isRemoved) {
                    System.out.println("Ссылка " + shortUrl + " успешно удалена");
                } else System.out.println("Не удалось удалить ссылку. Обратитесь в поддержку сервиса.");
            }
        }

    }

    public static void editUrl(String userUuid) {
        System.out.println("Введите ссылку для удаления:");
        String shortUrl = scanner.nextLine();
        String urlID = shortUrl.replace("clck.ru/", "");
        DB db = DB.createDB();
        UserLink link = db.getLinkByID(urlID);
        if (link == null) {
            System.out.println("Такой ссылки не существует, будет операция прервана.");
            return;
        } else {
            if (!Objects.equals(link.creatorUserUUID, userUuid)) {
                System.out.println("Данная ссылка создана другим пользователем, удаление невозможно.");
            } else {
                System.out.println("""
                        Ссылка найдена, что вы хотите изменить? Введите соответствующий номер меню:
                        1 -> Лимит количества использований
                        2 -> Время жизни ссылки""");
                String userChoice = scanner.nextLine();
                switch (userChoice) {
                    case "1": {
                        System.out.println("Текущий лимит количества использований ссылки: "+ link.usageLimit + ".\n" +
                                "Введите новое значение (0 - безлимит), либо отправьте пустое поле для того, чтобы оставить без изменений ");
                        String newUsages = scanner.nextLine();
                        switch (newUsages) {
                            case "": {
                                System.out.println("Лимит количества использований не был изменен.");
                                return;
                            }
                            case "0": {
                                link.usageLimit = 0;
                                db.updateLink(link);
                                System.out.println("Лимит количества использований успешно удалён, теперь ссылка безлимитная.");
                                return;
                            }
                            default: {
                                int newLimit;
                                try {
                                    newLimit = Integer.parseInt(newUsages);
                                } catch (NumberFormatException e) {
                                    System.out.println("Ошибка! Ожидается целое положительное число, операция прервана. Попробуйте снова");
                                    return;
                                }
                                link.usageLimit = newLimit;
                                db.updateLink(link);
                                System.out.println("Лимит количества использований успешно изменен, текущий лимит: " + link.usageLimit + ", текущее количество использований: " + link.usages + ".");
                                return;
                            }
                        }
                    }

                    case "2": {
                        Date currentDate = new Date(link.validUntil);
                        long curTimeMs = System.currentTimeMillis();
                        System.out.println("Текущее время жизни ссылки: до " + currentDate + ".\n" +
                                "Введите новое значение в минутах от текущего времени (0 - безлимит), либо отправьте пустое поле для того, чтобы оставить без изменений ");
                        String newMinutes = scanner.nextLine();
                        switch (newMinutes) {
                            case "": {
                                System.out.println("Время жизни ссылки не было изменено.");
                                return;
                            }
                            case "0": {
                                link.validUntil = 0;
                                db.updateLink(link);
                                System.out.println("Время жизни ссылки успешно сброшено, теперь ссылка бессрочная.");
                                return;
                            }
                            default: {
                                long newValidUntil;
                                try {
                                    newValidUntil = Long.parseLong(newMinutes);
                                } catch (NumberFormatException e) {
                                    System.out.println("Ошибка! Ожидается целое положительное число, операция прервана. Попробуйте снова");
                                    return;
                                }
                                link.validUntil = newValidUntil;
                                db.updateLink(link);
                                Date newDate = new Date(link.validUntil);
                                System.out.println("Время жизни ссылки успешно изменено, текущее время жизни ссылки: до " + newDate + ".");
                                return;
                            }
                        }
                    }

                    default: {
                        System.out.println("Похоже, вы ввели неверный номер пункта меню. Операция будет прервана");
                        return;
                    }
                }

            }

        }
    }

    public static void showAllUserLinks(String userUuid) {
        DB db = DB.createDB();
        ArrayList<UserLink> linkList = db.getAllUserLinks(userUuid);
        if (linkList.isEmpty()) {
            System.out.println("В базе данных не найдено ни одной вашей ссылки.");
        } else {
            for (UserLink link : linkList) {
                Date currentDate = new Date(link.validUntil);
                String outText ="-------------------------------------------------------------------------------------\n" +
                        "ID: " + link.id + "\nСсылка: " + link.shortURL + "\nИсходный ресурс: " + link.sourceURL +
                        "\nКоличество использований: " + link.usages + "\nЛимит количества использований: " + link.usageLimit +
                        "\nСрок жизни: до " + currentDate + ", (timestamp: " + link.validUntil + " ms)" + "\n" +
                        "-------------------------------------------------------------------------------------\n";
                System.out.println(outText);
            }
        }
    }

}

