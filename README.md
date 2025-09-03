# Projekt: Autonomes Fahrzeug – Klassifikation für Rückkehr zur Basis

Dieses Repository enthält den Code und die Dokumentation für ein Projekt, in dem ein **autonomes Fahrzeug** ein großes Gelände erkundet. Ziel ist es, einen **Klassifikator** zu entwickeln, der erkennt, wann das Fahrzeug zur Basis zurückkehren muss, um Schäden zu vermeiden.

## Ausgangssituation

* Das Fahrzeug erkundet eigenständig ein Gelände.
* In bestimmten Situationen muss die Erkundung **abgebrochen und zur Basis zurückgekehrt** werden, um Schäden zu vermeiden.
* Die Entscheidung zur Rückkehr soll das Fahrzeug **autonom** treffen.

## Kostenmodell

* Weiterfahren trotz notwendiger Rückkehr → **Kosten = 8**
* Rückkehr im richtigen Moment → **Kosten = 2**
* Rückkehr, obwohl nicht nötig → **Kosten = 4**
* Ziel: **minimierung der Gesamtkosten**.

## Sensordaten

* Das Fahrzeug besitzt ein Sensorsystem, das **zeitlich aufeinanderfolgende Messwert-Sequenzen** liefert.
* Jeder Messwert ist eine einzelne ganze Zahl.
* Beispiel für eine Sequenz: `49 21 54 35 23 ...`

## Klassifikationsaufgabe

* Aufgabe: Erkennen, ob eine Sequenz eine **gefährliche Situation** (Rückkehr nötig) oder **harmlose Situation** (Erkundung fortsetzen) darstellt.
* Historische Daten zeigen Unterschiede in den Abfolgen der Sensorwerte zwischen gefährlichen und harmlosen Situationen.
* Klassifikation kann mittels **Markovkette erster Ordnung** erfolgen.
* Wahrscheinlichkeiten: 90% harmlose Situationen, 10% gefährliche Situationen.

## Zielsetzung

* Entwicklung eines Klassifikators, der basierend auf den Sensordaten **korrekt entscheidet**, wann eine Rückkehr zur Basis notwendig ist.
* Minimierung der **Betriebskosten** durch richtige Entscheidungen.

## Hinweise

* Das Projekt simuliert **entscheidungsbasierte Klassifikation unter Unsicherheit**.
* Optimierung kann z. B. über die Analyse von Übergangswahrscheinlichkeiten und Sequenzmustern erfolgen.

## Kontakt

* **Autorin:** Tamara Boerner
* **Projekt:** Autonomes Fahrzeug – Klassifikation für Rückkehr zur Basis
* **Hochschule:** TH Lübeck, Studiengang Informatik/Softwaretechnik
