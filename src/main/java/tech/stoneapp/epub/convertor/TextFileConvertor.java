package tech.stoneapp.epub.convertor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TextFileConvertor {
    private final ChineseTranslator translator = new ChineseTranslator();

    public TextFileConvertor() {}

    public InputStream convert(InputStream fileStream) {
        String content = new BufferedReader(new InputStreamReader(fileStream))
                .lines().collect(Collectors.joining("\n"));
        String translatedContent = translator.translate(content);

        return new ByteArrayInputStream(translatedContent.getBytes(StandardCharsets.UTF_8));
    }
}