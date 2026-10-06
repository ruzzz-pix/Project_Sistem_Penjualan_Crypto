package com.crypto;
public class Pembayaran {
    // variables - private
    private String bank;
    private String nomorRekening;
    private String atasNama;

    // constructor - public
    public Pembayaran(String bank, String nomorRekening, String atasNama) {
        this.bank = bank;
        this.nomorRekening = nomorRekening;
        this.atasNama = atasNama;
    }

    public Pembayaran() {
    }

    // setters and getters - public
    public void setBank(String bank) {
        this.bank = bank;
    }

    public void setNomorRekening(String nomorRekening) {
        this.nomorRekening = nomorRekening;
    }

    public void setAtasNama(String atasNama) {
        this.atasNama = atasNama;
    }

    public String getBank() {
        return bank;
    }

    public String getNomorRekening() {
        return nomorRekening;
    }

    public String getAtasNama() {
        return atasNama;
    }
}
