package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GeneralFunc {
    //string format mặc định là uft-8

    //Chuyển string thành base 64 string (lưu ý: nếu chuyển byte[] thành string rồi đưa vào hàm này
    // thì khi chuyển lại base 64 thành string sẽ có tỉ lệ sai)
    public static String str2Base64(String string){
        Base64.Encoder encoder=Base64.getEncoder();
        byte[] bytes= encoder.encode(string.getBytes());
        return new String(bytes);
    }

    //Chuyển base 64 thành string
    public static String base64ToStr(String string){
        Base64.Decoder decoder=Base64.getDecoder();
        byte[] bytes= decoder.decode(string.getBytes());
        return new String(bytes);
    }

    //Nén img thành base 64 string với input là đường dẫn tới img
    public static String zipImg2Base64(String imgPath){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(byteArrayOutputStream)){

            Bitmap bitmap= BitmapFactory.decodeFile(imgPath);
            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80,byteArrayOutputStream1);
            byte[] bytes=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            deflaterOutputStream.write(bytes);
            deflaterOutputStream.close();

            String ret= new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            return ret;
        } catch (IOException e){
            return "";
        }
    }

    //Nén ảnh đại diện mặc định thành base 64
    public static String defaultImg2Base64(Context context){
        try(ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream=new
                    DeflaterOutputStream(byteArrayOutputStream)){

            Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.user);
            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0,byteArrayOutputStream1);
            byte[] bytes=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            deflaterOutputStream.write(bytes);
            deflaterOutputStream.close();

            String ret= new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            return ret;
        } catch (IOException e){
            return "";
        }
    }

    //Giải nén từ base 64
    public static Bitmap unzipBase64ToImg(String string){
        byte[] bytes=Base64.getDecoder().decode(string.getBytes());
        try(ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
                InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream)){

            ByteArrayOutputStream byteArrayOutputStream1=new ByteArrayOutputStream();
            int read;
            while ((read = inflaterInputStream.read()) != -1) {
                byteArrayOutputStream1.write(read);
            }

            inflaterInputStream.close();
            byteArrayInputStream.close();
            byte[] bytes1=byteArrayOutputStream1.toByteArray();
            byteArrayOutputStream1.close();

            return BitmapFactory.decodeByteArray(bytes1,0,bytes1.length);
        } catch (IOException e){
            return null;
        }
    }

}
