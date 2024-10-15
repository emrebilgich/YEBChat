package YEBChat;

import java.awt.*;                                                   //java grafik arayüzü için
import java.awt.event.ActionEvent;                        //butona tıklamak için
import java.awt.event.ActionListener;                    //butona tıklamayı algılamak için
import java.awt.event.KeyAdapter;                        //Klavye girişi almak için
import java.awt.event.KeyEvent;                           //klavyeden basılan tuşla işlem yapmak için
import java.net.*;                                                   //TCP cihazlar arası veri transferi için
import java.io.*;                                                     //input output
import javax.swing.*;                                             //Grafik arayüzü tasarlamak için
import javax.swing.text.*;                                      //arayüzdeki metinler için
import javax.swing.event.DocumentEvent;           //arayüzde bildirim için
import javax.swing.event.DocumentListener;       //textdeki değişiklikler için arayüz
import java.util.ArrayList;                                      //yeniden boyutlanabilir dizi
import java.util.Arrays;                                          //dizi işlemleri için arama vs

public class ClientGui extends Thread{

  final JTextPane MesajlasmaPaneli = new JTextPane(); //Mesajlaşma paneli nesnesi oluşturduk
  final JTextPane jKullaniciListesi = new JTextPane(); //Kullanıcı listesi paneli nesnesi oluşturduk
  final JTextField jMesajYazmaAlani = new JTextField(); //mesaj yazma alanı nesnesi oluşturduk
  private Thread read;
  private String serverName;
  private int PORT;
  private String KullaniciAdi;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public static void main(String[] args) throws Exception {
    ClientGui Client = new ClientGui();
  }
  
