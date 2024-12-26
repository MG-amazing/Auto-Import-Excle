package com.example.excel.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileMd5Utils {

    /**
     * 获取 MultipartFile 文件的 16 位 MD5 值
     * 
     * @param file MultipartFile 文件
     * @return 文件的 16 位 MD5 值
     */
    public static String getFileMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192]; // 8K 缓冲区
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : md5Bytes) {
                hexString.append(String.format("%02x", b));
            }
            // 截取前16个字符，得到16位MD5
            return hexString.toString().substring(0, 16);  // 返回16位MD5
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
