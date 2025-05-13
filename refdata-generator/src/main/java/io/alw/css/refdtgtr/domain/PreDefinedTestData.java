package io.alw.css.refdtgtr.domain;

import io.alw.css.domain.referencedata.Country;
import io.alw.css.domain.referencedata.Currency;
import io.alw.css.domain.referencedata.Entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class PreDefinedTestData {
    public static PreDefinedTestData singleton;
    public final List<Entity> entities;
    public final List<Country> countries;
    public final List<Currency> currencies;

    public static PreDefinedTestData singleton() {
        if (singleton == null) {
            synchronized (PreDefinedTestData.class) {
                if (singleton == null) {
                    singleton = new PreDefinedTestData();
                }
            }
        }
        return singleton;
    }

    private PreDefinedTestData() {
        this.countries = List.of(
                new Country("DE", "Germany        ".trim(), "EMEA", LocalDateTime.now()),
                new Country("US", "United States  ".trim(), "NA", LocalDateTime.now()),
                new Country("UK", "United Kingdom ".trim(), "EMEA", LocalDateTime.now()),
                new Country("CH", "China          ".trim(), "APAC", LocalDateTime.now()),
                new Country("IN", "India          ".trim(), "APAC", LocalDateTime.now())
        );

        //NOTE: The cutOffTime value is determined relative to the Deutschland UTC time, ie UTC+1/CET
        this.currencies = List.of(
                new Currency("EUR", "DE", false, LocalTime.of(16, 15), true, LocalDateTime.now()),
                new Currency("USD", "US", false, LocalTime.of(17, 30).plusHours(6), true, LocalDateTime.now()),
                new Currency("GBP", "UK", false, LocalTime.of(17, 0).plusHours(1), true, LocalDateTime.now()),
                new Currency("CNH", "CH", false, LocalTime.of(16, 0).minusHours(7), true, LocalDateTime.now()),
                new Currency("INR", "IN", false, LocalTime.of(16, 0).minusHours(4).minusMinutes(30), true, LocalDateTime.now())
        );

        this.entities = List.of(
                new Entity("INT", 1, "Alw International                ".trim(), "EUR", "DE", "Germany       ".trim(), "ALWINTXXXXX", true, LocalDateTime.now()),
                new Entity("FRA", 1, "Alw Frankfurt, Germany           ".trim(), "EUR", "DE", "Germany       ".trim(), "ALWGERXXXXX", true, LocalDateTime.now()),
                new Entity("NYC", 1, "Alw, New York City, United States".trim(), "USD", "US", "United States ".trim(), "ALWNYCXXXXX", true, LocalDateTime.now()),
                new Entity("LON", 1, "Alw, London, United Kingdom      ".trim(), "GBP", "UK", "United Kingdom".trim(), "ALWLONXXXXX", true, LocalDateTime.now()),
                new Entity("HKG", 1, "Alw, Hong Kong, China            ".trim(), "CNH", "CH", "China         ".trim(), "ALWHKGXXXXX", true, LocalDateTime.now()),
                new Entity("DEL", 1, "Alw, Delhi, India                ".trim(), "INR", "IN", "India         ".trim(), "ALWDELXXXXX", true, LocalDateTime.now())
        );

        String dataCopiedFromExcelToCreateAReferenceDataRecordClass = """
                new Entity("INT", 1, "Alw International                ".trim(), "DE", "Germany       ".trim(), "ALWINTXXXXX", true, LocalDateTime.now()),
                new Entity("FRA", 1, "Alw Frankfurt, Germany           ".trim(), "DE", "Germany       ".trim(), "ALWGERXXXXX", true, LocalDateTime.now()),
                new Entity("NYC", 1, "Alw, New York City, United States".trim(), "US", "United States ".trim(), "ALWNYCXXXXX", true, LocalDateTime.now()),
                new Entity("LON", 1, "Alw, London, United Kingdom      ".trim(), "UK", "United Kingdom".trim(), "ALWLONXXXXX", true, LocalDateTime.now()),
                new Entity("HKG", 1, "Alw, Hong Kong, China            ".trim(), "CH", "China         ".trim(), "ALWHKGXXXXX", true, LocalDateTime.now()),
                new Entity("DEL", 1, "Alw, Delhi, India                ".trim(), "IN", "India         ".trim(), "ALWDELXXXXX", true, LocalDateTime.now()),
                
                """;

    }

    public static String regionCountryStateRawData() {
        return """
                Murmansk Oblast
                Limon
                Mpumalanga
                Zhytomyr oblast
                Eastern Visayas
                Henegouwen
                Lambayeque
                Upper Austria
                Los Lagos
                Van
                Delta
                Arica y Parinacota
                Chihuahua
                Binh Dinh
                Leinster
                Western Visayas
                Northamptonshire
                Kogi
                Oklahoma
                Bihar
                Xibei
                Salzburg
                Soccsksargen
                Junin
                Kharkiv oblast
                Rivne oblast
                izmir
                Cartago
                Baden Wurttemberg
                Arequipa
                Newfoundland and Labrador
                Leinster
                South Kalimantan
                Xibei
                Veneto
                Bourgogne
                Daman and Diu
                ostergotlands lan
                innlandet
                Rivne oblast
                Alabama
                Murmansk Oblast
                Biobio
                Yucatan
                West Region
                Lambayeque
                Caraga
                Comunitat Valenciana
                Northwest Territories
                Parana
                Victoria
                National Capital Region
                North island
                Atacama
                Lubuskie
                Florida
                Carinthia
                Boyaca
                Jeju
                New South Wales
                Kujawsko-pomorskie
                Colorado
                Gauteng
                Huntingdonshire
                Lao Cai
                ostergotlands lan
                South island
                Burgenland
                Eastern Visayas
                Kogi
                Castilla y Leon
                San Andres y Providencia
                Pernambuco
                Limon
                Ulster
                Free State
                Vorarlberg
                Rio de Janeiro
                Lagos
                Agder
                Antofagasta
                Jonkopings lan
                Marche
                Sokoto
                West-Vlaanderen
                Stockholms lan
                North-East Region
                Lima
                Northern Cape
                Agder
                Tien Giang
                Saratov Oblast
                Pays de la Loire
                Chernihiv oblast
                Nova Scotia
                Baja California
                Sindh
                Dnipropetrovsk oblast
                South Australia
                Henegouwen
                Northern Mindanao
                Puntarenas
                Zhongnan
                Nordrhein-Westphalen
                Valparaiso
                Rogaland
                Kerala
                Vinnytsia oblast
                ivano-Frankivsk oblast
                ilocos Region
                Ontario
                Luik
                Nuevo Leon
                istanbul
                ilocos Region
                Salzburg
                Stockholms lan
                Saskatchewan
                Calabarzon
                South island
                British Columbia
                Vlaams-Brabant
                Nunavut
                Choco
                Durham
                Limon
                Bursa
                Gloucestershire
                Sachsen-Anhalt
                Victoria
                Euskadi
                Kon Tum
                Eastern Visayas
                Rio Grande do Sul
                Jammu and Kashmir
                Veneto
                Dongbei
                North Gyeongsang
                North West
                Sardegna
                Delta
                Coahuila
                Mpumalanga
                Pernambuco
                Calabarzon
                Xinan
                West Nusa Tenggara
                Tasmania
                Xinan
                Principado de Asturias
                Paraiba
                Hidalgo
                Queretaro
                innlandet
                Ulster
                Aydin
                Zhongnan
                Queensland
                Pays de la Loire
                Queretaro
                Kayseri
                Picardie
                North Region
                Champagne-Ardenne
                Queretaro
                Veneto
                Puno
                Caraga
                Diyarbakir
                West Nusa Tenggara
                Andalucia
                Santa Catarina
                Novosibirsk Oblast
                Lazio
                Henegouwen
                Caraga
                Gujarat
                Guerrero
                Midi-Pyrenees
                Aydin
                Connacht
                Gavleborgs lan
                ostergotlands lan
                Kherson oblast
                Aisen
                South island
                Podlaskie
                Southwestern Tagalog Region
                Victoria
                izmir
                Jalisco
                Corse
                Florida
                South Australia
                Rio de Janeiro
                Ontario
                Khyber Pakhtoonkhwa
                Ho Chi Minh City
                Cundinamarca
                Vestland
                Samsun
                North Gyeongsang
                Borno
                Chihuahua
                Australian Capital Territory
                Leinster
                Adana
                Para
                Dalarnas lan
                Rio Grande do Sul
                Hoa Binh
                O'Higgins
                Balikesir
                Buteshire
                Troms og Finnmark
                Louisiana
                Arkansas
                Limon
                Tambov Oblast
                Arkansas
                Troms og Finnmark
                ostergotlands lan
                Magallanes y Antartica Chilena
                Puntarenas
                Flintshire
                Dalarnas lan
                Mizoram
                East Region
                Ontario
                Gangwon
                Lima
                Stockholms lan
                Eastern Cape
                Huabei
                Basilicata
                Poitou-Charentes
                Tolima
                Puglia
                National Capital Region
                Ha Nam
                Shetland
                Gavleborgs lan
                West-Vlaanderen
                Norfolk
                New South Wales
                imo
                Dongbei
                Munster
                Yucatan
                Los Rios
                West Java
                izmir
                Vestfold og Telemark
                Limon
                South island
                Alabama
                Puno
                Guanacaste
                izmir
                Gavleborgs lan
                Mecklenburg-Vorpommern
                Maranhao
                Boyaca
                East Region
                British Columbia
                Ogun
                Vlaams-Brabant
                Tomsk Oblast
                Wyoming
                East Nusa Tenggara
                Gilgit Baltistan
                Mpumalanga
                Balikesir
                Wisconsin
                British Columbia
                Zakarpattia oblast
                Tasmania
                Zhytomyr oblast
                Cusco
                Auvergne
                Liguria
                Overijssel
                Jonkopings lan
                Ulster
                Nordrhein-Westphalen
                ostergotlands lan
                Kherson oblast
                Xinan
                Tasmania
                illinois
                Eastern Cape
                Baja California
                Manipur
                Waals-Brabant
                Central Region
                Upper Austria
                Rivers
                Jammu and Kashmir
                Arizona
                Central Kalimantan
                imo
                Riau islands
                Magallanes y Antartica Chilena
                Bihar
                Nordland
                Gelderland
                Leinster
                South Australia
                Diyarbakir
                North island
                Lambayeque
                North-East Region
                Gavleborgs lan
                Northern Mindanao
                Santander
                Puntarenas
                Limousin
                South island
                Eastern Cape
                Antofagasta
                Trondelag
                Bourgogne
                Brussels Hoofdstedelijk Gewest
                West Region
                Puglia
                Agder
                Khanh Hoa
                ilocos Region
                Piura
                Central Visayas
                Central Region
                Pomorskie
                Leinster
                ostergotlands lan
                Lam Dong
                Tyrol
                ostergotlands lan
                Piura
                Overijssel
                North-East Region
                Central Region
                Odessa oblast
                Loreto
                La Guajira
                Flevoland
                Connacht
                Zhongnan
                Ulster
                Queretaro
                Biobio
                West Region
                Gilgit Baltistan
                Sindh
                Minas Gerais
                Zuid Holland
                Veracruz
                Northern Territory
                Maharastra
                Puntarenas
                Chernivtsi oblast
                Free State
                Delhi
                Trondelag
                Oost-Vlaanderen
                Southeast Sulawesi
                Munster
                Champagne-Ardenne
                Jambi
                Ulster
                Manipur
                North Jeolla
                British Columbia
                Vaupes
                Bengkulu
                Zhongnan
                Central Region
                Surrey
                Eastern Cape
                Azad Kashmir
                Riau islands
                Chihuahua
                Huadong
                Luik
                South island
                Noord Holland
                Northwest Territories
                Corse
                Sevastopol City
                Berlin
                Queensland
                Limpopo
                Saarland
                Coquimbo
                Sonora
                Umbria
                Mizoram
                National Capital Region
                South island
                Ontario
                Limon
                Vienna
                Nordland
                Extremadura
                Jonkopings lan
                Biobio
                Guainia
                Maule
                Heredia
                Henegouwen
                Rio de Janeiro
                Sachsen
                Vastra Gotalands lan
                ostergotlands lan
                Leinster
                Victoria
                Niedersachsen
                FATA
                Coahuila
                Southwestern Tagalog Region
                North-East Region
                Nordland
                Puntarenas
                Berlin
                Niedersachsen
                Cartago
                South island
                Araucania
                South Gyeongsang
                Limon
                Jammu and Kashmir
                Eastern Visayas
                Cordoba
                Tula Oblast
                Munster
                Vienna
                Kaliningrad Oblast
                Zakarpattia oblast
                Sindh
                Moscow Oblast
                South Chungcheong
                Hertfordshire
                Piura
                Bursa
                Limon
                Virginia
                North island
                Connecticut
                Andaman and Nicobar islands
                Khyber Pakhtoonkhwa
                San Jose
                Picardie
                Newfoundland and Labrador
                Aquitaine
                Angus
                Comunitat Valenciana
                Parana
                Murmansk Oblast
                Carinthia
                Limpopo
                Argyllshire
                Puntarenas
                Andalucia
                Picardie
                Niger
                Punjab
                Belgorod Oblast
                Colorado
                Junin
                Nuevo Leon
                Tyrol
                Stockholms lan
                Cajamarca
                Andaman and Nicobar islands
                Riau
                Vestfold og Telemark
                Sardegna
                North Jeolla
                Tabasco
                indiana
                Ulster
                Orenburg Oblast
                Saratov Oblast
                Heredia
                Kaluga Oblast
                Midi-Pyrenees
                Basse-Normandie
                Vienna
                Atacama
                Bursa
                """;
    }

}
