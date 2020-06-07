package com.elenakuropatkina.my.cloud.common;

public class FileMessage extends AbstractMessage {
    private String filename;
    private int partNumber;
    private int quantityOfParts;
    private byte[] data;

//    public FileMessage(Path path) throws IOException {
//        filename = path.getFileName().toString();
//        data = Files.readAllBytes(path);
//    }

    public FileMessage(String filename, int quantityOfParts, int partNumber, byte[] data){
        this.filename = filename;
        this.quantityOfParts = quantityOfParts;
        this.partNumber = partNumber;
        this.data = data;
    }

    public String getFilename() {

        return filename;
    }

    public byte[] getData() {

        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public int getQuantityOfParts() {
        return quantityOfParts;
    }




}

