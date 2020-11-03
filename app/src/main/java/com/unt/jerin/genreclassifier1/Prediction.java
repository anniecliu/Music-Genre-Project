package com.unt.jerin.genreclassifier1;

public class Prediction {
    private String filename;
    private String predictedGenre;

    public Prediction(){

    }

    public Prediction(String filename, String predictedGenre) {
        this.filename = filename;
        this.predictedGenre = predictedGenre;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "filename='" + filename + '\'' +
                ", predictedGenre='" + predictedGenre + '\'' +
                '}';
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPredictedGenre() {
        return predictedGenre;
    }

    public void setPredictedGenre(String predictedGenre) {
        this.predictedGenre = predictedGenre;
    }
}
