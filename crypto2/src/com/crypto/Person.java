package com.crypto;
public abstract class Person {
    // variables - private
    private String kode;
    private String nama;

    // constructor - public
    public Person(String kode, String nama) {
        this.kode = kode;
        this.nama = nama;
    }

    public Person() {
    }

    // setters and getters - public
    public void setKode(String kode) {
        this.kode = kode;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    // abstract, wajib diisi sama subclass
    public abstract String getPeran();
}
