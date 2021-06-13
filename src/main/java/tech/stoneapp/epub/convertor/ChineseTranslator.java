package tech.stoneapp.epub.convertor;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.util.stream.Collectors;

public class ChineseTranslator {
    public String translate(String content) {
        return content.lines().map(ZhConverterUtil::toTraditional).collect(Collectors.joining("\n"));
    }
}