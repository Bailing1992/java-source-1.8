package com.lichao.lang.ref.PhantomReference;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperation {
    private FileOutputStream outputStream;
    private FileInputStream inputStream;

    public FileOperation(FileInputStream inputStream, FileOutputStream outputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public void operate() {
        try {
            inputStream.getChannel().transferTo(0, inputStream.getChannel().size(), outputStream.getChannel());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}