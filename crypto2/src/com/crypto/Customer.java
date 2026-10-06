package com.crypto;
public class Customer extends Person {
    // variables - private
    private String alamatWallet;

    // constructor - public
    public Customer(String kode, String nama, String alamatWallet) {
        super(kode, nama);
        this.alamatWallet = alamatWallet;
    }

    public Customer() {
    }

    // setters and getters - public
    public void setAlamatWallet(String alamatWallet) {
        this.alamatWallet = alamatWallet;
    }

    public String getAlamatWallet() {
        return alamatWallet;
    }

    // isi abstract method dari Person
    public String getPeran() {
        return "Customer";
    }
}
