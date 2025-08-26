package com.yourcompany.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Launcher extends JFrame {
    private JTextField usernameField;
    private JButton launchButton;
    private JButton tempLaunchButton;
    private JTextArea consoleArea;
    private JProgressBar progressBar;
    private JLabel linkLabel;
    private String minecraftPath;
    private String javaPath;
    private static final String CLIENT_ZIP_URL = "http://92.51.36.57/game/client.zip";

    public Launcher() {
        setTitle("Minecraft Launcher");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String userHome = System.getProperty("user.home");
        minecraftPath = userHome + "/AppData/Roaming/.minecraft-test";

        initComponents();
        layoutComponents();

        loadUsername();
        checkJava();
    }

    private void initComponents() {
        usernameField = new JTextField(15);

        launchButton = new JButton("Запуск");
        launchButton.addActionListener(this::tryLaunchGame);

        tempLaunchButton = new JButton("Временный запуск");
        tempLaunchButton.addActionListener(this::tempLaunchGame);

        consoleArea = new JTextArea(5, 30);
        consoleArea.setEditable(false);
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(Color.GREEN);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        linkLabel = new JLabel("<html><a href=''>Ссылка на репозиторий</a></html>");
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new java.net.URI("https://github.com/eturnercus/test")
                    );
                } catch (Exception ex) {
                    appendToConsole("Ошибка открытия ссылки: " + ex.getMessage() + "\n");
                }
            }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(launchButton);
        buttonPanel.add(tempLaunchButton);
        centerPanel.add(buttonPanel, gbc);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.add(linkLabel);

        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setPreferredSize(new Dimension(400, 120));
        consolePanel.add(new JLabel("Консоль:"), BorderLayout.NORTH);
        consolePanel.add(new JScrollPane(consoleArea), BorderLayout.CENTER);
        consolePanel.add(progressBar, BorderLayout.SOUTH);

        add(linkPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(consolePanel, BorderLayout.SOUTH);
    }

    private void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void loadUsername() {
        File usernameFile = new File(minecraftPath + "/name.txt");
        if (usernameFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usernameFile))) {
                String savedUsername = reader.readLine();
                if (savedUsername != null && !savedUsername.trim().isEmpty()) {
                    usernameField.setText(savedUsername.trim());
                }
            } catch (IOException e) {
                appendToConsole("Ошибка загрузки ника: " + e.getMessage() + "\n");
            }
        }
    }

    private void saveUsername() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            File usernameFile = new File(minecraftPath + "/name.txt");
            try {
                usernameFile.getParentFile().mkdirs();
                try (PrintWriter writer = new PrintWriter(usernameFile)) {
                    writer.println(username);
                }
            } catch (IOException e) {
                appendToConsole("Ошибка сохранения ника: " + e.getMessage() + "\n");
            }
        }
    }

    private void checkJava() {
        appendToConsole("Поиск Java 21...\n");

        List<String> possibleJavaPaths = new ArrayList<>();

        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isEmpty()) {
            possibleJavaPaths.add(javaHome + "/bin/java.exe");
            possibleJavaPaths.add(javaHome + "/bin/javaw.exe");
            appendToConsole("Проверяем JAVA_HOME: " + javaHome + "\n");
        }

        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String userHome = System.getProperty("user.home");

        if (programFiles != null) {
            possibleJavaPaths.add(programFiles + "/Java/jdk-21/bin/java.exe");
            possibleJavaPaths.add(programFiles + "/Java/jdk-21/bin/javaw.exe");
            possibleJavaPaths.add(programFiles + "/Java/jre-21/bin/java.exe");
            possibleJavaPaths.add(programFiles + "/Java/jre-21/bin/javaw.exe");
        }

        if (programFilesX86 != null) {
            possibleJavaPaths.add(programFilesX86 + "/Java/jdk-21/bin/java.exe");
            possibleJavaPaths.add(programFilesX86 + "/Java/jdk-21/bin/javaw.exe");
            possibleJavaPaths.add(programFilesX86 + "/Java/jre-21/bin/java.exe");
            possibleJavaPaths.add(programFilesX86 + "/Java/jre-21/bin/javaw.exe");
        }

        possibleJavaPaths.add(userHome + "/.jdks/jdk-21/bin/java.exe");
        possibleJavaPaths.add(userHome + "/.jdks/jdk-21/bin/javaw.exe");

        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String[] paths = pathEnv.split(File.pathSeparator);
            for (String path : paths) {
                if (path.contains("Java") || path.contains("jdk") || path.contains("jre")) {
                    possibleJavaPaths.add(path + "/java.exe");
                    possibleJavaPaths.add(path + "/javaw.exe");
                }
            }
        }

        for (String path : possibleJavaPaths) {
            File javaFile = new File(path);
            if (javaFile.exists()) {
                appendToConsole("Найдена Java: " + path + "\n");

                if (checkJavaVersion(path)) {
                    javaPath = path;
                    appendToConsole("Подходящая версия Java 21 найдена!\n");
                    return;
                }
            }
        }

        appendToConsole("Java 21 не найдена!\n");
        appendToConsole("Minecraft 1.21.1 требует Java 21 для работы.\n");
        appendToConsole("Пожалуйста, скачайте и установите Java 21 с официального сайта:\n");
        appendToConsole("https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html\n");
        appendToConsole("или\n");
        appendToConsole("https://adoptium.net/temurin/releases/?version=21\n");

        launchButton.setEnabled(false);
        tempLaunchButton.setEnabled(false);

        JOptionPane.showMessageDialog(this,
                "Java 21 не найдена!\n\n" +
                        "Minecraft 1.21.1 требует Java 21 для работы.\n" +
                        "Пожалуйста, скачайте и установите Java 21 с одного из сайтов:\n" +
                        "- https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html\n" +
                        "- https://adoptium.net/temurin/releases/?version=21",
                "Ошибка: Java не найдена",
                JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkJavaVersion(String javaPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(javaPath, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("version \"21") || line.contains("version 21")) {
                    return true;
                }
            }

        } catch (Exception e) {
            appendToConsole("Ошибка проверки версии Java: " + e.getMessage() + "\n");
        }
        return false;
    }

    private UUID generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private void tryLaunchGame(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            appendToConsole("Ошибка: Введите ник!\n");
            return;
        }

        saveUsername();

        if (javaPath == null) {
            appendToConsole("Ошибка: Java 21 не найдена!\n");
            return;
        }

        launchButton.setEnabled(false);
        tempLaunchButton.setEnabled(false);
        consoleArea.setText("");

        new Thread(() -> {
            try {
                File gameDir = new File(minecraftPath);
                File clientZip = new File(minecraftPath + "/client.zip");

                boolean needsUpdate = true;
                if (clientZip.exists()) {
                    try {
                        long localSize = clientZip.length();
                        long serverSize = getRemoteFileSize(CLIENT_ZIP_URL);

                        if (localSize == serverSize && serverSize > 0) {
                            appendToConsole("Локальный файл актуален, пропускаем скачивание\n");
                            needsUpdate = false;
                        } else {
                            appendToConsole("Найдены расхождения, требуется обновление\n");
                        }
                    } catch (IOException ex) {
                        appendToConsole("Не удалось проверить обновление. Используем существующий client.zip.\n");
                        needsUpdate = false;
                    }
                }

                if (needsUpdate) {
                    if (gameDir.exists()) {
                        appendToConsole("Удаляем старую папку...\n");
                        deleteRecursive(gameDir);
                    }

                    gameDir.mkdirs();
                    appendToConsole("Создана папка: " + minecraftPath + "\n");

                    downloadClientWithProgress(CLIENT_ZIP_URL, clientZip);
                    appendToConsole("Распаковка архива...\n");
                    unzipFile(clientZip, gameDir);

                    // Удаляем конфликтующие версии библиотек после распаковки
                    removeConflictLibraries(new File(gameDir, "libraries"));
                }

                appendToConsole("Запуск игры...\n");
                launchMinecraft(gameDir, username);

            } catch (Exception ex) {
                appendToConsole("Ошибка: " + ex.getMessage() + "\n");
                ex.printStackTrace();
                launchButton.setEnabled(true);
                tempLaunchButton.setEnabled(true);
            }
        }).start();
    }

    // Метод для удаления конфликтующих библиотек
    private void removeConflictLibraries(File librariesDir) {
        if (!librariesDir.exists()) return;

        // Удаляем старую версию ASM (9.3), которая конфликтует с новой (9.8)
        File asm93Dir = new File(librariesDir, "org/ow2/asm/asm/9.3");
        if (asm93Dir.exists()) {
            appendToConsole("Удаляем конфликтующую версию ASM: 9.3\n");
            deleteRecursive(asm93Dir);
        }

        // Удаляем старые версии Guava, которые могут конфликтовать
        File guavaDir = new File(librariesDir, "com/google/guava");
        if (guavaDir.exists() && guavaDir.isDirectory()) {
            File[] versions = guavaDir.listFiles();
            if (versions != null) {
                for (File versionDir : versions) {
                    // Оставляем только версию 32.1.2-jre
                    if (!versionDir.getName().equals("32.1.2-jre")) {
                        appendToConsole("Удаляем конфликтующую версию Guava: " + versionDir.getName() + "\n");
                        deleteRecursive(versionDir);
                    }
                }
            }
        }
    }

    private long getRemoteFileSize(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        long size = conn.getContentLengthLong();
        conn.disconnect();
        return size;
    }

    private void tempLaunchGame(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            appendToConsole("Ошибка: Введите ник для временного запуска!\n");
            return;
        }

        if (javaPath == null) {
            appendToConsole("Ошибка: Java 21 не найдена!\n");
            return;
        }

        appendToConsole("Временный запуск с ником: " + username + "\n");

        new Thread(() -> {
            try {
                File gameDir = new File(minecraftPath);

                if (!hasLibraries(gameDir)) {
                    appendToConsole("Библиотеки не найдены. Сначала используйте обычный запуск.\n");
                    return;
                }

                launchMinecraft(gameDir, username);
            } catch (Exception ex) {
                appendToConsole("Ошибка временного запуска: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }).start();
    }

    private boolean hasLibraries(File gameDir) {
        File librariesDir = new File(gameDir, "libraries");
        return librariesDir.exists() && librariesDir.isDirectory();
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursive(f);
                }
            }
        }
        file.delete();
    }

    private void downloadClientWithProgress(String urlStr, File outputFile) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        long serverFileSize = conn.getContentLengthLong();
        appendToConsole("Размер файла на сервере: " + serverFileSize + " байт\n");

        if (outputFile.exists()) {
            long localFileSize = outputFile.length();
            appendToConsole("Размер локального файла: " + localFileSize + " байт\n");

            if (localSize == serverFileSize) {
                appendToConsole("Файл актуален, пропускаем скачивание\n");
                progressBar.setValue(100);
                return;
            } else {
                appendToConsole("Файл устарел, удаляем и скачиваем зановo\n");
                outputFile.delete();
            }
        }

        conn = (HttpURLConnection) url.openConnection();
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long startTime = System.currentTimeMillis();
            long lastUpdateTime = startTime;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > 100) {
                    lastUpdateTime = currentTime;

                    int progress = (int) (totalBytesRead * 100 / serverFileSize);
                    long elapsedTime = currentTime - startTime;
                    double speed = (totalBytesRead / 1024.0) / (elapsedTime / 1000.0);

                    final long currentTotalBytes = totalBytesRead;
                    final int currentProgress = progress;
                    final double currentSpeed = speed;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(currentProgress);
                        appendToConsole(String.format("Скачано: %.1f KB из %.1f KB (%.1f%%) Speed: %.1f KB/s\n",
                                currentTotalBytes / 1024.0, serverFileSize / 1024.0,
                                (double) currentTotalBytes / serverFileSize * 100, currentSpeed));
                    });
                }
            }

            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(100);
                appendToConsole("Скачивание завершено!\n");
            });
        }
    }

    private void unzipFile(File zipFile, File outputDir) throws IOException {
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(outputDir, zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }

        appendToConsole("Распаковка завершена!\n");
    }

    private void launchMinecraft(File gameDir, String username) throws IOException {
        // Создаем assets директорию, если её нет
        File assetsDir = new File(gameDir, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }

        UUID offlineUUID = generateOfflineUUID(username);

        List<String> command = new ArrayList<>();
        command.add(javaPath);
        command.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        command.add("-Xms512m");
        command.add("-Xmx4096m");
        command.add("-Duser.language=en");
        command.add("-Djava.net.preferIPv4Stack=true");
        command.add("-Dfml.earlyprogresswindow=false");

        // Добавляем аргументы для обхода ограничений модульной системы
        command.add("--add-opens");
        command.add("java.base/java.lang=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.lang.invoke=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.lang.reflect=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.io=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.net=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.nio=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.util=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.util.concurrent=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/java.util.concurrent.atomic=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/sun.nio.ch=ALL-UNNAMED");
        command.add("--add-opens");
        command.add("java.base/sun.security.ssl=ALL-UNNAMED");

        // Добавляем natives путь, если существует
        File nativesDir = new File(gameDir, "natives");
        if (nativesDir.exists()) {
            command.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        }

        // Собираем classpath только из библиотек (без клиентского JAR)
        StringBuilder classpath = new StringBuilder();
        collectLibraries(new File(gameDir, "libraries"), classpath);

        // УБИРАЕМ добавление клиентского JAR, так как он уже включен в libraries
        // File clientJar = findClientJar(gameDir);
        // if (clientJar != null) {
        //     if (classpath.length() > 0) {
        //         classpath.append(File.pathSeparator);
        //     }
        //     classpath.append(clientJar.getAbsolutePath());
        // }

        command.add("-cp");
        command.add(classpath.toString());

        // Главный класс как в PolyMC
        command.add("cpw.mods.bootstraplauncher.BootstrapLauncher");

        // Аргументы командной строки как в PolyMC
        command.add("--username");
        command.add(username);
        command.add("--version");
        command.add("1.21.1");
        command.add("--gameDir");
        command.add(gameDir.getAbsolutePath());
        command.add("--assetsDir");
        command.add(assetsDir.getAbsolutePath());
        command.add("--assetIndex");
        command.add("17");
        command.add("--uuid");
        command.add(offlineUUID.toString());
        command.add("--accessToken");
        command.add("offline_token");
        command.add("--userType");
        command.add("offline");
        command.add("--versionType");
        command.add("release");
        command.add("--fml.neoForgeVersion");
        command.add("21.1.203");
        command.add("--fml.fmlVersion");
        command.add("4.0.41");
        command.add("--fml.mcVersion");
        command.add("1.21.1");
        command.add("--fml.neoFormVersion");
        command.add("20240808.144430");
        command.add("--launchTarget");
        command.add("forgeclient");
        command.add("--width");
        command.add("854");
        command.add("--height");
        command.add("480");

        appendToConsole("Запуск Minecraft с Java: " + javaPath + "\n");
        appendToConsole("Используется UUID: " + offlineUUID.toString() + "\n");
        appendToConsole("Главный класс: cpw.mods.bootstraplauncher.BootstrapLauncher\n");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(gameDir);
        pb.inheritIO();

        try {
            Process process = pb.start();

            // Читаем вывод процесса для диагностики
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendToConsole(line + "\n");
            }

            // Читаем ошибки процесса
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                appendToConsole("ERROR: " + line + "\n");
            }

            int exitCode = process.waitFor();
            appendToConsole("Процесс завершился с кодом: " + exitCode + "\n");

            if (exitCode != 0) {
                appendToConsole("Запуск игры завершился с ошибкой\n");
            }

        } catch (Exception ex) {
            appendToConsole("Ошибка при запуске процесса: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }

        dispose();
    }

    private void collectLibraries(File dir, StringBuilder classpath) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectLibraries(file, classpath);
                } else if (file.getName().endsWith(".jar")) {
                    if (classpath.length() > 0) {
                        classpath.append(File.pathSeparator);
                    }
                    classpath.append(file.getAbsolutePath());
                }
            }
        }
    }

    // Этот метод больше не используется, но оставлен на случай необходимости
    private File findClientJar(File gameDir) {
        File[] possiblePaths = {
                new File(gameDir, "libraries/com/mojang/minecraft/1.21.1/minecraft-1.21.1-client.jar"),
                new File(gameDir, "minecraft-client.jar"),
                new File(gameDir, "client.jar"),
                new File(gameDir, "minecraft.jar")
        };

        for (File path : possiblePaths) {
            if (path.exists()) {
                return path;
            }
        }

        File librariesDir = new File(gameDir, "libraries");
        if (librariesDir.exists()) {
            File[] files = librariesDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null && files.length > 0) {
                return files[0];
            }
        }

        return null;
    }

    public static void main(String[] args) {
        // Проверяем версию Java, на которой запущен лаунчер
        String javaVersion = System.getProperty("java.version");
        if (javaVersion != null && !javaVersion.startsWith("21")) {
            JOptionPane.showMessageDialog(null,
                    "Лаунчер должен быть запущен на Java 21!\n" +
                            "Текущая версия Java: " + javaVersion + "\n\n" +
                            "Пожалуйста, скачайте и установите Java 21 с официального сайта:\n" +
                            "https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html\n" +
                            "или\n" +
                            "https://adoptium.net/temurin/releases/?version=21",
                    "Ошибка: Неправильная версия Java",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new Launcher().setVisible(true);
        });
    }
}