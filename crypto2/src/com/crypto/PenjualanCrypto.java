









package com.crypto;

import java.util.ArrayList;
import java.util.Locale;

public class PenjualanCrypto implements Cetak {
    //status transaksi
    public static final String MENUNGGU_BAYAR = "MENUNGGU_BAYAR";
    public static final String MENUNGGU_VERIFIKASI = "MENUNGGU_VERIFIKASI";
    public static final String DIPROSES = "DIPROSES";
    public static final String SELESAI = "SELESAI";

    //tarif ini asumsi kami, bisa diganti kalau ada ketentuan lain
    private static final double TARIF_LAYANAN = 0.01;        
    private static final double BIAYA_TEKNIS_TETAP = 5000;   
    private static final double TARIF_PPH = 0.001;           
    private static final Locale LOKAL = Locale.forLanguageTag("id-ID");

    // variables - private
    private String invoiceNo;
    private String tanggalWaktu;
    private String status;
    private String buktiBayar;
    private String buktiKirim;
    private ArrayList<AsetDigital> listAset;
    private Penjual penjual;
    private Customer customer;
    private double biayaLayanan;
    private double biayaTeknis;
    private double pph;

    // constructor - public
    public PenjualanCrypto(String invoiceNo, String tanggalWaktu, Penjual penjual, Customer customer) {
        this.invoiceNo = invoiceNo;
        this.tanggalWaktu = tanggalWaktu;
        this.penjual = penjual;
        this.customer = customer;
        listAset = new ArrayList<>();
        status = MENUNGGU_BAYAR;
        buktiBayar = "-";
        buktiKirim = "-";
        biayaTeknis = BIAYA_TEKNIS_TETAP;
    }

    public PenjualanCrypto() {
        listAset = new ArrayList<>();
        status = MENUNGGU_BAYAR;
        buktiBayar = "-";
        buktiKirim = "-";
        biayaTeknis = BIAYA_TEKNIS_TETAP;
    }

    // setters - public
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public void setTanggalWaktu(String tanggalWaktu) {
        this.tanggalWaktu = tanggalWaktu;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBuktiBayar(String buktiBayar) {
        this.buktiBayar = buktiBayar;
    }

    public void setBuktiKirim(String buktiKirim) {
        this.buktiKirim = buktiKirim;
    }

    public void setPenjual(Penjual penjual) {
        this.penjual = penjual;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void addAset(AsetDigital aset) {
        listAset.add(aset);
    }

    // getters - public
    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getTanggalWaktu() {
        return tanggalWaktu;
    }

    public String getStatus() {
        return status;
    }

    public String getBuktiBayar() {
        return buktiBayar;
    }

    public String getBuktiKirim() {
        return buktiKirim;
    }

    public Penjual getPenjual() {
        return penjual;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ArrayList<AsetDigital> getListAset() {
        return listAset;
    }

    // biaya baru terisi setelah hitungTotalPembayaran() dipanggil
    public double getBiayaLayanan() {
        return biayaLayanan;
    }

    public double getBiayaTeknis() {
        return biayaTeknis;
    }

    public double getPph() {
        return pph;
    }

    // hitung total
    public double hitungTotalAset() {
        double total = 0;
        for (AsetDigital aset : listAset) {
            total += aset.hitungNilaiAset();
        }
        return total;
    }

    public double hitungTotalPembayaran() {
        double totalAset = hitungTotalAset();
        biayaLayanan = totalAset * TARIF_LAYANAN;
        pph = totalAset * TARIF_PPH;
        return totalAset + biayaLayanan + biayaTeknis + pph;
    }

    //proses pembelian
    //customer kirim ID transaksi bayar
    public void kirimBuktiBayar(String bukti) {
        buktiBayar = bukti;
        status = MENUNGGU_VERIFIKASI;
    }

    //admin cek nominal yang masuk ke rekening, valid kalau sama dengan total
    public boolean verifikasiPembayaran(double nominalMasuk) {
        if (Math.round(nominalMasuk) == Math.round(hitungTotalPembayaran())) {
            status = DIPROSES;
            return true;
        }
        status = MENUNGGU_BAYAR;   //ditolak, customer harus kirim ulang
        buktiBayar = "-";
        return false;
    }

    //admin kirim aset dan catat bukti pengirimannya
    public void kirimAset(String bukti) {
        buktiKirim = bukti;
        status = SELESAI;
    }

    //cetak
    //tagihan + rekening tujuan transfer
    public void tampilkanTagihan() {
        double total = hitungTotalPembayaran();
        String garis = "=".repeat(52);
        String putus = "-".repeat(52);
        System.out.println(garis);
        System.out.println("TAGIHAN " + invoiceNo + " (" + status + ")");
        System.out.println(putus);
        cetakRincian(total);
        System.out.println(putus);
        System.out.println("Transfer ke : " + penjual.getRekening().getBank() + " "
                + penjual.getRekening().getNomorRekening());
        System.out.println("Atas nama   : " + penjual.getRekening().getAtasNama());
        System.out.println("Nominal     : " + rupiah(total));
        System.out.println(garis);
    }

    //dari interface Cetak, ini invoice akhirnya
    public void struk() {
        double total = hitungTotalPembayaran();
        String garis = "=".repeat(52);
        String putus = "-".repeat(52);
        System.out.println(garis);
        System.out.println(TOKO);
        System.out.println(ALAMAT);
        System.out.println(garis);
        System.out.println("Invoice  : " + invoiceNo);
        System.out.println("Tanggal  : " + tanggalWaktu);
        System.out.println("Penjual  : " + penjual.getNama() + " (" + penjual.getPerusahaan() + ")");
        System.out.println("Customer : " + customer.getNama());
        System.out.println("Wallet   : " + customer.getAlamatWallet());
        System.out.println(putus);
        cetakRincian(total);
        System.out.println(putus);
        System.out.println("ID transaksi bayar : " + buktiBayar);
        System.out.println("Hash pengiriman    : " + buktiKirim);
        System.out.println("Status             : " + status);
        System.out.println(garis);
    }

    private void cetakRincian(double total) {
        for (AsetDigital aset : listAset) {
            System.out.println(aset.getNamaAset() + "  " + angka(aset.getJumlahAset())
                    + " x " + rupiah(aset.getHargaPerUnit())
                    + " = " + rupiah(aset.hitungNilaiAset()));
        }
        System.out.println("-".repeat(52));
        System.out.printf("%-20s %s%n", "Total Aset", rupiah(hitungTotalAset()));
        System.out.printf("%-20s %s%n", "Biaya Layanan", rupiah(biayaLayanan));
        System.out.printf("%-20s %s%n", "Biaya Teknis", rupiah(biayaTeknis));
        System.out.printf("%-20s %s%n", "PPh", rupiah(pph));
        System.out.printf("%-20s %s%n", "TOTAL BAYAR", rupiah(total));
    }

    // format tampilan
    private String rupiah(double nilai) {
        return String.format(LOKAL, "Rp%,.0f", nilai);
    }

    private String angka(double nilai) {
        return String.format(LOKAL, "%,.8f", nilai).replaceAll(",?0+$", "");
    }
}
