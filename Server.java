package YEBChat;

import java.io.IOException;                 //input output hataları için
import java.io.InputStream;                 //veriyi  okur
import java.io.PrintStream;                 //okunabilir metin yazar
import java.net.ServerSocket;            //sunucu soketi
import java.net.Socket;                      //istemci soketi
import java.util.ArrayList;                   //liste arayüzünde yeniden boyutlanabilir dizi için
import java.util.List;                           //liste
import java.util.Scanner;                   //metin tarayıcı

public class Server {
  private final int port;
  private final List<Kullanici> clients;
  private ServerSocket server;

  public static void main(String[] args) throws IOException {
    new Server(35).run();
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<>();
  }

  public void run() throws IOException {//program sona erdiğinde bağlantıların temizlenmesi için kullandık.
    server = new ServerSocket(port) {
      @Override
      protected void finalize() throws IOException, Throwable {
          try {
              this.close();//sunucunun belirli bir port numarasında dinlemesini sonlandırır
          } finally {
              super.finalize();
          }
      }
    };
    System.out.println("Port 35 açık.");

    while (true) {
      //clienti sürekli kabul et
      Socket client = server.accept();

      // yeni kullanıcının kullanıcı adını al
      String kullaniciadi = (new Scanner ( client.getInputStream() )).nextLine();
      kullaniciadi = kullaniciadi.replace(",", ""); // kullanıcı adındaki virgülleri siler.
      kullaniciadi = kullaniciadi.replace(" ", "_"); // kullanıcı adındaki boşlukları alt çizgi ile değiştirir.
      System.out.println("Yeni Kullanıcı: \"" + kullaniciadi + "\"\n\t     Host :" + client.getInetAddress().getHostAddress()); //outputta bağlanan kullanıcının kullanıcı adını ve ip adresini veriyor

      // yeni kullanıcı oluştur
      Kullanici yeniKullanici = new Kullanici(client, kullaniciadi);

      // listeye yeni kullanıcı mesajı ekle
      this.clients.add(yeniKullanici);

      // Hoşgeldiniz mesajı
      yeniKullanici.getOutStream().println("Hoşgeldin " + kullaniciadi);
           
      // yeni kullanıcıdan gelen mesajlarının işlenmesi için yeni bir thread oluşturduk
      new Thread(new UserHandler(this, yeniKullanici)).start();
    }
  }
  // listeden bir kullanıcı sil
 public void kullaniciyiKaldir(Kullanici kullanici){
 this.clients.remove(kullanici);
 }

  // gelen mesajı tüm kullanıcılara gönder
  public void broadcastMessages(String mesaj, Kullanici Gonderen_Kullanici) {
    for (Kullanici client : this.clients) {
      client.getOutStream().println(Gonderen_Kullanici.toString() +":  "+ mesaj);
    }
  }

  //tüm kullanıcılara gönder metodu oluşturuyoruz.
  public void TumKullanicilaraGonder(){
    for (Kullanici client : this.clients) {
      client.getOutStream().println(this.clients);//çıkışı tüm clientlere gönder
    }
  }

class UserHandler implements Runnable {//userhandler kullanıcı işlemlerini yönetmek için kullanılır.

  private final Server server;
  private final Kullanici kullanici;

  public UserHandler(Server server, Kullanici _kullanici_) {
    this.server = server;
    this.kullanici = _kullanici_;
    this.server.TumKullanicilaraGonder();
  }

  public void run() {//Thread tarafından çağrılacak ve kullanıcıdan gelen mesajları dinleyecek.
    String mesaj;
      try ( 
                // yeni bir mesaj olduğunda herkese gönder
              Scanner sc = new Scanner(this.kullanici.getInputStream())) {
          while (sc.hasNextLine()) {//kullanıcının gönderdiği her mesajı dinlemek için bir döngü başlattık.
              mesaj = sc.nextLine();//kullanıcıdan gelen mesajı "mesaj" değişkenine atadık.
              
              // Özel mesajların yönetimi
              switch (mesaj.charAt(0)) {
                  default -> // güncelleme kullanıcı listesi
                      server.broadcastMessages(mesaj, kullanici);
              }
          }
          //kullanıcı sohbetten ayrılınca kullanıcı listesinden kullanıcıyı çıkartıyor
          server.kullaniciyiKaldir(kullanici);
          this.server.TumKullanicilaraGonder();
      }
  }
  }

class Kullanici {
  private final PrintStream yazdir;
  private final InputStream giris_al;
  private final String kullaniciadi;

  // yapıcı method constructor
  public Kullanici(Socket client, String ad) throws IOException {
    this.yazdir = new PrintStream(client.getOutputStream());
    this.giris_al = client.getInputStream();
    this.kullaniciadi = ad;
  }

  public PrintStream getOutStream(){//kullanıcı çıkışını verir
    return this.yazdir;
  }

  public InputStream getInputStream(){//kullanıcıdan giriş alır
    return this.giris_al;
  }

  public String kullaniciadi_al(){//kullanıcı adını döndürür
    return this.kullaniciadi;
  }
  
  public String toString(){//string ifade döndürür
      return this.kullaniciadi_al();}
    }
}