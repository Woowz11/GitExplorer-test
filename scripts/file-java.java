package woowzcore.action;

import woowzcore.engine.exception.DirectoryException;
import woowzcore.engine.exception.DirectoryNotFoundException;
import woowzcore.engine.exception.FileException;
import woowzcore.engine.exception.PathException;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

/**
 * Класс содержит в себе функции для работы с Explorer<br>
 * Создание файлов, чтение файлов, запись файлов, удаление файлов, так же с папками, работа с рабочим столом и т.д
 */
public class ExplorerAction {
    private static final ClassLoader CL = ExplorerAction.class.getClassLoader();

    /**
     * Сообщение если файл не найден
     */
    private static final String FILE_NOT_FOUND_MESSAGE   = "Файл не найден!"  ;

    /**
     * Сообщение если папка не найдена
     */
    private static final String FOLDER_NOT_FOUND_MESSAGE = "Папка не найдена!";

    /**
     * Проверяет, существует ли файл по указанному пути
     * @param Path Путь до файла
     * @return "Существует файл по указанному пути?"
     */
    public static boolean HasFile(String Path){
        return Files.exists(Paths.get(Path));
    }

    /**
     * Открывает файл через дефолтное для этого формата приложением
     * @param Path Путь до файла
     * @throws FileException Если файл не найден, или недостаточно прав, или не получилось открыть
     */
    public static void OpenFile(String Path) throws FileException {
        try {
            if (HasFile(Path)) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    try {
                        Desktop.getDesktop().open(new File(Path));
                    } catch (Exception e) {
                        throw new FileException("Не получилось открыть файл на базовом уровне [" + Path + "]!", e);
                    }
                } else {
                    throw new FileException("Не достаточно прав для открытия файла [" + Path + "]!");
                }
            } else {
                throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Не получилось открыть файл [" + Path + "]!", e);
        }
    }

    /**
     * Читает данные из указанного файла
     * @param Path Путь до файла
     * @return Данные внутри файла
     * @throws FileException Если файл не найден, или не получилось прочитать данные из файла
     */
    public static String ReadFile(String Path) throws FileException{
        try {
            Path file = Paths.get(Path);
            if(Files.exists(file)){
                return Files.readString(file);
            }else{
                throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Не получилось прочитать файл [" + Path + "]!", e);
        }
    }

    /**
     * Записывает (заменяет) данные в указанный файл
     * @param Path Путь до файла
     * @param Content Записываемые данные
     * @return Записываемые данные
     * @throws FileException Если файл не найден, или если не получилось записать данные в файл
     */
    public static String WriteFile(String Path, String Content) throws FileException{
        try {
            Path file = Paths.get(Path);
            if(Files.exists(file)){
                Files.writeString(file, Content, StandardCharsets.UTF_8);
                return Content;
            }else{
                throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Не получилось записать данные в файл [" + Path + "]!", e);
        }
    }

    /**
     * Создаёт файл (пустой) по указанному пути
     * @param Path Путь, где создать файл с названием файла и расширением
     * @return Путь до файла
     * @throws FileException Если файл уже существует, или не получилось создать файл
     */
    public static String CreateFile(String Path) throws FileException{
        try {
            File file = new File(Path);

            if (file.createNewFile()){
                return Path;
            } else{
                throw new FileException("Создаваемый файл уже существует!");
            }
        } catch (Exception e) {
            throw new FileException("Произошла ошибка при создании файла [" + Path + "]!", e);
        }
    }

    /**
     * Создаёт файл (с указанными данными) по указанному пути
     * @param Path Путь, где создать файл с названием файла и расширением
     * @param Content Начальные данные для файла
     * @return Путь до файла
     * @throws FileException Если файл уже существует, или не получилось создать файл, или не получилось записать данные
     */
    public static String CreateFile(String Path, String Content) throws FileException {
        String File = CreateFile(Path);
        return WriteFile(File, Content);
    }

    /**
     * Удаляет указанный файл
     * @param Path Путь до файла
     * @throws FileException Если файл не найден, или не получилось удалить указанный файл
     */
    public static void DeleteFile(String Path) throws FileException {
        try {
            Path p = Paths.get(Path);
            if(Files.exists(p)){
                Files.delete(p);
            }else{
                throw new FileException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Произошла ошибка при удалении файла [" + Path + "]!", e);
        }
    }

    /**
     * Создаёт папку (или папки) по указанному пути
     * @param Path Путь содержащий в себе новые папки для создания
     * @return Путь до последней папки
     * @throws DirectoryException Если не получилось создать папку-папки
     */
    public static String CreateFolder(String Path) throws DirectoryException {
        try{
            Files.createDirectories(Paths.get(Path));
            return Path;
        } catch (Exception e) {
            throw new DirectoryException("Произошла ошибка при создании папки-папок по пути [" + Path + "]!", e);
        }
    }

    /**
     * Сжимает файл в формате .gz, и удаляет старый файл
     * @param Path Путь до файла
     * @param NewPath Путь до файла, но файл в формате .gz (в каком месте создать новый файл)
     * @throws FileException Если файл не найден, или не получилось сжать файл, или не получилось удалить старый файл
     */
    public static void CompressFile(String Path, String NewPath) throws FileException {
        try{
            if(HasFile(Path)) {
                try (FileInputStream IN = new FileInputStream(Path)) {
                    GZIPOutputStream OUT = new GZIPOutputStream(new FileOutputStream(NewPath));

                    byte[] Buf = new byte[1024];
                    int L;
                    while ((L = IN.read(Buf)) != -1) {
                        OUT.write(Buf, 0, L);
                    }
                    OUT.finish();
                }
                Files.delete(Paths.get(Path));
            } else {
                throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Произошла ошибка во время сжатия файла [" + Path + "]!", e);
        }
    }

    /**
     * Получает время последней модификации файла
     * @param Path Путь до файла
     * @return Время последней модификации файла в формате long
     * @throws FileException Если файл не найден, или не получилось получить время с последней модификации файла
     */
    public static long GetLastModificationDate(String Path) throws FileException {
        try{
            Path file = Paths.get(Path);
            if (Files.exists(file)) {
                try {
                    FileTime LMT = Files.getLastModifiedTime(file);
                    return LMT.to(TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    throw new FileException("Произошла ошибка при получении времени!", e);
                }
            }else{
                throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            throw new FileException("Не получилось получить время с последней модификации файла [" + Path + "]!", e);
        }
    }

    /**
     * Получает массив всех файлов внутри указанной папки
     * @param Path Путь до папки
     * @param ExtraFolders Так же получить все файлы внутри под папок?
     * @return Возвращает массив путей до файлов
     * @throws FileException Если папка не найдена, или указан файл вместо папки, или не получилось получить все файлы внутри папки
     */
    public static List<String> GetAllFilesInFolder(String Path, boolean ExtraFolders) throws FileException {
        try {
            Path file;

            URL url = CL.getResource(Path);
            if (url == null) {
                file = Paths.get(Path);

                if (!Files.exists(file)) {
                    throw new DirectoryNotFoundException(FOLDER_NOT_FOUND_MESSAGE);
                }
            }else{
                file = Paths.get(url.toURI());
            }

            if(Files.isDirectory(file)){
                try (Stream<java.nio.file.Path> stream = Files.walk(file, ExtraFolders ? Integer.MAX_VALUE : 1)) {
                    return stream
                            .filter(Files::isRegularFile)
                            .map(java.nio.file.Path::toString)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    throw new RuntimeException("Проблема с получением всех файлов в папке", e);
                }
            }else{
                throw new NotDirectoryException("Путь указывает на файл, а не на папку!");
            }
        }catch (Exception e){
            throw new FileException("Не получилось получить все файлы из папки [" + Path +"]", e);
        }
    }

    /**
     * Под класс {@link ExplorerAction}, содержащий в себе работу с путями к файлам<br>
     * Добавление и чтение файлов из путей, исправление путей
     */
    public static class PATH{
        /**
         * Получает название файла по указанному пути
         * @param Path Путь до файла
         * @return Название файла с расширением
         * @see #GetClearFileName(String)
         */
        public static String GetFileName(String Path){
            return Paths.get(Path).getFileName().toString();
        }

        /**
         * Получает только название файла по указанному пути
         * @param Path Путь до файла
         * @return Название файла без расширения
         * @throws PathException Если путь не содержит расширение
         * @see #GetFileName(String) 
         * @see #GetFileExtension(String) 
         */
        public static String GetClearFileName(String Path) throws PathException {
            try{
                String FileName = GetFileName(Path);
                return FileName.substring(0, FileName.length() - GetFileExtension(Path).length() - 1);
            } catch (Exception e) {
                throw new PathException("Не получилось получить чистое название файла [" + Path + "]!", e);
            }
        }

        /**
         * Получает расширение файла по указанному пути
         * @param Path Путь до файла
         * @return Расширение файла (без точки)
         * @throws PathException Если путь не содержит расширение
         */
        public static String GetFileExtension(String Path) throws PathException {
            int LastDotIndex = Path.lastIndexOf('.');
            if(LastDotIndex != -1 && LastDotIndex < Path.length() - 1){
                return Path.substring(LastDotIndex + 1);
            }else{
                throw new PathException("Не получилось получить расширение файла [" + Path + "]! Указанный путь не содержит расширение!");
            }
        }

        /**
         * Исправляет ошибки в пути файла, заменяет <code>\</code> на <code>/</code>,
         * удаляет <code>/</code> из начала и конца, удаляет пробелы из начала и конца,
         * заменяет все неподходящие символы на <code>_</code>
         * @param Path Путь
         * @return Исправленный путь
         */
        public static String FixPath(String Path){
            if(Path == null || Path.isEmpty()) return "";

            String Result = Path;
            boolean HasDisk = Result.charAt(1) == ':';
            /* Убираем все \ (не только одиночные) и заменяем их на / */
            Result = Result.replaceAll("[/\\\\]+", "/");
            /* Убираем все / из начала и конца */
            Result = Result.replaceAll("^[/\\\\]+|[/\\\\]+$", "");
            /* Убираем пробелы из начала и конца */
            Result = Result.trim();
            /* Убираем все неподходящие символы для пути */
            Result  = Result.replaceAll("[<*>?'\":|]","_");

            /* Заменить _ на : если в начале пути указан диск */
            if(HasDisk){
                Result = Result.charAt(0) + ":" + Result.substring(2);
            }
            return Result;
        }

        /**
         * Удаляет из пути, путь до папки с ресурсами игры {@linkplain JAR#Prefix}
         * @param Path Путь до файла
         * @return Путь до файла без папки с ресурсами игры
         */
        public static String ShortResourcePath(String Path){
            String Result = FixPath(Path);
            /* Убрать префикс Resource файла */
            if(Result.startsWith(JAR.Prefix)){
                Result = Result.substring(JAR.Prefix.length());
            }
            return Result;
        }
    }

    /**
     * Под класс {@link ExplorerAction}, содержащий в себе работу с Jar файлами<br>
     * Позволяет читать ресурсы прямиком внутри игры <code>assets/</code> и т.д
     */
    public static class JAR{
        private static final ClassLoader CL = ExplorerAction.JAR.class.getClassLoader();
        /**
         * Префикс ресурсов внутри Jar файла
         */
        public static final String Prefix = "assets/";

        /**
         * Читает данные из указанного файла, внутри Jar
         * @param Path Путь до файла, внутри Jar
         * @return Данные внутри файла
         * @throws FileException Если файл не найден, или не получилось прочитать данные из файла
         */
        public static String ReadFile(String Path) throws FileException {
            Path = Prefix + Path;
            try (InputStream IS = CL.getResourceAsStream(Path)) {
                if (IS != null){
                    Scanner scanner = new Scanner(IS);
                    StringBuilder Result = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        Result.append(scanner.nextLine()).append("\n");
                    }
                    return Result.toString();
                }else{
                    throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
                }
            } catch (Exception e) {
                throw new FileException("Не получилось прочитать файл [" + Path + "] из JAR!", e);
            }
        }

        /**
         * Читает данные из указанного файла, внутри Jar
         * @param Path Путь до файла, внутри Jar
         * @return Данные внутри файла<br>(в виде <code>byte[]</code>)
         * @throws FileException Если файл не найден, или не получилось прочитать данные из файла
         */
        public static byte[] ReadFileBytes(String Path) throws FileNotFoundException {
            Path = Prefix + Path;
            try (InputStream IS = CL.getResourceAsStream(Path)) {
                if (IS != null){
                    ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                    int BytesRead;
                    byte[] DataBuffer = new byte[1024];
                    while ((BytesRead = IS.read(DataBuffer, 0, DataBuffer.length)) != -1) {
                        BAOS.write(DataBuffer, 0, BytesRead);
                    }
                    return BAOS.toByteArray();
                }else{
                    throw new FileNotFoundException(FILE_NOT_FOUND_MESSAGE);
                }
            } catch (Exception e) {
                throw new RuntimeException("Не получилось прочитать файл в виде байтов [" + Path + "] из JAR!", e);
            }
        }

        /**
         * Проверяет, является ли URL - Jar файлом
         * @param url Указанный URL
         * @return "Указанный URL является Jar?"
         */
        public static boolean ThatJAR(URL url){ return "jar".equals(url.getProtocol()); }

        /**
         * Получает массив всех файлов внутри указанной папки, внутри Jar
         * @param Path Путь до папки, внутри Jar
         * @param ExtraFolders Так же получить все файлы внутри под папок?
         * @return Возвращает массив путей до файлов, внутри Jar
         * @throws FileException Если папка не найдена, или не получилось получить доступ к Jar файлу, или не получилось получить все файлы внутри папки
         */
        public static List<String> GetAllFilesInFolder(String Path, boolean ExtraFolders) throws FileException {
            Path = Prefix + Path;
            try {
                URL url = CL.getResource(Path);
                if (url == null) {
                    throw new DirectoryNotFoundException(FOLDER_NOT_FOUND_MESSAGE);
                }

                if(ThatJAR(url)){
                    List<String> result = new ArrayList<>();
                    String jarPath = url.getPath();
                    String[] jarParts = jarPath.split("!");
                    String jarFile = jarParts[0].substring(5);

                    try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + jarFile), Collections.emptyMap())) {
                        Path jarRoot = fs.getPath(jarParts[1]);
                        Files.walkFileTree(jarRoot, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (!attrs.isDirectory()) {
                                    result.add(file.toString());
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (!ExtraFolders && !dir.equals(jarRoot)) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (Exception e) {
                        throw new FileException("Не получилось получить доступ к JAR файлу!", e);
                    }
                    return result;
                }else{
                    List<String> AllFiles = ExplorerAction.GetAllFilesInFolder(Path, ExtraFolders);
                    List<String> Result = new ArrayList<>();
                    String FolderPath = PATH.FixPath(url.getPath()).replace(Path,"");
                    for(String FilePath : AllFiles){
                        FilePath = FilePath.substring(FolderPath.length());
                        Result.add(FilePath);
                    }
                    return Result;
                }
            }catch (Exception e){
                throw new FileException("Не получилось получить все файлы из папки [" + Path +"] из JAR!", e);
            }
        }

        /**
         * Получает массив всех файлов внутри указанной папки, игнорируя под папки, внутри Jar
         * @param Path Путь до папки, внутри Jar
         * @return Возвращает массив путей до файлов, внутри Jar
         * @throws FileException Если папка не найдена, или не получилось получить доступ к Jar файлу, или не получилось получить все файлы внутри папки
         */
        public static List<String> GetAllFilesInFolder(String Path) throws FileException { return GetAllFilesInFolder(Path, false); }
    }
}
