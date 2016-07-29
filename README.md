[![Build Status](https://travis-ci.org/bitctrl/de.bsvrz.dua.progglaette.svg?branch=develop)](https://travis-ci.org/bitctrl/de.bsvrz.dua.progglaette)
[![Build Status](https://api.bintray.com/packages/bitctrl/maven/de.bsvrz.dua.progglaette/images/download.svg)](https://bintray.com/bitctrl/maven/de.bsvrz.dua.progglaette)

# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.14 SWE Glättewarnung und -prognose

Version: ${version}

## Übersicht

Die SWE Glättewarnung und -prognose ermittelt aus den aktuell gültigen und den 
in letzten 10 Minuten eingelaufenen messwertersetzten Daten der Fahrbahnoberflächen-
und Taupunkttemperatur sowie dem momentan gemessenen gültigen Fahrbahnzustand und der
aktuell gültigen Lufttemperatur einer Umfelddatenmessstelle eine Aussage über eine
mögliche Glätte. Des Weiteren wird für die Prognosehorizonte 5-, 15-, 30-, 60- und
90-Minuten eine entsprechende Prognose berechnet und publiziert.

## Versionsgeschichte

### 2.0.2

Release-Datum: 28.07.2016

de.bsvrz.dua.progglaette.tests.DuAProgGlaetteTestBase
- der Member "_glaetteWarnungUndPrognose" sollte nicht statisch sein, der er bei jedem Test neu initialisiert wird

- Javadoc für Java8-Kompatibilität korrigiert
- Obsolete SVN-Tags aus Kommentaren entfernt
- Obsolete inheritDoc-Kommentare entfernt

### 2.0.1

Release-Datum: 22.07.2016

- Umpacketierung gemäß NERZ-Konvention

### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua
in Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0,
sowie die Kernsoftware in Mindestversion 3.8.0.

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Ist kein Taupunkttemperatursensor vorhanden oder liefert dieser keine gültigen
  Daten, dann wird stattdessen die berechnete Taupunkttemperatur-Fahrbahn aus
  der Datenaufbereitung verwendet.

#### Fehlerkorrekturen

Folgende Fehler gegenüber vorhergehenden Versionen wurden korrigiert:

- Tippfehler in Warnmeldung: Fehler bei der Initialisierung des EntscheidunsBaumes.

### 1.4.0

- Umstellung auf Java 8 und UTF-8

### 1.3.0

- Umstellung auf Funclib-BitCtrl-Dua

### 1.2.0

- Umstellung auf Maven-Build
- Behandlung nicht unterstützter Sensorarten über die 'UmfeldDatenSensorUnbekannteDatenartException'
- benötigt SWE_de.bsvrz.sys.funclib.bitctrl_FREI_V1.2.3.zip oder höher 

### 1.0.1

- Bash-Startskript hinzu

### 1.0.0

- Erste Auslieferung
  
## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
