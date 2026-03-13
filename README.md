# Herbrich Nexus App

Die **Herbrich Nexus App** ist eine native Android‑Anwendung zur Darstellung und Exploration von **Nodes** aus dem Herbrich‑Ökosystem.  
Sie dient als mobiler Client für die öffentliche REST‑API unter **https://api.herbrich.org** und stellt Orte, Entitäten und Beziehungen strukturiert dar.

Die App ist **API‑first**, **Entity‑zentriert** und nutzt moderne Android‑Technologien wie **Jetpack Compose** und **Android App Links**.

---

## ✨ Features

- Anzeige von Node‑Listen (Grid / Kacheln)
- Detailseiten für einzelne Nodes
- Unterstützung für Bilder, Beschreibungen und Metadaten
- Native Android App Links (`https://www.herbrich.org/node/{uuid}`)
- Moderne UI mit Jetpack Compose
- Klare Trennung von UI, Daten und API‑Zugriff

---

## 🧱 Architektur

- **Sprache:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Netzwerk:** REST‑API (JSON)
- **Backend:** https://api.herbrich.org
- **Design‑Ansatz:** Entity / Node‑basiert

Die App ist bewusst **leichtgewichtig** gehalten und folgt einem klaren Datenfluss:

## 📱 App Links & Deep Linking

Die App unterstützt **verifizierte Android App Links**.

Beispiel: https://www.herbrich.org/node/{uuid}

Öffnet – sofern installiert – direkt die App und zeigt die entsprechende Node‑Detailseite an.  
Fallback erfolgt automatisch über den Browser.

---

## 📂 Projektstruktur (Auszug)

app/
├── ui/
│   ├── theme/
│   ├── node/
│   │   └── JenniferHerbrichNodeActivity.kt
│   └── MainActivity.kt
├── api/
│   └── HerbrichApiService.kt
├── network/
│   └── RetrofitClient.kt
└── AndroidManifest.xml

## 🚀 Build & Run

### Voraussetzungen

- Android Studio (aktuelle Version empfohlen)
- Android SDK (API 24+)
- Kotlin

### Starten

1. Repository klonen
2. Projekt in Android Studio öffnen
3. Gradle Sync ausführen
4. App auf Emulator oder Gerät starten

---

## 🔗 Backend / API

Die App konsumiert Daten aus der öffentlichen REST‑API:https://api.herbrich.org/v1/

Beispiel-Endpunkte:

- `/nodes`
- `/nodes/{uuid}`
- `/nodes/{uuid}/image`

## 🔒 Sicherheit & Identität

- App Links sind über `assetlinks.json` verifiziert
- API‑Zugriffe erfolgen read‑only
- Keine sensiblen Nutzerdaten werden gespeichert

## 🧭 Ziel des Projekts

Herbrich Nexus ist Teil eines größeren Ökosystems zur **strukturierten Darstellung von Wissen, Orten und Beziehungen**.  
Die App dient als mobiler Zugangspunkt zu diesem Graphen.

## 📜 Lizenz

Dieses Projekt steht unter einer freien Lizenz.  
Details siehe `LICENSE` (falls vorhanden).

## 👤 Autor

**Herbrich Corporation**  
https://www.herbrich.org
