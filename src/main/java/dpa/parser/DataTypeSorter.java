package dpa.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Setter;

@Setter
public class DataTypeSorter {

    // Список поддерживаемых типов данных
    private static final List<Class<?>> supportedTypes = new ArrayList<>();

    private boolean flagA;
    private String fileNamePrefix;
    private String argPath;
    private boolean flagS;
    private boolean flagF;

    // Списки для хранения данных (для статистики)
    private final List<Integer> integers = new ArrayList<>();
    private final List<Float> floats = new ArrayList<>();
    private final List<String> strings = new ArrayList<>();


    static {
        // Инициализация списка поддерживаемых типов
        supportedTypes.add(Integer.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(String.class);
    }

    public void processInputFiles(List<File> fileList) throws IOException {

        // Удаление предыдущих файлов, если "-a" не указан
        if (!flagA) {
            deleteFilesInDirectory();
        }

        for (File file : fileList) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new
                    FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Class<?> dataType = checkDataType(line.trim());
                    if (dataType != null) {
                        writeToFile(dataType, line);
                    }
                }
            }
        }


        if (flagS) {
            System.out.println();
            System.out.println("Краткая статистика:");
            System.out.println("Integer, количество записей: " + integers.size());
            System.out.println("Float, количество записей: " + floats.size());
            System.out.println("String, количество записей: " + strings.size());
            System.out.println();
        }

        if (flagF) {
            showFullStatistics();
        }
    }

    private Class<?> checkDataType(String inputStr) {
        for (Class<?> clazz : supportedTypes) {
            try {
                Constructor<?> constructor = clazz.getConstructor(String.class);
                constructor.newInstance(inputStr);
                return clazz;
            } catch (ReflectiveOperationException | NumberFormatException ignored) {
            }
        }
        return null;
    }

    private void writeToFile(Class<?> dataType, String data) {
        String className = dataType.getSimpleName();
        String currentDir = System.getProperty("user.dir");
        String fileName;

        if (fileNamePrefix != null) {
            fileName = fileNamePrefix + className;
        } else {
            fileName = className;
        }

        if (argPath != null) {
            fileName = argPath + "\\" + fileName;
        }

        File file = new File(currentDir + "\\" + fileName.toLowerCase() + "s.txt");

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                System.out.println("Созданы папки: " + parentDir.getAbsolutePath());
            } else {
                System.err.println("Не удалось создать папки: " + parentDir.getAbsolutePath());
                return;
            }
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Создан файл: " + file.getName());
            } catch (IOException e) {
                System.err.println("Ошибка создания файла " + fileName + ": " + e.getMessage());
                return;
            }
        }

        if (flagS || flagF) {
            switch (className) {
                case "Integer" -> integers.add(Integer.valueOf(data));
                case "Float" -> floats.add(Float.valueOf(data));
                case "String" -> strings.add(data);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + fileName + ": " + e.getMessage());
        }
    }

    private void showFullStatistics() {
        double average = 0.0;

        System.out.println();
        System.out.println("Полная статистика:");
        System.out.println();

        System.out.println("Integer:");
        if (integers.isEmpty()) {
            System.out.println("Значений нет");
        } else {
            Collections.sort(integers);
            System.out.println("Kоличество записей: " + integers.size());
            System.out.println("min: " + integers.get(0));
            System.out.println("max: " + integers.get(integers.size()-1));

            int intSum = integers.stream()
                    .mapToInt(Integer::intValue) // Преобразуем Integer в int
                    .sum();

            average = (double) intSum / integers.size();

            System.out.println("Сумма элементов: " + intSum);
            System.out.println("Среднее значение: " + average);
            System.out.println();
        }

        System.out.println("Float:");
        if (floats.isEmpty()) {
            System.out.println("Значений нет");
        } else {
            Collections.sort(floats);
            System.out.println("Kоличество записей: " + floats.size());
            System.out.println("min: " + floats.get(0));
            System.out.println("max: " + floats.get(floats.size()-1));

            double doubleSum = floats.stream()
                    .mapToDouble(Float::doubleValue)
                    .sum();

            average = (double) doubleSum / floats.size();

            System.out.println("Сумма элементов: " + doubleSum);
            System.out.println("Среднее значение: " + average);
            System.out.println();
        }

        System.out.println("String:");
        if (strings.isEmpty()) {
            System.out.println("Значений нет");
        } else {
            System.out.println("Количество записей: " + strings.size());
            strings.sort(Comparator.comparingInt(String::length));
            System.out.println("Размер самой короткой строки: " + strings.get(0).length());
            System.out.println("Размер самой длинной строки: " + strings.get(strings.size()-1).length());
        }
    }

    private void deleteFilesInDirectory() {
        String currentDir = System.getProperty("user.dir");

        if (argPath != null) {
            currentDir = currentDir + argPath;
        }

        File directory = new File(currentDir);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.endsWith("strings.txt") || fileName.endsWith("floats.txt") || fileName.endsWith("integers.txt")) {
                        if (file.delete()) {
                            System.out.println("Удалён файл: " + file.getAbsolutePath());
                        } else {
                            System.err.println("Не удалось удалить файл: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            System.out.println("Указанный путь не является папкой или пока не существует: " + directory);
        }
    }
}
