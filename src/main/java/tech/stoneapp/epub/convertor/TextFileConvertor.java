package tech.stoneapp.epub.convertor;


import java.io.*;
import java.util.stream.Collectors;

public class TextFileConvertor {
    private final ChineseTranslator translator = new ChineseTranslator();

    public TextFileConvertor() {}

    public ByteArrayOutputStream convert(InputStream fileStream) {
        String content = new BufferedReader(new InputStreamReader(fileStream))
                .lines().collect(Collectors.joining("\n"));
        String translatedContent = translator.translate(content);
        return new ByteArrayOutputStream();
    }
}
