# Warehouse ERP - Projekt Edukacyjny (Backend & Data Logic Focus)

### 🚀 O projekcie
Projekt ten jest etapem mojej nauki programowania w języku Java. Głównym celem nie było stworzenie "ładnych okienek", ale zrozumienie, jak zaprojektować **logikę biznesową**, zarządzać **obiektami (POJO)** oraz jak skutecznie komunikować aplikację z **relacyjną bazą danych (PostgreSQL)**.

### 🎯 Cele edukacyjne (Na co zwrócić uwagę):
W tym projekcie skupiłem się przede wszystkim na:

* **Modelowaniu Danych:** Stworzenie klas `Product`, `Supplier` oraz `Order` z pełną enkapsulacją (prywatne pola, przemyślane gettery i settery).
* **Architekturze DAO (Data Access Object):** Implementacja klasy `DatabaseManager`, która oddziela zapytania SQL od logiki interfejsu.
* **Relacjach SQL:** Zastosowanie relacji 1:N (Jeden-do-Wielu) pomiędzy dostawcami a produktami oraz obsługa kluczy obcych (`Foreign Keys`).
* **Integralności Danych:** Logika w Javie sprawdzająca stany magazynowe przed zatwierdzeniem zamówienia (walidacja biznesowa).
* **Zarządzaniu Cyklem Życia Bazy:** Automatyczna inicjalizacja tabel przy starcie aplikacji (`CREATE TABLE IF NOT EXISTS`).

### 💻 Technologie
* **Język:** Java 17+
* **Baza danych:** PostgreSQL (JDBC)
* **Budowanie projektu:** Maven
* **Interfejs (Wizualizacja):** JavaFX (użyty jako narzędzie do prezentacji działania logiki backendowej).

### 🏗️ Struktura Projektu
* `src/main/java/warehouse/` - Główny kod aplikacji.
    * `Product`, `Supplier`, `Order` - Klasy modelowe odzwierciedlające strukturę bazy.
    * `DatabaseManager` - Serce aplikacji; obsługa CRUD i połączenia JDBC.
    * `MainApp` - Kontroler interfejsu i orchestrator procesów.
* `schema.sql` - Pełny schemat bazy danych dla PostgreSQL.

### 🛠️ Jak uruchomić
1.  Skonfiguruj bazę PostgreSQL i bazę o nazwie `warehouse_erp`.
2.  W pliku `DatabaseManager.java` podaj swoje dane dostępowe (USER/PASSWORD).
3.  Uruchom projekt przez Maven: `mvn javafx:run`.
4.  Tabele zostaną stworzone automatycznie przy pierwszym uruchomieniu.

---
*Projekt ma charakter edukacyjny i służy do prezentacji postępów w nauce fundamentów backendu i baz danych.*