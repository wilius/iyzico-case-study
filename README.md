# iyzico coding challenge

iyzico mühendislik ekibinde çalışmak istediğiniz için teşekkür ederiz.

İşe alım sürecimizin bir parçası olarak, mühendislik ekibimize dahil etmek istediğimiz arkadaşlarımız için, 
kodlama yeteneklerini anlayabilme amacıyla kısa bir proje yapmalarını istiyoruz. Proje için hazır bir Spring Boot 
ve Java 8 teması hazırladık. Temada veritabanı olarak HSQLDB kullanıldı.

Bu projeden bir [fork](https://help.github.com/articles/fork-a-repo/) alıp ilgili geliştirmeleri yaptıktan sonra 
"pull request" açmanızı rica ediyoruz.

# Soru 1 : Kredi Kartı Maskeleme

iyzico'da kredi kart bilgilerinin geçtiği her yerde maskelenmesi ve kredi kartının hiçbir yerde saklanmaması gerekiyor. Bu gereksinim 
kapsamında [CardMaskingService.java](src/main/java/com/iyzico/challenge/service/CardMaskingService.java) sınıfını kullanarak
kart numaralarını maskelemenizi istiyoruz (ör: 4729150000000005 -> 472915******0005). Bu sınıfın birim testlerinin de
[CardMaskingServiceTest.java](src/test/java/com/iyzico/challenge/service/CardMaskingServiceTest.java) sınıfına eklenmesi gerekmektedir.

## Gereksinimler

* Kart numarası 15 hane ya da 16 hane olabilir.
* İlk 6 ve son 4 hanesi dışındaki alanlar * ile maskelenmelidir.
* Kart numarası olmayan metinler maskelenmemelidir.
* İlgili kod bir kütüphane kullanılmadan sadece Java 8 kullanılarak geliştirilmelidir.

# Soru 2: Ürün Yönetimi

iyzico'ya entegre olan müşterilerin büyük çoğunluğu ürün satışı yapmaktadır. Bir iyzico müşterisi olarak, ürünlerin
listelenebildiği, eklenip satışının yapılabildiği RESTful servislere ihtiyaç vardır. Aşağıda gereksinimleri olan
bu servisleri geliştirmenizi istiyoruz.

## Gereksinimler

* Ürün listeleme, ekleme, silme ve güncelleme servisi
* Eklenen ürünün isminin, açıklamasının, stokta kaç tane kaldığının ve fiyatının bulunduğu listeleme servisi
* Son kullanıcı için eklenen ürünün görüntülenme servisi
* Son kullanıcı için eklenen ürünün ödemesinin yapıldığı ödeme servisi
* İlgili servislerin birim test kapsamı (coverage) %80 olmalıdır.
* Eklenen bir ürün stoktaki sayısı kadar satılabilmelidir.
* Ödeme sayfasını aynı anda açan onlarca kullanıcı aynı anda ödeme tuşuna bastığında alınan ödemeler 
  stoktaki sayıdan fazla olmamalıdır.
* Ön yüz kodu beklenmemektedir. Sadece RESTFul servis yeterlidir.
* Birim testleri ve entegrasyon testleri beklenmektedir.
* Bonus: Ödeme servisinde iyzico'nun ödeme entegrasyonu yapılabilir. 
Referans: [https://dev.iyzipay.com/](https://dev.iyzipay.com/)


# Soru 3 : Gecikme Yönetimi (Latency Management)

iyzico bankaların ödeme servislerini çağırarak ödeme almaktadır ve bankadan dönen sonuçları veritabanına
kaydetmektedir.[IyzicoPaymentServiceTest.java](src/test/java/com/iyzico/challenge/service/IyzicoPaymentServiceTest.java)
sınıfında 100 adet kullanıcının aynı anda ödeme alma servisini çağırmasının simülasyonu bulunmaktadır.

Aşağıdaki metodda önce bankadan ödeme alınıp ardından veritabanına kayıt atılan kod simülasyonu bulunmaktadır.

```java
    public void pay(BigDecimal price) {
        //pay with bank
        BankPaymentRequest request = new BankPaymentRequest();
        request.setPrice(price);
        BankPaymentResponse response = bankService.pay(request);

        //insert records
        Payment payment = new Payment();
        payment.setBankResponse(response.getResultCode());
        payment.setPrice(price);
        paymentRepository.save(payment);
        logger.info("Payment saved successfully!");
    }
```

Bu simülasyonda bankalarda yaşanan bir problemden dolayı bankaların ödeme alması ortalama 5 saniye
sürmekte ve bir gecikme yaşanmaktadır. Bu gecikmeden dolayı da bir süre sonra veritabanı bağlantılarında
problem yaşanmaktadır. ([IyzicoPaymentServiceTest.java](src/test/java/com/iyzico/challenge/service/IyzicoPaymentServiceTest.java)
sınıfı çalıştırıldığında belli bir süre sonra "Connection is not available, request timed out after 30005ms." hatası
görülebilir)

Bankalarda gecikme olsa da bankadan dönen cevabın veritabanına başarılı bir şekilde kaydedilmesini sağlayınız.

## Gereksinimler

* DB connection pool'un boyutu arttırılmamalıdır.
* BankService.java, PaymentServiceClients.java ve IyzicoPaymentServiceTest.java sınıfları değiştirilmemelidir.
* Sunulan çözüm detaylı açıklanabilir ve yorumlanabilir olmalıdır. Üzerine uzun uzun tartışılacaktır.
* Herhangi bir hata ile karşılaşıldığında veritabanında kirli kayıt kalmamalıdır.






