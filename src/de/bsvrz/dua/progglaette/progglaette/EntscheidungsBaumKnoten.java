/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.14 Gl�ttewarnung und -prognose
 * 
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.progglaette.progglaette;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;

/**
 * Ein Knoten des Eintscheidungsbaumes
 * Enthaelt Referenzen fuer seine Nachfolger und eine
 * Entscheidungsmethode, die die Richtung bestimmt
 * Fals es ein EndKnoten ist, dann enthaelt den Ergebnisswert.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class EntscheidungsBaumKnoten {
	
	/**
	 *  Die WertKonstant fuer nicht ermittelbare Werte
	 */
	public static final int EBK_TEMPERATUR_NICHT_ERMITTELBAR = -1001;
	
	/**
	 * Nachfolgende Knoten Links
	 */
	protected EntscheidungsBaumKnoten nachfolgerLinks = null;
	
	/**
	 * Nachfolgende Knoten Rechts
	 */
	protected EntscheidungsBaumKnoten nachfolgerRechts = null;

	/**
	 * Nachfolgende Knoten in der MitteLinks
	 */
	protected EntscheidungsBaumKnoten nachfolgerMitteL = null;
	/**
	 * Nachfolgende Knoten in der MitteRechts
	 */
	protected EntscheidungsBaumKnoten nachfolgerMitteR = null;
	
	/**
	 * Die EntscheidungsMethode des Knotens
	 */
	private EntscheidungsMethode methode = null; 
	
	/**
	 * Ergebnisswert, dem ein Endknoten leifert
	 */
	private int ergebnissWert = -1;
	
	/**
	 * Die Richtungen in dennen man im Baum weitergehen kann
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private enum Richtung {
		R_LINKS,  							// Richtung nach Links
		R_MITTE_LINKS,  					// Richtung in der MitteLinks
		R_MITTE_RECHTS,  					// Richtung in der MitteRechts
		R_RECHTS,							// Richtung nach Rechts
		R_NICHT_ERMITTELBAR,				// Richtung keine ( z.B. Wert nicht verfuegbar )
		R_TENDENZBERECHNUNG_NICHT_MOEGLICH, // Richtung keine ( z.B. Tendenzwert nicht verfuegbar )
	};
	
	/**
	 *  Die EntscheidungsMethode des Knotens, entscheidet in welcher Richtung man
	 *  weitergeht
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public interface EntscheidungsMethode {
		/**
		 * Entscheidet in welcher Richtung man weitergehen soll
		 * 
		 * @param fbzAktuell Fahrbahnzustand aktuell
		 * @param fbtAktuell FahrbahnTemperatur aktuell
		 * @param tptAktuell Taupunkttemperatur aktuell
		 * @param lftAktuell Lufttemperatur aktuell 
		 * @param fbtExtrapoliert Fahrbahntemperatur extrapoliert im Prognosehorizont
		 * @param tptExtrapoliert Taupunkttemperatur extrapoliert im Prognosehorizont
		 * @param horizont Prognosehorizont
		 * @return Richtung 
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell, double tptAktuell, double lftAktuell, double fbtExtrapoliert, double tptExtrapoliert);
	}
	
	/**
	 * Abstrakte Klasse fuer die Entscheidung nach Temperatur
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static abstract class EntscheidungTemperatur implements EntscheidungsMethode {
		/**
		 * Grenzwert fuer die Entscheidung
		 */
		private double grenzWert = 0;
		
		/**
		 *  Operator, dem man anwenden soll, um den Grenzwert mit aktuellen Wert zu vergleichen  
		 */
		private EntscheidungsBaum.Operator operator = null;
		
		/**
		 *  Der aktuelle Wert
		 */
		protected double wert = 0;
		
		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts 
		 */
		public EntscheidungTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			this.grenzWert = grenzWert;
			this.operator = operator;
		}
		
		/**
		 * Mit Anwendung des Operators entscheidet, in welcher Richtung man weitergehen soll
		 * 
		 * @param fbzAktuell Fahrbahnzustand aktuell
		 * @param fbtAktuell FahrbahnTemperatur aktuell
		 * @param tptAktuell Taupunkttemperatur aktuell
		 * @param lftAktuell Lufttemperatur aktuell 
		 * @param fbtExtrapoliert Fahrbahntemperatur extrapoliert im Prognosehorizont
		 * @param tptExtrapoliert Taupunkttemperatur extrapoliert im Prognosehorizont
		 * @param horizont Prognosehorizont
		 * @return Richtung in welcher man weiter
		 */
		protected final Richtung auswerte(long fbzAktuell, double fbtAktuell, double tptAktuell, double lftAktuell, double fbtExtrapoliert, double tptExtrapoliert) {
			if(operator.anwende(wert, grenzWert)) return Richtung.R_LINKS;
			else return Richtung.R_RECHTS;
		}
	}
	
	/**
	 * Trifft Entscheidung nach der Fahrbahnoberflaechentemperatur
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FbofTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator  - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts
		 */
		public FbofTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			if(fbtAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return Richtung.R_NICHT_ERMITTELBAR;
			wert = fbtAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
	}
		

	/**
	 * Trifft Entscheidung nach der Lufttemperatur
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class LuftTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator  - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts
		 */
		public LuftTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			if(lftAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return Richtung.R_NICHT_ERMITTELBAR;
			wert = lftAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
	}
	
	/**
	 * Trifft Entscheidung nach der Differenz der Fahrbahnoberflaechen- und Taupunkttemperatur
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class DifferenzFbofTaupunktTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator  - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts
		 */
		public DifferenzFbofTaupunktTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			if(fbtAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR ||
					tptAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return Richtung.R_NICHT_ERMITTELBAR;
			wert = fbtAktuell - tptAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach der Differenz der Prognose der Fahrbahnoberflaechen- und  Taupunkttemperatur
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class DifferenzPrognoseFbofTaupunktTemperatur extends EntscheidungTemperatur {
		
		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator  - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts
		 */
		public DifferenzPrognoseFbofTaupunktTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			if(fbtExtrapoliert == EBK_TEMPERATUR_NICHT_ERMITTELBAR ||
					tptExtrapoliert == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			wert = fbtExtrapoliert - tptExtrapoliert;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
	}
	
	/**
	 * Trifft Entscheidung nach der  Prognose der Fahrbahnoberflaechentemperatur
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FbofPrognoseTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor
		 * @param grenzWert Grenzwert
		 * @param operator Operator  - wenn als <code>true</code> ausgewertet, geht nach links, sonst rechts
		 */
		public FbofPrognoseTemperatur(double grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			if(fbtExtrapoliert == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			wert = fbtExtrapoliert;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
	}
	
	/**
	 * Trifft Entscheidung nach dem Fahrbahnoberflaechenzustand
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FahbrBahnZustand implements EntscheidungsMethode {
		/**
		 * Wertenmenge, bei denen man nach links geht 
		 */
		protected long [] werteLinks = null;
		/**
		 * Wertenmenge, bei denen man nach rechts geht 
		 */
		protected long [] werteRechts = null;
		/**
		 * Standardkonstruktor
		 * @param werteLinks Wertenmenge, bei denen man nach links geht 
		 * @param werteRechts Wertenmenge, bei denen man nach rechts geht
		 */
		public FahbrBahnZustand(long [] werteLinks, long [] werteRechts) {
			this.werteLinks = werteLinks;
			this.werteRechts = werteRechts;
		}
		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			
			for(int i=0; i<werteLinks.length; i++) 
				if(werteLinks[i] == fbzAktuell) 
					return Richtung.R_LINKS;
				
			for(int i=0; i<werteRechts.length; i++) 
				if(werteRechts[i] == fbzAktuell) 
					return Richtung.R_RECHTS;
				
			return Richtung.R_NICHT_ERMITTELBAR;
		}
	}
	
	/**
	 * Trifft Entscheidung nach dem Fahrbahnoberflaechenzustand, erweitert es mit 
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FahbrBahnZustandVollDefiniert extends FahbrBahnZustand {
		
		/**
		 * Wertenmenge, bei denen man in die Mitte Links weitergeht 
		 */
		protected long [] werteMitteL = null;
		/**
		 * Wertenmenge, bei denen man in die Mitte Rechts weitergeht 
		 */
		protected long [] werteMitteR = null;
		
		/**
		 * Standardkonstruktor
		 * @param werteLinks Wertenmenge, bei denen man nach links geht 
		 * @param werteMitte Wertenmenge, bei denen man in die Mitte geht
		 * @param werteRechts Wertenmenge, bei denen man nach rechts geht
		 */
		public FahbrBahnZustandVollDefiniert(long [] werteLinks, long [] werteMitteL, long [] werteMitteR, long [] werteRechts) {
			super(werteLinks, werteRechts);
			this.werteMitteL = werteMitteL;
			this.werteMitteR = werteMitteR;
		}
		/**
		 * {@inheritDoc}
		 */
		public Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert) {
			
			for(int i=0; i<werteLinks.length; i++) 
				if(werteLinks[i] == fbzAktuell) 
					return Richtung.R_LINKS;
				
			for(int i=0; i<werteMitteL.length; i++) 
				if(werteMitteL[i] == fbzAktuell) 
					return Richtung.R_MITTE_LINKS;
			
			for(int i=0; i<werteMitteR.length; i++) 
				if(werteMitteR[i] == fbzAktuell) 
					return Richtung.R_MITTE_RECHTS;
			
			for(int i=0; i<werteRechts.length; i++) 
				if(werteRechts[i] == fbzAktuell) 
					return Richtung.R_RECHTS;
			
			return Richtung.R_NICHT_ERMITTELBAR;
		}
	}
		
	
	/**
	 * Auswertet den Ergebniss der Glaetteprognose, mit Hilfe der Entscheidungsmethode 
	 * recursiv geht weiter in den Nachfolgerknoten bis der Prognosewert nicht gefunden wird
	 * 
	 * @param fbzAktuell Fahrbahnzustand aktuell
	 * @param fbtAktuell FahrbahnTemperatur aktuell
	 * @param tptAktuell Taupunkttemperatur aktuell
	 * @param lftAktuell Lufttemperatur aktuell 
	 * @param fbtExtrapoliert Fahrbahntemperatur extrapoliert im Prognosehorizont
	 * @param tptExtrapoliert Taupunkttemperatur extrapoliert im Prognosehorizont
	 * @return die Glaetteprognose
	 */
	public int getPrognose(long fbzAktuell, double fbtAktuell, double tptAktuell, double lftAktuell, double fbtExtrapoliert, double tptExtrapoliert) {
		// Endknoten
		if(methode == null) return ergebnissWert;
		
		Richtung r = methode.getRichtung(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		
		if(r == Richtung.R_LINKS) {
			return nachfolgerLinks.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
		else if(r == Richtung.R_RECHTS)  {
			return nachfolgerRechts.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
		else if(r == Richtung.R_MITTE_LINKS)  {
			return nachfolgerMitteL.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
		else if(r == Richtung.R_MITTE_RECHTS)  {
			return nachfolgerMitteR.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		}
		else if(r == Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH) {
			return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
		}
		else if(r == Richtung.R_NICHT_ERMITTELBAR) {
			return EntscheidungsBaum.EB_NICHT_ERMITTELBAR;
		}
		// nur wegen Compiler
		return EntscheidungsBaum.EB_NICHT_ERMITTELBAR;
	}
	
	/**
	 * Konstruktor
	 * @param methode Die EntscheidungsMethode
	 * @param nachfolgerLinks Der Nachfolger an der linken Seite
	 * @param nachfolgerRechts Der Nachfolger an der rechten Seite
	 * @throws DUAInitialisierungsException 
	 */
	public EntscheidungsBaumKnoten(EntscheidungsMethode methode, EntscheidungsBaumKnoten nachfolgerLinks, EntscheidungsBaumKnoten nachfolgerRechts) 
	throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null || methode == null)
			throw new DUAInitialisierungsException("Kein parameter darf null sein");
		if( methode instanceof FahbrBahnZustandVollDefiniert)
			throw new DUAInitialisierungsException("Knoten mit Entscheidungsmethode Fahrbahnzustandmitglaette  soll 3 Nachfolger haben");		
		this.nachfolgerLinks = nachfolgerLinks;
		this.nachfolgerRechts = nachfolgerRechts;
		this.methode = methode;
		
	}
	
	/**
	 * Konstruktor
	 * @param methode Die EntscheidungsMethode
	 * @param nachfolgerLinks Der Nachfolger an der linken Seite
	 * @param nachfolgerMitteLinks Der Nachfolger in der Mitte nach Links
	 * @param nachfolgerMitteRechts Der Nachfolger in der Mitte nach Rechts
	 * @param nachfolgerRechts Der Nachfolger an der rechten Seite
	 * @throws DUAInitialisierungsException 
	 */
	public EntscheidungsBaumKnoten(EntscheidungsMethode methode, EntscheidungsBaumKnoten nachfolgerLinks, EntscheidungsBaumKnoten nachfolgerMitteLinks, 
				EntscheidungsBaumKnoten nachfolgerMitteRechts, EntscheidungsBaumKnoten nachfolgerRechts) 
	throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerMitteLinks == null 
				|| nachfolgerMitteRechts == null || nachfolgerRechts == null || methode == null)
			throw new DUAInitialisierungsException("Kein parameter darf null sein");
		this.nachfolgerLinks = nachfolgerLinks;
		this.nachfolgerMitteL = nachfolgerMitteLinks;
		this.nachfolgerMitteR = nachfolgerMitteRechts;
		this.nachfolgerRechts = nachfolgerRechts;
		this.methode = methode;
	}
	
	/**
	 * Konstruktor
	 * @param ergebnisswert Glaetteprognosewert
	 */
	public EntscheidungsBaumKnoten(int ergebnisswert) {
		this.methode = null;
		this.ergebnissWert = ergebnisswert;
	}
}