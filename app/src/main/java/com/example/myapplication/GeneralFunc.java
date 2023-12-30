package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeneralFunc {
    public static String str2Base64(String string){
        Base64.Encoder encoder=Base64.getEncoder();
        byte[] bytes= encoder.encode(string.getBytes());
        return new String(bytes);
    }

    public static String zipImg2Base64(String imgPath){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            ZipArchiveOutputStream zipArchiveOutputStream=new ZipArchiveOutputStream(byteArrayOutputStream)){
            File file = new File(imgPath);
            ZipArchiveEntry zipArchiveEntry=new ZipArchiveEntry(file.getName());
            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);

            byte[] buffer=new byte[1024];
            try(FileInputStream fileInputStream=new FileInputStream(imgPath)){
                int len;
                while((len=fileInputStream.read(buffer)) > 0){
                    zipArchiveOutputStream.write(buffer,0,len);
                }
            }
            zipArchiveOutputStream.closeArchiveEntry();
            zipArchiveOutputStream.finish();

            byte[] zipBytes=byteArrayOutputStream.toByteArray();
            return str2Base64(new String(zipBytes));
        } catch (IOException e){
            return "";
        }
    }
    public static String defaultImg2Base64(Context context){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream=new ZipOutputStream(byteArrayOutputStream)){
            ZipEntry zipEntry=new ZipEntry("user.png");
            zipOutputStream.putNextEntry(zipEntry);

            Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.user);
            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,byteArrayOutputStream1);
            byte[] bytes=byteArrayOutputStream1.toByteArray();

            zipOutputStream.write(bytes,0,bytes.length);
            zipOutputStream.closeEntry();

            byte[] zipBytes=byteArrayOutputStream.toByteArray();
            return str2Base64(new String(zipBytes));
        } catch (IOException e){
            return "";
        }
    }
}
