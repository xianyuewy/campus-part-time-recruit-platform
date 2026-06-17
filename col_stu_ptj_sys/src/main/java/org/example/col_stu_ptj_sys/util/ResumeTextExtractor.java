package org.example.col_stu_ptj_sys.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResumeTextExtractor {

    /**
     * 从简历文件中提取纯文本
     */
    public static String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (fileName.endsWith(".pdf")) {
            return extractPdfText(file.getInputStream());
        } else if (fileName.endsWith(".docx")) {
            return extractWordText(file.getInputStream());
        } else if (fileName.endsWith(".doc")) {
            // 旧版doc兼容，简单场景可直接提示用户用docx/pdf
            throw new IllegalArgumentException("暂支持 .docx 和 .pdf 格式简历");
        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }

    /**
     * 提取PDF文本
     */
    private static String extractPdfText(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 提取Word(docx)文本
     */
    private static String extractWordText(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
    // ========== 异步场景专用 ==========
    public static String extractFromBytes(byte[] fileBytes, String fileName) throws IOException {
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(fileBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (fileName.endsWith(".docx")) {
            try (InputStream is = new ByteArrayInputStream(fileBytes);
                 XWPFDocument document = new XWPFDocument(is);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        } else {
            throw new IllegalArgumentException("仅支持 .pdf 和 .docx 格式");
        }
    }
}