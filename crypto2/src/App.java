import com.crypto.AsetDigital;
import com.crypto.Customer;
import com.crypto.Pembayaran;
import com.crypto.Penjual;
import com.crypto.PenjualanCrypto;
import com.crypto.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class App {
    private static final String FOLDER = "data";
    private static final String FILE_PENJUAL = "data/penjual.txt";
    private static final String FILE_ASET = "data/aset.txt";
    private static final String FILE_TRANSAKSI = "data/transaksi.txt";
    private static final Locale LOKAL = Locale.forLanguageTag("id-ID");
    private static final Scanner input = new Scanner(System.in);

    private static Penjual penjual;
    private static ArrayList<Customer> daftarCustomer = new ArrayList<>();
    private static ArrayList<PenjualanCrypto> daftarTransaksi = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== SISTEM PENJUALAN CRYPTO ===");
        muatPenjual();
        muatKatalog();
        muatTransaksi();

        boolean selesai = false;
        while (!selesai) {
            System.out.println();
            System.out.println("1. Pesan aset (customer)");
            System.out.println("2. Cek pesanan / kirim ID transaksi bayar");
            System.out.println("3. Login admin");
            System.out.println("4. Keluar");
            int pilihan = bacaInt("Masukkan pilihan (1 - 4): ");

            switch (pilihan) {
                case 1:
                    pesanAset();
                    break;
                case 2:
                    cekPesanan();
                    break;
                case 3:
                    loginAdmin();
                    break;
                case 4:
                    selesai = true;
                    break;
                default:
                    System.out.println("Pilihan harus 1 sampai 4, coba lagi!");
                    break;
            }
        }
        System.out.println("Terima kasih.");
        input.close();
    }

    //customer
    private static void pesanAset() {
        if (penjual.getKatalog().isEmpty()) {
            System.out.println("Belum ada aset yang dijual. Admin perlu mengisi katalog dahulu.");
            return;
        }
        System.out.println("\n--- Data Customer ---");
        String nama = bacaTeks("Nama customer : ");
        String wallet = bacaTeks("Alamat wallet : ");
        Customer customer = cariCustomer(nama, wallet);
        if (customer == null) {
            customer = new Customer("CST-" + String.format("%03d", daftarCustomer.size() + 1), nama, wallet);
            daftarCustomer.add(customer);
        }

        String invoice = "INV-" + String.format("%03d", daftarTransaksi.size() + 1);
        String waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        PenjualanCrypto transaksi = new PenjualanCrypto(invoice, waktu, penjual, customer);

        boolean tambah = true;
        while (tambah) {
            tampilkanKatalog();
            AsetDigital dipilih = pilihAset(transaksi);
            if (dipilih != null) {
                double sisa = dipilih.getJumlahAset() - jumlahDipesan(transaksi, dipilih.getNamaAset());
                double jumlah = bacaJumlah("Jumlah yang dibeli : ", sisa);
                transaksi.addAset(new AsetDigital(dipilih.getNamaAset(), jumlah, dipilih.getHargaPerUnit()));
            }
            if (transaksi.getListAset().isEmpty()) {
                System.out.println("Pilih minimal satu aset.");
            } else {
                tambah = bacaYaTidak("Tambah aset lagi? (y/n): ");
            }
        }

        daftarTransaksi.add(transaksi);
        simpanTransaksi();
        System.out.println("\nPesanan dibuat. Nomor invoice Anda: " + invoice + " (catat untuk cek status).");
        System.out.println("Silakan transfer sesuai tagihan berikut.\n");
        transaksi.tampilkanTagihan();
        if (bacaYaTidak("Sudah transfer dan ingin mengirim ID transaksi sekarang? (y/n): ")) {
            kirimBuktiBayar(transaksi);
        } else {
            System.out.println("Nanti pilih menu 2 dan masukkan nomor invoice " + invoice + ".");
        }
    }

    private static void cekPesanan() {
        System.out.println("\n--- Cek Pesanan ---");
        String invoice = bacaTeks("Nomor invoice (contoh INV-001): ").toUpperCase();
        PenjualanCrypto t = null;
        for (PenjualanCrypto x : daftarTransaksi) {
            if (x.getInvoiceNo().equals(invoice)) {
                t = x;
            }
        }
        if (t == null) {
            System.out.println("Invoice tidak ditemukan.");
            return;
        }
        String status = t.getStatus();
        if (status.equals(PenjualanCrypto.MENUNGGU_BAYAR)) {
            t.tampilkanTagihan();
            if (bacaYaTidak("Sudah transfer dan ingin mengirim ID transaksi sekarang? (y/n): ")) {
                kirimBuktiBayar(t);
            }
        } else if (status.equals(PenjualanCrypto.MENUNGGU_VERIFIKASI)) {
            System.out.println("ID transaksi sudah terkirim: " + t.getBuktiBayar());
            System.out.println("Menunggu verifikasi admin.");
        } else if (status.equals(PenjualanCrypto.DIPROSES)) {
            System.out.println("Pembayaran sudah diverifikasi. Aset sedang dikirim admin.");
        } else {
            t.struk();
        }
    }

    private static void kirimBuktiBayar(PenjualanCrypto t) {
        String bukti = bacaTeks("ID transaksi transfer (nomor referensi dari bank): ");
        t.kirimBuktiBayar(bukti);
        simpanTransaksi();
        System.out.println("ID transaksi terkirim. Menunggu verifikasi admin.");
    }

    private static void tampilkanKatalog() {
        System.out.println("\nAset yang dijual:");
        ArrayList<AsetDigital> katalog = penjual.getKatalog();
        for (int i = 0; i < katalog.size(); i++) {
            AsetDigital a = katalog.get(i);
            System.out.println((i + 1) + ". " + a.getNamaAset() + " | harga " + rupiah(a.getHargaPerUnit())
                    + " | stok " + angka(a.getJumlahAset()));
        }
    }

    //customer pilih nomor aset dari katalog, akan mengembalikan null kalau stok habis atau nomor salah
    private static AsetDigital pilihAset(PenjualanCrypto transaksi) {
        ArrayList<AsetDigital> katalog = penjual.getKatalog();
        int nomor = bacaInt("Pilih nomor aset : ");
        if (nomor < 1 || nomor > katalog.size()) {
            System.out.println("Nomor tidak ada, coba lagi!");
            return null;
        }
        AsetDigital a = katalog.get(nomor - 1);
        if (a.getJumlahAset() - jumlahDipesan(transaksi, a.getNamaAset()) <= 0) {
            System.out.println("Stok " + a.getNamaAset() + " habis.");
            return null;
        }
        return a;
    }

    private static double jumlahDipesan(PenjualanCrypto transaksi, String namaAset) {
        double total = 0;
        for (AsetDigital a : transaksi.getListAset()) {
            if (a.getNamaAset().equalsIgnoreCase(namaAset)) {
                total += a.getJumlahAset();
            }
        }
        return total;
    }

    private static Customer cariCustomer(String nama, String wallet) {
        for (Customer c : daftarCustomer) {
            if (c.getNama().equalsIgnoreCase(nama) && c.getAlamatWallet().equals(wallet)) {
                return c;
            }
        }
        return null;
    }

    //admin
    private static void loginAdmin() {
        System.out.println("\n--- Login Admin ---");
        String username = bacaTeks("Username : ");
        String password = bacaTeks("Password : ");
        if (penjual.signIn(username, password)) {
            System.out.println("Selamat datang, " + penjual.getNama() + " (" + penjual.getPerusahaan() + ")!");
            menuAdmin();
        } else {
            System.out.println("Username atau password salah.");
        }
    }

    private static void menuAdmin() {
        boolean logout = false;
        while (!logout) {
            System.out.println();
            System.out.println("[Admin: " + penjual.getNama() + "]");
            System.out.println("1. Lihat aset yang dijual");
            System.out.println("2. Tambah / ubah aset");
            System.out.println("3. Semua transaksi");
            System.out.println("4. Verifikasi pembayaran");
            System.out.println("5. Kirim aset dan cetak invoice");
            System.out.println("6. Logout");
            int pilihan = bacaInt("Masukkan pilihan (1 - 6): ");

            switch (pilihan) {
                case 1:
                    if (penjual.getKatalog().isEmpty()) {
                        System.out.println("Katalog masih kosong.");
                    } else {
                        tampilkanKatalog();
                    }
                    break;
                case 2:
                    tambahAset();
                    break;
                case 3:
                    pilihTransaksi(daftarTransaksi);
                    break;
                case 4:
                    verifikasiPembayaran();
                    break;
                case 5:
                    kirimAset();
                    break;
                case 6:
                    logout = true;
                    break;
                default:
                    System.out.println("Pilihan harus 1 sampai 6, coba lagi!");
                    break;
            }
        }
    }

    private static void tambahAset() {
        System.out.println("\n--- Tambah / Ubah Aset ---");
        String nama = bacaTeks("Nama aset (contoh BTC) : ");
        double harga = bacaDouble("Harga per unit (Rp)     : ");
        double stok = bacaDouble("Stok yang ditambahkan   : ");
        AsetDigital ada = penjual.cariAset(nama);
        if (ada == null) {
            penjual.addKatalog(new AsetDigital(nama, stok, harga));
            System.out.println("Aset baru ditambahkan.");
        } else {
            ada.setHargaPerUnit(harga);
            ada.setJumlahAset(ada.getJumlahAset() + stok);
            System.out.println("Aset sudah ada: harga diperbarui dan stok ditambah.");
        }
        simpanKatalog();
    }

    private static void verifikasiPembayaran() {
        PenjualanCrypto t = pilihTransaksi(filterStatus(PenjualanCrypto.MENUNGGU_VERIFIKASI));
        if (t == null) {
            return;
        }
        t.tampilkanTagihan();
        System.out.println("ID transaksi dari customer: " + t.getBuktiBayar());
        double nominal = bacaDouble("Nominal yang masuk di mutasi rekening (Rp): ");
        if (t.verifikasiPembayaran(nominal)) {
            System.out.println("Pembayaran VALID. Status: " + t.getStatus());
        } else {
            System.out.println("Nominal tidak sama dengan total bayar. Pembayaran DITOLAK,");
            System.out.println("customer diminta mengirim ulang ID transaksi.");
        }
        simpanTransaksi();
    }

    private static void kirimAset() {
        PenjualanCrypto t = pilihTransaksi(filterStatus(PenjualanCrypto.DIPROSES));
        if (t == null) {
            return;
        }
        // cek stok semuanya dulu, biar nggak ada aset yang terkirim setengah
        for (AsetDigital a : t.getListAset()) {
            AsetDigital stok = penjual.cariAset(a.getNamaAset());
            if (stok == null || stok.getJumlahAset() < a.getJumlahAset()) {
                System.out.println("Stok " + a.getNamaAset() + " tidak cukup. Tambah stok dahulu (menu 2).");
                return;
            }
        }
        System.out.println("Kirim aset ke wallet " + t.getCustomer().getAlamatWallet());
        String bukti = bacaTeks("Hash / ID transaksi crypto pengiriman: ");
        for (AsetDigital a : t.getListAset()) {
            AsetDigital stok = penjual.cariAset(a.getNamaAset());
            stok.setJumlahAset(stok.getJumlahAset() - a.getJumlahAset());
        }
        t.kirimAset(bukti);
        simpanKatalog();
        simpanTransaksi();
        System.out.println("\nAset terkirim. Invoice:\n");
        t.struk();
        tampilkanPihak(t);
    }

    //tampilan
    private static ArrayList<PenjualanCrypto> filterStatus(String status) {
        ArrayList<PenjualanCrypto> hasil = new ArrayList<>();
        for (PenjualanCrypto t : daftarTransaksi) {
            if (t.getStatus().equals(status)) {
                hasil.add(t);
            }
        }
        return hasil;
    }

    //tampilkan daftar transaksi, user pilih nomor transaksi, kembalikan transaksi yang dipilih atau null kalau batal
    private static PenjualanCrypto pilihTransaksi(ArrayList<PenjualanCrypto> daftar) {
        if (daftar.isEmpty()) {
            System.out.println("Tidak ada transaksi.");
            return null;
        }
        System.out.println();
        for (int i = 0; i < daftar.size(); i++) {
            PenjualanCrypto t = daftar.get(i);
            System.out.println((i + 1) + ". " + t.getInvoiceNo() + " | " + t.getTanggalWaktu()
                    + " | " + t.getCustomer().getNama()
                    + " | " + rupiah(t.hitungTotalPembayaran())
                    + " | " + t.getStatus());
        }
        while (true) {
            int nomor = bacaInt("Nomor transaksi (0 = kembali): ");
            if (nomor == 0) {
                return null;
            }
            if (nomor >= 1 && nomor <= daftar.size()) {
                return daftar.get(nomor - 1);
            }
            System.out.println("Nomor tidak ada, coba lagi!");
        }
    }

    //polymorphism: Customer dan Penjual dimasukin ke satu list Person
    private static void tampilkanPihak(PenjualanCrypto t) {
        ArrayList<Person> pihak = new ArrayList<>();
        pihak.add(t.getPenjual());
        pihak.add(t.getCustomer());

        System.out.println("Pihak dalam transaksi:");
        for (Person p : pihak) {
            System.out.print(" - " + p.getPeran() + ": " + p.getKode() + " / " + p.getNama());
            if (p instanceof Customer) {
                System.out.println(" (wallet " + ((Customer) p).getAlamatWallet() + ")");
            } else if (p instanceof Penjual) {
                System.out.println(" (" + ((Penjual) p).getPerusahaan() + ")");
            }
        }
    }

    //baca file
    private static void muatPenjual() {
        File berkas = new File(FILE_PENJUAL);
        if (!berkas.exists()) {
            // file belum ada, jadi bikin akun admin bawaan lalu disimpan
            Pembayaran rekening = new Pembayaran("BCA", "1234567890", "Budi Santoso");
            penjual = new Penjual("P01", "Budi", "PT Kripto Nusantara", rekening);
            penjual.signUp("admin", "admin123");
            simpanPenjual();
            return;
        }
        try {
            Scanner pembaca = new Scanner(berkas);
            String[] f = pembaca.nextLine().split(",", -1);
            Pembayaran rekening = new Pembayaran(f[5], f[6], f[7]);
            penjual = new Penjual(f[0], f[1], f[2], rekening);
            penjual.signUp(f[3], f[4]);
            pembaca.close();
        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            System.out.println("File penjual bermasalah: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void muatKatalog() {
        File berkas = new File(FILE_ASET);
        if (!berkas.exists()) {
            return;
        }
        try {
            Scanner pembaca = new Scanner(berkas);
            while (pembaca.hasNextLine()) {
                String baris = pembaca.nextLine();
                if (baris.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] f = baris.split(",", -1);
                    penjual.addKatalog(new AsetDigital(f[0], Double.parseDouble(f[2]), Double.parseDouble(f[1])));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Baris aset dilewati (format salah): " + baris);
                }
            }
            pembaca.close();
        } catch (FileNotFoundException e) {
            System.out.println("File aset tidak ditemukan: " + e.getMessage());
        }
    }

    private static void muatTransaksi() {
        File berkas = new File(FILE_TRANSAKSI);
        if (!berkas.exists()) {
            return;
        }
        try {
            Scanner pembaca = new Scanner(berkas);
            while (pembaca.hasNextLine()) {
                String baris = pembaca.nextLine();
                if (baris.trim().isEmpty()) {
                    continue;
                }
                try {
                    daftarTransaksi.add(barisKeTransaksi(baris));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Baris transaksi dilewati (format salah): " + baris);
                }
            }
            pembaca.close();
        } catch (FileNotFoundException e) {
            System.out.println("File transaksi tidak ditemukan: " + e.getMessage());
        }
    }

    private static PenjualanCrypto barisKeTransaksi(String baris) {
        String[] f = baris.split(",", -1);
        Customer customer = null;
        for (Customer c : daftarCustomer) {
            if (c.getKode().equals(f[2])) {
                customer = c;
            }
        }
        if (customer == null) {
            customer = new Customer(f[2], f[3], f[4]);
            daftarCustomer.add(customer);
        }
        PenjualanCrypto transaksi = new PenjualanCrypto(f[0], f[1], penjual, customer);
        transaksi.setStatus(f[5]);
        transaksi.setBuktiBayar(f[6]);
        transaksi.setBuktiKirim(f[7]);
        for (String a : f[8].split(";")) {
            String[] d = a.split(":");
            transaksi.addAset(new AsetDigital(d[0], Double.parseDouble(d[1]), Double.parseDouble(d[2])));
        }
        return transaksi;
    }

    //simpan file
    private static void siapkanFolder() {
        File folder = new File(FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private static void simpanPenjual() {
        try {
            siapkanFolder();
            FileWriter fw = new FileWriter(FILE_PENJUAL);
            fw.write(penjual.getKode() + "," + penjual.getNama() + "," + penjual.getPerusahaan() + ","
                    + penjual.getUsername() + "," + penjual.getPassword() + ","
                    + penjual.getRekening().getBank() + "," + penjual.getRekening().getNomorRekening() + ","
                    + penjual.getRekening().getAtasNama());
            fw.write(System.lineSeparator());
            fw.close();
        } catch (IOException e) {
            System.out.println("Gagal menyimpan file penjual: " + e.getMessage());
        }
    }

    private static void simpanKatalog() {
        try {
            siapkanFolder();
            FileWriter fw = new FileWriter(FILE_ASET);
            for (AsetDigital a : penjual.getKatalog()) {
                fw.write(a.getNamaAset() + "," + angka(a.getHargaPerUnit()) + "," + angka(a.getJumlahAset()));
                fw.write(System.lineSeparator());
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("Gagal menyimpan file aset: " + e.getMessage());
        }
    }

    private static void simpanTransaksi() {
        try {
            siapkanFolder();
            FileWriter fw = new FileWriter(FILE_TRANSAKSI);   // tulis ulang semua transaksi
            for (PenjualanCrypto t : daftarTransaksi) {
                fw.write(t.getInvoiceNo() + "," + t.getTanggalWaktu() + ","
                        + t.getCustomer().getKode() + "," + t.getCustomer().getNama() + ","
                        + t.getCustomer().getAlamatWallet() + ","
                        + t.getStatus() + "," + t.getBuktiBayar() + "," + t.getBuktiKirim() + ",");
                fw.write(asetKeTeks(t));
                fw.write(System.lineSeparator());
            }
            fw.close();
        } catch (IOException e) {
            System.out.println("Gagal menyimpan ke file: " + e.getMessage());
        }
    }

    private static String asetKeTeks(PenjualanCrypto t) {
        StringBuilder sb = new StringBuilder();
        for (AsetDigital a : t.getListAset()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(a.getNamaAset()).append(":")
              .append(angka(a.getJumlahAset())).append(":")
              .append(angka(a.getHargaPerUnit()));
        }
        return sb.toString();
    }

    //input
    private static int bacaInt(String pesan) {
        while (true) {
            System.out.print(pesan);
            String cek = input.nextLine().trim();
            try {
                return Integer.parseInt(cek);
            } catch (NumberFormatException e) {
                System.out.println("Salah input, ulangi lagi!");
            }
        }
    }

    private static double bacaDouble(String pesan) {
        while (true) {
            System.out.print(pesan);
            String cek = input.nextLine().trim().replace(',', '.');
            try {
                double nilai = Double.parseDouble(cek);
                if (nilai > 0) {
                    return nilai;
                }
                System.out.println("Nilai harus lebih dari 0, ulangi lagi!");
            } catch (NumberFormatException e) {
                System.out.println("Salah input, ulangi lagi!");
            }
        }
    }

    //jumlah harus > 0 dan nggak boleh lebih dari stok
    private static double bacaJumlah(String pesan, double maksimal) {
        while (true) {
            double nilai = bacaDouble(pesan + "(maks " + angka(maksimal) + ") ");
            if (nilai <= maksimal) {
                return nilai;
            }
            System.out.println("Jumlah melebihi stok, ulangi lagi!");
        }
    }

    private static String bacaTeks(String pesan) {
        while (true) {
            System.out.print(pesan);
            //tanda pemisah file diganti spasi supaya format file nggak rusak
            String teks = input.nextLine().replace(",", " ").replace(";", " ").replace(":", " ")
                    .replaceAll("\\s+", " ").trim();
            if (!teks.isEmpty()) {
                return teks;
            }
            System.out.println("Tidak boleh kosong, ulangi lagi!");
        }
    }

    private static boolean bacaYaTidak(String pesan) {
        while (true) {
            System.out.print(pesan);
            String jawab = input.nextLine().trim();
            if (jawab.equalsIgnoreCase("y")) {
                return true;
            } else if (jawab.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Jawab dengan y atau n!");
        }
    }

    //format angka
    private static String rupiah(double nilai) {
        return String.format(LOKAL, "Rp%,.0f", nilai);
    }

    private static String angka(double nilai) {
        return BigDecimal.valueOf(nilai).toPlainString();
    }
}
