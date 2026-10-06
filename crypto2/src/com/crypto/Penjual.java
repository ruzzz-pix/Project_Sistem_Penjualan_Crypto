package com.crypto;

import java.util.ArrayList;

public class Penjual extends Person implements Login {
    //private variabel
    private String perusahaan;
    private String username;
    private String password;
    private Pembayaran rekening;
    private ArrayList<AsetDigital> katalog;


    //constructor - public
    public Penjual(String kode, String nama, String perusahaan, Pembayaran rekening) {
        super(kode, nama);
        this.perusahaan = perusahaan;
        this.rekening = rekening;
        katalog = new ArrayList<>();
    }

    public Penjual() {
        katalog = new ArrayList<>();
    }

    //setters and getters - public
    public void setPerusahaan(String perusahaan) {
        this.perusahaan = perusahaan;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRekening(Pembayaran rekening) {
        this.rekening = rekening;
    }

    public void addKatalog(AsetDigital aset) {
        katalog.add(aset);
    }

    public String getPerusahaan() {
        return perusahaan;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Pembayaran getRekening() {
        return rekening;
    }

    public ArrayList<AsetDigital> getKatalog() {
        return katalog;
    }

    //cari aset di katalog menggunakan nama, akan mengembalikan nilai null jika tidak di temukan
    public AsetDigital cariAset(String namaAset) {
        for (AsetDigital aset : katalog) {
            if (aset.getNamaAset().equalsIgnoreCase(namaAset)) {
                return aset;
            }
        }
        return null;
    }

    //isi abstract method dari Person
    public String getPeran() {
        return "Penjual";
    }

    // dari interface Login
    public void signUp(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean signIn(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
