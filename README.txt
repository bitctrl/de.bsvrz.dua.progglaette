***********************************************************************************************
*  Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.14 SWE Gl�ttewarnung und �prognose  *
***********************************************************************************************

Version: 1.2.0


�bersicht
=========

Die SWE Gl�ttewarnung und �prognose ermittelt aus den aktuell g�ltigen und den 
in letzten 10 Minuten eingelaufenen messwertersetzten Daten der Fahrbahnoberfl�chen-
und Taupunkttemperatur sowie dem momentan gemessenen g�ltigen Fahrbahnzustand und der
aktuell g�ltigen Lufttemperatur einer Umfelddatenmessstelle eine Aussage �ber eine
m�gliche Gl�tte. Des Weiteren wird f�r die Prognosehorizonte 5-, 15-, 30-, 60- und
90-Minuten eine entsprechende Prognose berechnet und publiziert.


Versionsgeschichte
==================

1.2.0
- Umstellung auf Maven-Build

1.0.0

  - Erste Auslieferung
  
1.0.1

  - Bash-Startskript hinzu

Diese SWE ist eine eigenst�ndige Datenverteiler-Applikation, welche �ber die Klasse
de.bsvrz.dua.progglaette.progglaette.GlaeteWarnungUndPrognose mit folgenden Parametern
gestartet werden kann (zus�tzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)
	
	
- Tests:

Alle Tests befinden sich unterhalb des Verzeichnisses junit und sind als JUnit-Tests ausf�hrbar.
Die Tests untergliedern sich wie folgt:
	- DAV-Tests: Tests mit Datenaustausch �ber eine Datenverteiler-Schnittstelle (bei der
	  Durchf�hrung dieser Tests wird jeweils implizit eine Instanz der Gl�ttewarnung und -prognose
	  gestartet)
	  	- GlaetteWarnungUndPrognoseTest.java: Innerhalb dieses Tests werden �ber den Datenverteiler
	  	  verschiedene Vektoren (LFT, FBT, TPT, FBZ) eingegeben, deren Elemente jeweils teilweise nicht
	  	  gesendet werden (also z.B. (0, 0, x, 0) -> es wird kein TPT-Datum mit diesem Zeitstempel
	  	  verschickt). Es soll �berpr�ft werden, ob auch bei unvollst�ndigen Eingaben eine Ausgabe
	  	  durch die SWE vorgenommen wird.
	  	   
	- Einzel-Tests: Tests von Teilfunktionalit�ten einzelner SW-Elemente (die Daten werden dabei
	  nur �ber Java-Funktionsschnittstellen ausgetauscht):
		- EntscheidungsBaumTest.java: Innerhalb dieser Klasse werden unterschiedliche Vektoren in die
		  Implementierung des Entscheidungsbaums eingegeben und die entsprechende Ausgabe (Ist, Soll)
		  verglichen.
		- PrognoseZustandTest.java: Diese Klasse testet die Implementierung der Trendextrapolation 

Voraussetzungen f�r die DAV-Tests:
- Start der Test-Konfiguration (extra/test_konfig_progglaette.zip)
- Anpassung der DAV-Start-Parameter (Variable CON_DATA) innerhalb von 
	junit/de.bsvrz.dua.progglaette.progglaette.GlaetteWarnungUndPrognoseTest.java	

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", 
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd",
			"-debugLevelStdErrText=WARNING",
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBeriechsPid=kb.glaetteProgTest"};


Alle Tests wurden so bereits erfolgreich ausgef�hrt.


Disclaimer
==========

Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.14 SWE Gl�ttewarnung und �prognose
Copyright (C) 2007 BitCtrl Systems GmbH 

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


Kontakt
=======

BitCtrl Systems GmbH
Wei�enfelser Stra�e 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
