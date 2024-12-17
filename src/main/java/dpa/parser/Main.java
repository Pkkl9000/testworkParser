package dpa.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern TXT_FILE_PATTERN = Pattern.compile(".*\\.txt$", Pattern.CASE_INSENSITIVE);
    private static final List<File> filesToProcess = new ArrayList<>();
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("No arguments provided.");
            return;
        }

        DataTypeSorter dataTypeSorter = new DataTypeSorter();

        // Обработка аргументов main
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a" -> dataTypeSorter.setFlagA(true);
                case "-o" -> {
                    String path = args[++i];
                    if ((i + 1 < args.length) && (path.contains("/") || path.contains("\\"))) {
                        dataTypeSorter.setArgPath(path);
                    } else {
                        System.out.println("Нет значения для флага -o");
                    }
                }
                case "-p" -> {
                    if (i + 1 < args.length) {
                        dataTypeSorter.setFileNamePrefix(args[++i]);
                    } else {
                        System.out.println("Нет значения для флага -p");
                    }
                }
                case "-s" -> dataTypeSorter.setFlagS(true);
                case "-f" -> dataTypeSorter.setFlagF(true);
                default -> checkForTxtFile(args[i]);
            }
        }



        // Обработка файлов
        if (filesToProcess.size() > 0) {
            try {
                dataTypeSorter.processInputFiles(filesToProcess);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Список входящих файлов пуст");
        }
    }

    private static void checkForTxtFile(String fileName) {
        if (TXT_FILE_PATTERN.matcher(fileName).matches()) {
            filesToProcess.add(new File(fileName));
        } else {
            System.out.println("Неверный формат аргумента");
        }
    }


}