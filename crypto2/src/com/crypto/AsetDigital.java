package com.crypto;
public class AsetDigital {
    // variables - private
    private String namaAset;
    private double jumlahAset;
    private double hargaPerUnit;

    // constructor - public
    public AsetDigital(String namaAset, double jumlahAset, double hargaPerUnit) {
        this.namaAset = namaAset;
        this.jumlahAset = jumlahAset;
        this.hargaPerUnit = hargaPerUnit;
    }

    public AsetDigital() {
    }

    // setters and getters - public
    public void setNamaAset(String namaAset) {
        this.namaAset = namaAset;
    }

    public void setJumlahAset(double jumlahAset) {
        this.jumlahAset = jumlahAset;
    }

    public void setHargaPerUnit(double hargaPerUnit) {
        this.hargaPerUnit = hargaPerUnit;
    }

    public String getNamaAset() {
        return namaAset;
    }

    public double getJumlahAset() {
        return jumlahAset;
    }

    public double getHargaPerUnit() {
        return hargaPerUnit;
    }

    public double hitungNilaiAset() {
        return jumlahAset * hargaPerUnit;
    }
}