  public ClientGui() {
    this.serverName = "127.0.0.1";
    this.PORT = 35;
    this.KullaniciAdi = "";

    String fontfamily = "Arial, sans-serif"; 
    Font font = new Font(fontfamily, Font.PLAIN, 15); //karakter şekli ve boyutu belirledik

    final JFrame jFrame = new JFrame("YEBChat");
    jFrame.getContentPane().setLayout(null);//otomatik arayüz yerleşimi yerine manuel (x,y,w,h) yerleşim için
    jFrame.setSize(700, 500);//program çerçeve boyutları
    jFrame.setResizable(false);// programın çerçevesini genişletip küçültmemek için içine false atadık true atarsak program çerçevesini değiştirebiliriz.
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // arka planda işlem sonlandırmak için

    //  Mesajların göründüğü alan
    MesajlasmaPaneli.setBounds(12, 25, 502, 325);//mesajlaşma paneli boyutları ve konumunu belirledik.
    MesajlasmaPaneli.setFont(font);//mesajlaşma alanı karakter şekli
    MesajlasmaPaneli.setEditable(false); //setEditable kullanıp içine false atadık bu sayede mesajlaşma panelinde mesajlar tıklayarak editlenemeyecek
    JScrollPane jMesajlasmaAlani_Kaydirma = new JScrollPane(MesajlasmaPaneli); // JTextPane aşağı yukarı kaydırmayı desteklemez JScrollPane ekledik.
    jMesajlasmaAlani_Kaydirma.setBounds(12, 25, 502, 325); //kaydırmanın olacağı boyutları belirledik mesajlaşma paneli ile aynı olmalı.

    // Kullanıcı listesinin olduğu alan
    jKullaniciListesi.setBounds(520, 40, 156, 310);//kullanici listesi boyutları ve konumunu belirledik.
    jKullaniciListesi.setFont(font); // kullanıcı listesindeki karakter şekli
    jKullaniciListesi.setEditable(false); //setEditable kullanıp içine false atadık bu sayede mesajlaşma panelinde mesajlar tıklayarak editlenemeyecek
    JScrollPane jKullaniciListesi_Kaydirma = new JScrollPane(jKullaniciListesi); // JTextPane aşağı yukarı kaydırmayı desteklemez JScrollPane ekledik.
    jKullaniciListesi_Kaydirma.setBounds(520, 40, 156, 310); //kaydırmanın olacağı boyutları belirledik kullanıcı listesi boyutları ile aynı olmalı.

    // Mesaj yazılan alan
    jMesajYazmaAlani.setBounds(12, 350, 664, 50);//mesaj yazma alani boyutları ve konumunu belirledik.
    jMesajYazmaAlani.setFont(font);//arial-15 fontunu kullandık
    final JScrollPane jMesajYazmaAlani_Kaydirma = new JScrollPane(jMesajYazmaAlani); // JTextPane aşağı yukarı kaydırmayı desteklemez JScrollPane ekledik.
    jMesajYazmaAlani_Kaydirma.setBounds(12, 350, 664, 50);//kaydırmanın olacağı boyutları belirledik mesaj yazma alanı boyutları ile aynı olmalı.

    // Mesajı Gönder butonu görünümü
    final JButton jMesajiGonderButonu = new JButton("Mesajı Gönder"); //yeni bir button oluşturduk ve içine mesajı gönder yazdık.
    jMesajiGonderButonu.setFont(font);//arial-15 fontunu kullandık
    jMesajiGonderButonu.setBounds(534, 410, 140, 35);// mesajı gönder butonunun boyutlarını ve konumunu belirledik.

    // Sohbetten Ayrıl butonu görünümü
    final JButton jSohbettenAyrılButonu = new JButton("Sohbetten Ayrıl");//yeni bir button oluşturduk ve içine sohbetten ayrıl yazdık.
    jSohbettenAyrılButonu.setFont(font);//arial-15 fontunu kullandık
    jSohbettenAyrılButonu.setBounds(12, 410, 140, 35);// sohbetten ayrıl butonunun boyutlarını ve konumunu belirledik.
    
    //Kullanıcı Adınız Label görünümü
    final JLabel jKullaniciAdiLabel = new JLabel("Kullanıcı Adınız:");
    jKullaniciAdiLabel.setFont(font); // Font ayarı
    jKullaniciAdiLabel.setBounds(358, 365, 160, 12); // konumu ve boyutu
    jFrame.add(jKullaniciAdiLabel);

    // Port Label görünümü
    final JLabel jPortLabel = new JLabel("PORT:");
    jPortLabel.setFont(font); // Font ayarı
    jPortLabel.setBounds(180, 350, 135, 40); //konumu ve boyutu
    jFrame.add(jPortLabel);

    // Host Label görünümü
    final JLabel jHostLabel = new JLabel("HOST:");
    jHostLabel.setFont(font); // Font ayarı
    jHostLabel.setBounds(12, 350, 135, 40);//konumu ve boyutu
    jFrame.add(jHostLabel);
    
    // Kullanıcı Listesi Label görünümü
    final JLabel jKullaniciListesiLabel = new JLabel("Kullanıcı Listesi:");
    jKullaniciListesiLabel.setFont(font); // Font ayarı
    jKullaniciListesiLabel.setBounds(520, -120, 156, 300); //konumu ve boyutu

    jMesajYazmaAlani.addKeyListener(new KeyAdapter() {
      // Enter ile mesaj gönderme
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          MesajGonder();
        }}});

    // Mesajı Gönder butonuna tıkladığımızda olacakları yazıyoruz.
    jMesajiGonderButonu.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            MesajGonder();// butona basıldığında mesajgonder() metodu devreye girecek.
        }
    });

    // Başlangıç görünümü
    final JTextField jKullaniciAdiGirinizAlani = new JTextField(this.KullaniciAdi);
    final JTextField jportAlani = new JTextField(Integer.toString(this.PORT));    //35
    final JTextField jHostAlani = new JTextField(this.serverName);             //127.0.0.1
    final JButton jSohbeteBaglanButonu = new JButton("Sohbete Bağlan");           //Sohbete bağlan butonu
 
    // başlangıçtaki text alanlarının ve butonun konumlarını boyutlarını ve fontunu belirledik.
    jSohbeteBaglanButonu.setFont(font);//arial-15 fontunu kullandık
    jHostAlani.setBounds(12, 380, 135, 40); //LocalHost konumu
    jKullaniciAdiGirinizAlani.setBounds(358, 380, 135, 40); // kullanıcıadıgiriniz konumu
    jportAlani.setBounds(180, 380, 135, 40); // 51 port konumu
    jSohbeteBaglanButonu.setBounds(520, 380, 156, 40); //sohbete bağlan butonu konumu

    //Arkaplan renklerini belirledik.
    MesajlasmaPaneli.setBackground(Color.LIGHT_GRAY); //Sohbete bağlanmadan önceki mesajlaşma alanı rengi
    jKullaniciListesi.setBackground(Color.LIGHT_GRAY); //Sohbete bağlanmadan önceki kullanıcı listesi alanı rengi

    //Başlangıç görünümü öğelerimizi ekledik.
    jFrame.add(jSohbeteBaglanButonu);
    jFrame.add(jMesajlasmaAlani_Kaydirma);//mesajlaşma alanı gri tonda
    jFrame.add(jKullaniciListesi_Kaydirma);//kullanıcı listesi gri tonda
    jFrame.add(jKullaniciAdiGirinizAlani);
    jFrame.add(jportAlani);
    jFrame.add(jHostAlani);
    jFrame.setVisible(true); // chat penceremizin görünürlüğünü sağlıyor false atarsak chat uygulamamız görünmez.

    //SohbeteBaglanButonu'na tıkladığımızda olacakları yazıyoruz.
    jSohbeteBaglanButonu.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                
                //Kullanıcı adı kısmı boş ise hata vermesini sağlıyoruz
                KullaniciAdi = jKullaniciAdiGirinizAlani.getText(); //kullanıcı adı alanına gettext() metodu ile kullanıcı girişi alabiliriz.
                if (KullaniciAdi.trim().isEmpty()) {
                Yazdır(MesajlasmaPaneli, "Lütfen bir kullanıcı adı giriniz!!!");
                return;
                }
                
                //port kısmı boş ise hata vermesini sağlıyoruz
                String portText = jportAlani.getText();
                if (portText.trim().isEmpty()) {
                Yazdır(MesajlasmaPaneli, "Lütfen bir port giriniz!!!");
                return;
                }
                 
                //host alanı boş ise hata vermesini sağlıyoruz
                serverName = jHostAlani.getText();//host alanına gettext() metodu ile kullanıcı girişi alabiliriz.
                 if (serverName.trim().isEmpty()) {
                Yazdır(MesajlasmaPaneli, "Lütfen bir IP adresi giriniz!!!");
                return;
                }
                 
                PORT = Integer.parseInt(portText);
                server = new Socket(serverName, PORT);//serverName ve PORT numarasına bağlantı kurarak bir Socket nesnesi oluşturduk.Clientin servera bağlanmasını sağlar.
                Yazdır(MesajlasmaPaneli,"Sohbete Bağlanıldı!");
                input = new BufferedReader(new InputStreamReader(server.getInputStream())); //serverdan gelen verileri okumak için bir BufferedReader oluşturur.
                output = new PrintWriter(server.getOutputStream(), true); //servera veri göndermek için bir PrintWriter oluşturur.
                
                // servera kullanıcı adı gönder
                output.println(KullaniciAdi);
                
                // yeni read threadi oluştur
                read = new Read();
                read.start();
                
                //sohbete bağlanınca çıkarılan buton ve text alanları
                jFrame.remove(jKullaniciAdiGirinizAlani);
                jFrame.remove(jportAlani);
                jFrame.remove(jHostAlani);
                jFrame.remove(jSohbeteBaglanButonu);
                jFrame.remove(jKullaniciAdiLabel);
                jFrame.remove(jHostLabel);
                jFrame.remove(jPortLabel);
                
                //sohbete bağlanınca eklenen buton ve text alanları
                jFrame.add(jMesajiGonderButonu);
                jFrame.add(jMesajYazmaAlani_Kaydirma);
                jFrame.add(jSohbettenAyrılButonu);
                jFrame.add(jKullaniciListesiLabel);
                
                jFrame.revalidate();//revalidate() metodu boyutları, konumları ve genel düzenini günceller.
                jFrame.repaint();//ekranın güncellenmesini sağlar.
                
                //mesajlaşma paneli ve kullanıcı listesinin arkaplan renklerini belirledik.
                MesajlasmaPaneli.setBackground(Color.WHITE);
                jKullaniciListesi.setBackground(Color.WHITE);
                
            } catch (IOException | NumberFormatException ex) {//hata yakalama durumunda
                Yazdır(MesajlasmaPaneli,"Sunucuya Bağlanılamadı!");//hata yakalama çıktısını MesajlasmaPanelineYaz metodu ile panele yazdırdık.
            }
        }
    });

    // sohbetten ayrılınca olacaklar
    jSohbettenAyrılButonu.addActionListener(new ActionListener() {//sohbetten ayrılınca id port host alanı panele eklenir mesaj gönder sohbetten ayrıl butonu panelden çıkarılır.
        @Override
        public void actionPerformed(ActionEvent ae) {
            
            //sohbetten ayrılınca panele eklenen buton ve text alanları
            jFrame.add(jKullaniciAdiGirinizAlani);
            jFrame.add(jportAlani);
            jFrame.add(jHostAlani);
            jFrame.add(jSohbeteBaglanButonu);
            jFrame.add(jKullaniciAdiLabel);
            jFrame.add(jHostLabel);
            jFrame.add(jPortLabel);
            
            //sohbetten ayrılınca panelden çıkarılan butonlar
            jFrame.remove(jMesajiGonderButonu);
            jFrame.remove(jMesajYazmaAlani_Kaydirma);
            jFrame.remove(jSohbettenAyrılButonu);
            jFrame.remove(jKullaniciListesiLabel);
            
            jFrame.revalidate();//revalidate() metodu boyutları, konumları ve genel düzenini günceller.
            jFrame.repaint();//ekranın güncellenmesini sağlar.
            read.interrupt();// read threadimiz kesintiye uğrayacak.
            jKullaniciListesi.setText(null);//kullanıcı listesi sıfırlanacak
            
            //sohbetten ayrılma sonrası mesajlaşma paneli ve kullanıcı listesinin arkaplan renklerini belirledik.
            MesajlasmaPaneli.setBackground(Color.LIGHT_GRAY);
            jKullaniciListesi.setBackground(Color.LIGHT_GRAY);
            
            //sohbetten ayrılınca MesajlasmaPanelineYaz metodu ile çıktı verdik.
            Yazdır(MesajlasmaPaneli, "Sohbetten Ayrıldın!"); output.close();
        }
    })  ;
  }

  // tüm alanların boş olup olmadığını kontrol edin
  public class TextListener implements DocumentListener{
    JTextArea jHostAlani;
    JTextField jPortAlani;
    JTextField jKullaniciAdiGirinizAlani;
    JButton jSohbeteBaglanButonu;

    public TextListener(JTextArea jHostAlani, JTextField jPortAlani, JTextField jKullaniciAdiGirinizAlani, JButton jSohbeteBaglanButonu){
      this.jHostAlani = jHostAlani;
      this.jPortAlani = jPortAlani;
      this.jKullaniciAdiGirinizAlani = jKullaniciAdiGirinizAlani;
      this.jSohbeteBaglanButonu = jSohbeteBaglanButonu;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {} //metin belgesinde metin değişikliği olursa çağrılır.

    @Override
    public void removeUpdate(DocumentEvent e) { //metin belgesinden metin çıkarma işlemi olursa çağırılır
        
        //if else kullanarak kullanıcı girişi yapılan alanlar boş mu değil mi kontrol ediyoruz ve ona göre sohbetebağlan butonunu aktif ediyoruz.
      if(jHostAlani.getText().trim().equals("") || 
          jPortAlani.getText().trim().equals("") || 
          jKullaniciAdiGirinizAlani.getText().trim().equals("") 
          ){
        jSohbeteBaglanButonu.setEnabled(false);//eğer text alanları boşsa sohbetebağlan butonu devre dışı bırakılır (false)
      }else{
        jSohbeteBaglanButonu.setEnabled(true);//eğer text alanları doluysa, sohbetebağlan butonu kullanılabilir durumda olur (true)
      }
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {//metin belgesine yeni metin eklendiğinde çağırılır
        
        //if else kullanarak kullanıcı girişi yapılan alanlar boş mu değil mi kontrol ediyoruz ve ona göre sohbetebağlan butonunu aktif ediyoruz.
      if(jHostAlani.getText().trim().equals("") ||
          jPortAlani.getText().trim().equals("") ||
          jKullaniciAdiGirinizAlani.getText().trim().equals("")
          ){
        jSohbeteBaglanButonu.setEnabled(false);//eğer text alanları boşsa sohbetebağlan butonu devre dışı bırakılır (false)
      }else{
        jSohbeteBaglanButonu.setEnabled(true);//eğer text alanları doluysa, sohbetebağlan butonu kullanılabilir durumda olur (true)
      }
    }
  }

  // MesajGonder metodu oluşturuyoruz.
  public void MesajGonder() {
    try{
      //eğer MesajYazmaAlani boş ise bir çıktı alınmaz.
      String mesaj = jMesajYazmaAlani.getText().trim();
      if (mesaj.equals("")) {
        return;
      }    
      //eğer MesajYazmaAlani boş değil ise "mesaj" çıktısını verir.
      output.println(mesaj);
      jMesajYazmaAlani.setText(null); //mesaj gönderdikten sonra gönderdiğimiz mesaj , mesaj yazma alanindan temizleniyor.
    } 
    catch (Exception ex) {//hata yakalama durumunda
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  // kullanıcıdan gelen mesajları okuması için Read Threadimizi oluşturuyoruz.
  class Read extends Thread {
    @Override
    public void run() {
      String mesaj;
      while(!Thread.currentThread().isInterrupted()){  //Thread kesintiye uğramadığı sürece çalışır.
        try {
          mesaj = input.readLine();//mesaj satırı okuma işlemi.
          if(mesaj != null){ //mesajın null olup olmadığını kontrol ediyoruz.
            if (mesaj.charAt(0) != '[') {
                Yazdır(MesajlasmaPaneli, mesaj);                     
            }else{
                mesaj = mesaj.substring(1, mesaj.length()-1);//kullanıcı adının [] ile gösterilmemesini sağlıyor.
                ArrayList<String> KullaniciListesi = new ArrayList<>(//kullanıcı listesini oluşturduk 
                        Arrays.asList(mesaj.split(", "))
                );
                jKullaniciListesi.setText(null);
                KullaniciListesi.forEach((String kullaniciadi) -> {
                    Yazdır(jKullaniciListesi, "@" + kullaniciadi);//kullanıcı listesine @kullaniciadi yazdırdık.
                });
            }
          }
        }
        catch (IOException ex) {}//hata yakalamaya bir işlem atamadık.
      }
    }
  }

  private void Yazdır(JTextPane tp, String mesaj) {
    StyledDocument doc = tp.getStyledDocument(); //StyledDocument metni font vs ile tutan belge modelidir.
    Style style = tp.addStyle("Style", null);//style olarak null yani varsayılan stil belirlendi.

    try {
        doc.insertString(doc.getLength(), mesaj + "\n" , style);//StyledDocumente belgenin uzunluğu alınır ve mesaj eklenir.
        tp.setCaretPosition(doc.getLength());// imleç metnin sonunda kalır.
    } catch (BadLocationException e) {}//hata yakalamaya bir işlem atamadık.
}
}