# OlxSniperBot

*Disclaimer: This bot works exclusively with the polish website [olx.pl](https://olx.pl) and therefore the description is in polish.*
![OlxSniperBot on Telegram mobile app](https://i.imgur.com/Vpygswq.png)

## O bocie

OlxSniper jest botem do Telegrama napisanym w Javie w oparciu o oficjalne API [TelegramBots](https://github.com/rubenlagus/TelegramBots) oraz bibliotekę [HtmlUnit](http://htmlunit.sourceforge.net/). Używając techniki tzw. web scrappingu bot parsuje na bieżąco pojawiające się oferty wynajmu mieszkań ze strony [olx](https://olx.pl) i wysyła natychmiastowe powiadomienia z podsumowaniem każdej nowo wystawionej oferty.

## Przypadek użycia

Rosnący popyt na wynajem mieszkania sprawił, że znalezienie sobie lokum w większych miastach bywa czasami bardzo trudne. W okresie wakacyjnym oferty już paręnaście sekund po ich wystawieniu są "nieaktualne" czy też "zarezerwowane". Jeżeli więc odpada wynajęcie mieszkania po znajomości to zostaje nam siedzieć na stronie z ofertami wynajmu i odświeżać ją co 10 sekund - stąd pomysł na bota z altertami.

#### Dlaczego bot pobiera oferty akurat z OLX?

OLX jest największą bazą ofert wynajmu mieszkań. Nowe ogłoszenie pojawia się tam co ok. kilka minut (w większych miastach).

## Przykładowe grupy z ogłoszeniami 24/7

Te grupy wysyłają powiadomienia o nowych ofertach mieszkań:

* [OLX SnIpEr Warszawa](https://t.me/mieszkania_warszawa) - powiadomienia o ofertach mieszkań na wynajem w Warszawie (bez filtrów cenowych)
* [OLX SnIpEr Kraków](https://t.me/mieszkania_krakow) - powiadomienia o ofertach mieszkań na wynajem w Krakowie (bez filtrów cenowych)
* [OLX SnIpEr Gdańsk](https://t.me/mieszkania_gdansk) - powiadomienia o ofertach mieszkań na wynajem w Gdańsku (bez filtrów cenowych)

**Aby dostawać alerty na telefon najlepiej jest pobrać aplikację mobilną Telegram [[Android](https://play.google.com/store/apps/details?id=org.telegram.messenger&hl=pl) / [IOS](https://itunes.apple.com/us/app/telegram-messenger/id686449807?mt=8)] a następnie dołączyć do grupy.**

## Ustawianie własnego bota

**Zalety**
* Możliwość ustawienia dowolnego miasta wspieranego przez OLX
* Możliwość ustawienia filtrów cenowych
* Dostososowanie bota do własnych potrzeb poprzez zmianę kodu

**Wady**
* Bot działa tylko do póki odpalony jest proces na komputerze (przydałby się zewnętrzny VPS)
* Wymogiem jest pewne pojęcie o programowaniu w Javie

#### Wymagania wstępne

* Konto Telegram + [aplikacja Telegram](https://telegram.org/) (może być web app, mobile app lub desktop app)
* Środowisko Java IDE do modyfikacji i kompilacji kodu

#### Tworzenie własnego bota
1. Inicjalizacja bota po stronie Telegram Bot API
	1. Otwórz czat z [@BotFather](https://telegram.me/BotFather)'em na Telegramie - jest do bot, który służy do inicjowania nowych botów.
	1. Użyj komendy `/newbot`, [@BotFather](https://telegram.me/BotFather) zapyta wtedy o nick oraz unikalną nazwę użytkownika dla nowego bota.
	1. Po udanym dostarczeniu nicku oraz nazwy użytkownika otrzymasz komunikat zwrotny z unikalnym kluczem API.
1. Stworzenie grupy Telegramowej, w której będzie operować bot
	1. Utwórz nową grupę na Telegramie.
	1. [Dodaj do niej stworzonego przez siebie bota](https://imgur.com/a/cJqMVFb)
	1. Nadaj botowi prawa administratora grupy (aby mógł wysyłać oraz usuwać wiadomości).
1. Uzyskanie chatID
	1. Upewnij się, że bot ma prawa administratora grupy
	1. Przygotuj następujący link: [api.telegram.org/bot<TWÓJ_API_KEY_TUTAJ>/getUpdates](api.telegram.org/bot<TWÓJ_API_KEY_TUTAJ>/getUpdates) uzupełniając w nim swój API token uzyskany u [@BotFather](https://telegram.me/BotFather)'a. Link powinen wyglądać mniej więcej tak: `https://api.telegram.org/bot722859650:AAE1RE6Uunu_2TinMeRGytXaRnO9EphBOGQ/getUpdates`. 
	1. Uruchom link w przeglądarce.
	1. Napisz kilka losowych wiadomości na stworzonej wcześniej grupie.
	1. Uruchom link ponownie i [znajdź w odpowiedzi chatID](https://imgur.com/a/VqLksyI).
1. Kompilacja własnego OlxSnipera
	1. Pobierz repozytorium z githuba.
	1. Uzupełnij stałe w klasie Main o swój klucz API, unikalny username bota oraz chatID.
	
        ```Java
        private final static String BOT_TOKEN_SECRET = "api_secret";
        private final static String BOT_USERNAME = "bot_username";
        private final static String CHAT_ID = "chat_id";
        ```
     1. Skompiluj i uruchom.
     1. Ustaw bota oraz aktywuj skanowanie przy pomocy komend.

#### Komendy
Na dzień dzisiejszy bot wspiera następujące komendy.
Komenda | Opis | Przykład
------------ | ------------- | -------------
/start | Uruchom skanowanie w poszukiwaniu nowych ofert | -
/stop | Zatrzymaj skanowanie | -
/setup city [city*] | Ustaw miasto docelowe | /setup city warszawa
/setup priceFrom [minimalPrice] | Ustaw dolny filtr cenowy | /setup priceFrom 850
/setup priceTo [maximalPrice] | Ustaw górny filtr cenowy | /setup priceTo 2200
/help | Uzyskaj listę dostępnych komend | -
/say [text] | Komenda pomocnicza. Powiedz coś jako bot | /say Hej to ja bot!


\* - argument podawany w języku polskim