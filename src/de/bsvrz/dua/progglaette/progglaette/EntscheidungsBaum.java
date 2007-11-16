/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.14 Glättewarnung und -prognose
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
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
package de.bsvrz.dua.progglaette.progglaette;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Der Entscheidungsbaum macht eine Glaetteprognose aus den aktuellen 
 * Eigenschaften der Fahrbahn und Luft und seinen Prognosewerten
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class EntscheidungsBaum {
	/**
	 * Konstanden nach dem DatK
	 * 
	 * kb.tmUmfeldDatenGlobal.xml
	 * 
	 */
	public static final int EB_NICHT_ERMITTELBAR	              									=-1;
	public static final int EB_KEINE_GLAETTEGEHFAHR              									= 1;
	public static final int EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG      								= 2;
	public static final int EB_EISGLAETTE_MOEGLICH				 						   			= 3;
	public static final int EB_TENDENZBERECHNUNG_NICHT_MOEGLICH 									= 4;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG 								= 5;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE 			= 6;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT			 			= 7;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT 	= 8;
	public static final int EB_EISGLAETTE_MOEGLICH_SOFORT										 	= 9;
	public static final int EB_GLAETTE_VORHANDEN												 	= 10;
	public static final int EB_EIS_SCHNEE_AUF_DER_FAHRBAHN										 	= 11;
	
	/**
	 * Fahrbahnoberflaechenzustaende
	 */
	public static final long FBZ_TROCKEN 		= 0;
	public static final long FBZ_FEUCHT 		= 1;
	public static final long FBZ_NASS 			= 32;
	public static final long FBZ_GEFR_WASSER	= 64;
	public static final long FBZ_SCHNEE			= 65;
	public static final long FBZ_EIS			= 66;
	public static final long FBZ_RAUREIF		= 67;

	/**
	 * Der Wurzel des Entscheidungbaumes
	 */
	private static EntscheidungsBaumKnoten wurzel = null;
	
	/**
	 * Der Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Operatoren die waehrend der Entscheidung benutzt werden
	 */
	public interface Operator {
		/**
		 * Anwendung des Operators
		 * @param x Operand x
		 * @param y Operand y
		 * @return Ergebnisswert des Operators
		 */
		boolean anwende(double x, double y);
	}
	/**
	 *  Operator <
	 */
	public static class OperatorKleiner implements Operator {
		/**
		 * {@inheritDoc}
		 */
		public boolean anwende(double x, double y) {
			return (x < y);
		}
	}
	/**
	 *  Operator <=
	 */
	public static class OperatorKleinerGleich implements Operator {
		/**
		 * {@inheritDoc}
		 */
		public boolean anwende(double x, double y) {
			return (x <= y);
		}
	}
	/**
	 *  Operator >
	 */
	public static class OperatorGroesser implements Operator {
		/**
		 * {@inheritDoc}
		 */
		public boolean anwende(double x, double y) {
			return (x > y);
		}
	}
	/**
	 *  Operator >=
	 */
	public static class OperatorGroesserGleich implements Operator {
		/**
		 * {@inheritDoc}
		 */
		public boolean anwende(double x, double y) {
			return (x >= y);
		}
	}
	/**
	 * Erzeugt dem EntscheidungsBaum nach der Abbildung in der AFo
	 */
	private static void erzeugeEntscheidungsBaum() {
		EntscheidungsBaumKnoten k1, k2, k3;
		EntscheidungsBaumKnoten sGbNm, sGbNsRm, eM, eSm, gbWm, sGbNSm, sGbNSsRm,  kG, gV, esFb;
		EntscheidungsBaumKnoten.EntscheidungsMethode methode;
	
		try {
			sGbNm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG);
			sGbNsRm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE);
			eM = new EntscheidungsBaumKnoten(EB_EISGLAETTE_MOEGLICH);
			eSm = new EntscheidungsBaumKnoten(EB_EISGLAETTE_MOEGLICH_SOFORT);
			gbWm = new EntscheidungsBaumKnoten(EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG);
			sGbNSm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT);
			sGbNSsRm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT);
			kG = new EntscheidungsBaumKnoten(EB_KEINE_GLAETTEGEHFAHR);
			gV = new EntscheidungsBaumKnoten(EB_GLAETTE_VORHANDEN);
			esFb = new EntscheidungsBaumKnoten(EB_EIS_SCHNEE_AUF_DER_FAHRBAHN);
			
			methode = new EntscheidungsBaumKnoten.DifferenzPrognoseFbofTaupunktTemperatur(0, new OperatorGroesser());
			k1 = new EntscheidungsBaumKnoten(methode, sGbNm, sGbNsRm);
			
			methode = new EntscheidungsBaumKnoten.FahbrBahnZustand(new long [] {FBZ_TROCKEN}, new long [] {FBZ_FEUCHT, FBZ_NASS});
			k2 = new EntscheidungsBaumKnoten(methode, k1, eM);
			
			methode = new EntscheidungsBaumKnoten.FahbrBahnZustand(new long [] {FBZ_FEUCHT, FBZ_NASS}, new long [] {FBZ_TROCKEN});
			k1 = new EntscheidungsBaumKnoten(methode, eM, gbWm);
			
			methode  = new EntscheidungsBaumKnoten.FbofTemperatur(3, new OperatorKleinerGleich());
			k3 = new EntscheidungsBaumKnoten(methode, k1, gbWm);
			
			methode = new EntscheidungsBaumKnoten.FbofPrognoseTemperatur(2, new OperatorKleinerGleich());
			k1 = new EntscheidungsBaumKnoten(methode, k2, k3);
			
			methode = new EntscheidungsBaumKnoten.DifferenzPrognoseFbofTaupunktTemperatur(0, new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, sGbNSm, sGbNSsRm);
			
			methode = new EntscheidungsBaumKnoten.DifferenzFbofTaupunktTemperatur(2, new OperatorGroesser());
			k3 = new EntscheidungsBaumKnoten(methode, k2, sGbNSsRm);
			
			methode = new EntscheidungsBaumKnoten.DifferenzFbofTaupunktTemperatur(0, new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, k3, sGbNSsRm);
			
			methode = new EntscheidungsBaumKnoten.FahbrBahnZustandVollDefiniert(new long [] {FBZ_TROCKEN},
					new long [] {FBZ_FEUCHT, FBZ_NASS }, new long [] { FBZ_GEFR_WASSER, FBZ_RAUREIF}, new long [] { FBZ_EIS, FBZ_SCHNEE });
			k3 = new EntscheidungsBaumKnoten(methode, k2, eSm, gV, esFb);
					
			methode = new EntscheidungsBaumKnoten.FbofTemperatur(2, new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, k1, k3);
			
			methode = new EntscheidungsBaumKnoten.LuftTemperatur(2, new OperatorKleinerGleich());
			k1 = new EntscheidungsBaumKnoten(methode, sGbNm, kG);
			
			methode = new EntscheidungsBaumKnoten.FbofTemperatur(5, new OperatorGroesser());
			wurzel = new EntscheidungsBaumKnoten(methode, k1, k2);
			
		} catch (DUAInitialisierungsException e) {
			LOGGER.error("Fehler bei der Initialisierung des EntscheidunsBaumes: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	/**
	 * Berechnet die Glaetteprognose
	 * 
	 * @param fbzAktuell Fahrbahnzustand aktuell
	 * @param fbtAktuell FahrbahnTemperatur aktuell
	 * @param tptAktuell Taupunkttemperatur aktuell
	 * @param lftAktuell Lufttemperatur aktuell 
	 * @param fbtExtrapoliert Fahrbahntemperatur extrapoliert im Prognosehorizont
	 * @param tptExtrapoliert Taupunkttemperatur extrapoliert im Prognosehorizont
	 * @return die Glaetteprognose
	 */
	static public int getPrognose(long fbzAktuell, double fbtAktuell, double tptAktuell, double lftAktuell, double fbtExtrapoliert, double tptExtrapoliert) {
		//long fbzAkt = fbzAktuell.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").longValue();
		if(wurzel == null) {
			erzeugeEntscheidungsBaum();
		}
		return wurzel.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
	}
}
