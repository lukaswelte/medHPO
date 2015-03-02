footer: Integration der Human Phenotype Ontology (HPO) in ein medizinisches Forschungsnetz - Lukas Welte
slidenumbers: true

# Integration der Human Phenotype Ontology (HPO) in ein medizinisches Forschungsnetz
### Lukas Welte

---

# Inhalt
1. Motivation
1. Datenerfassung in der medizinischen Forschung
1. Anforderungen
1. Implementierung
1. Fazit

---

# 1. Motivation
- Viele potentielle Daten in Freitexten
- Keine Kontrolle der Freitexte

---
# 2. Datenerfassung in der medizinischen Forschung
1. Strukturierte Datenerfassung
   1. Ontologie
   1. Natural Language Processing
1. Freitext Datenerfassung

---
# 2.1 Strukturierte Datenerfassung 
- Schematische Daten
- Je mehr Schema desto mehr Struktur
- Verbessert Wiederverwendbarkeit und ist maschninenverarbeitbar

---
# 2.1.1 Ontologie
- Philosophie: Einteilung des Seienden under Möglichkeit
- Informatik: Spezifizierung einer Konzeptionalisierung
    - Teilt Entitäten in Begriffe und Relationen
    
---
# 2.1.1 Ontologie - Human Phenotype Ontology
- Phänotypische Abnormalitäten
- Integriert vorhandene Ontologien
- mehr als 11000 Terme
- über 115000 Annotationen

---
# 2.1.2 Natural Language Processing
- Maschinelle Verarbeitung natürlicher Sprache
- Umwandlung von Freitexten in strukturierte Daten

---
# 2.1.2 Natural Language Processing - Funktion
1. Spracherkennung
1. Tokenisierung
1. Morphologische Analyse
1. Syntaktische Analyse
1. Semantische Analyse
1. Dialog und Diskursanalyse

---
# 2.2 Freitext Datenerfassung
- Gegenteil der Strukturierten Datenerfassung
- Einfache Erfassung
- Schwere Auswertung
 
---
# 3. Anforderungen
1. Visitenbrowser
1. Visiten Detail
1. Term Editor
1. Daten Auswertung
1. Deidentifizierung der Daten
1. Daten Integrität

---
![](visitenoverview.png)
# 3.1 Visitenbrowser
- Einfacher und schneller Zugriff auf Visiten
- Basisinformationen einer Visite

---
![](visitendetail.png)
# 3.2 Visiten Detail
- Überblick über gefundene Terme
- Zusatzinformationen zu Termen
- Löschen eines Terms
- Manuelles Ergänzen von Termen

---
![](addterm.png)
# 3.3 Visiten Editor
- Zuordnen von Wörtern zu einem Term

---
# 3.4 Daten Auswertung
- Anfallende Daten können ausgewertet werden
- Persistierung in statistisch auswertbarem Format 

---
# 3.5 Deidentifizierung der Daten
- Ersetzen aller Namen durch ```[patient]```
- Erleichtert weitergabe der Texte an Dritte

---
# 3.6 Daten Integrität
- Nur lesender Zugriff auf HPO und klinik Datenbank
- Ermöglicht Plug and Play Applikation

---
# 4. Implemetierung
1. Technologiestack
1. Termsuche
1. Persistierung
1. Demo
1. Probleme

---
# 4.1 Technologiestack
- JSF
- Glassfish
- MySQL
- OpenNLP

---
# 4.2 Termsuche
1. Filtern von Elementen
1. Gruppierung von Elementen
1. Suche in der HPO
1. Trefferauswertung

---
# 4.3 Persistierung
- In Programmeigener Datenbank gespeichert
- Ein Datensatz je Analysedurchlauf

^ Datenbanken Diagramm

---
# 4.4 Demo

---
# 4.5 Probleme
- OpenNLP Erkennungs Modelle
- RAM Verbrauch
- OpenNLP Verarbeitungszeit
- Erkennung von Namen
- Datenbank Abfragen

---
# 5. Fazit
1. Ergebnis
1. Verbesserungsmöglichkeiten
1. Ausblick

---
# 5.1 Ergebnis
- Auswertung bestehender Daten
- Standardisierung der Daten
- HPO könnte schnell weiterentwickelt werden

---
# 5.2 Verbesserungsmöglichkeiten
- Optimierung der Datenbank Abfragen
- Verwendung einer verteilten Datenbank optimiert für die Suche (ElasticSearch)
- Konfigurierbarer gestalten
- Auch ohne UI verwendbar machen

---
# 5.3 Ausblick
- Automatische Diagnoseerstellung aus Freitext
- Präzise Epidemie Auswertung
