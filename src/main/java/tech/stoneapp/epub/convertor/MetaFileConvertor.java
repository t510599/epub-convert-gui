package tech.stoneapp.epub.convertor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class MetaFileConvertor extends TextFileConvertor {
    @Override
    public InputStream convert(InputStream fileStream) {
        String content = new BufferedReader(new InputStreamReader(fileStream))
                .lines().collect(Collectors.joining("\n"));
        String translatedContent = translator.translate(content);

        // I am too lazy too parse metadata, so just replace directly.
        translatedContent = translatedContent.replaceAll(
                "<dc:language>([^<]*)</dc:language>",
                "<dc:language>zh-TW</dc:language>"
        );

        return new ByteArrayInputStream(translatedContent.getBytes(StandardCharsets.UTF_8));
    }
}