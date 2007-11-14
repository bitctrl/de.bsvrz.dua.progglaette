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
import de.bsvrz.dua.progglaette.progglaette.EntscheidungsBaumKnoten.Horizont;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.debug.Debug;

public class EntscheidungsBaum {
	/**
	 * Konstanden nach dem DatK
	 * 
	 * kb.tmUmfeldDatenGlobal.xml
	 * 
	 */
	public static final int EB_KEINE_GLAETTEGEHFAHR              									= 1;
	public static final int EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG      								= 2;
	public static final int EB_EISGLAETTE_MOEGLICH				 						   			= 3;
	public static final int EB_TENDENZBERECHNUNG_NICHT_MOEGLICH 									= 4;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHKAG 								= 5;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE 			= 6;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT			 			= 7;
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT 	= 8;
	public static final int EB_EISGLAETTE_MOEGLICH_SOFORT										 	= 9;
	public static final int EB_GLAETTE_VORHANDEN												 	= 10;
	public static final int EB_EIS_SCHNEE_AUF_DER_FAHRBAHN										 	= 11;
	
	public static final long FBZ_TROCKEN 		= 0;
	public static final long FBZ_FEUCHT 		= 1;
	public static final long FBZ_NASS 			= 32;
	public static final long FBZ_GEFR_WASSER	= 64;
	public static final long FBZ_SCHNEE			= 65;
	public static final long FBZ_EIS			= 66;
	public static final long FBZ_RAUREIF		= 67;

	private static EntscheidungsBaumKnoten wurzel = null;
	
	private static Debug LOGGER = Debug.getLogger();
	

	/**
	 * Operatoren die waehredn der Entscheidung benutzt werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public interface Operator {
		boolean anwende(long x, long y);
	}
	
	public static class OperatorKleiner implements Operator {
		public boolean anwende(long x, long y) {
			return (x < y);
		}
	}
	public static class OperatorKleinerGleich implements Operator {
		public boolean anwende(long x, long y) {
			return (x <= y);
		}
	}
	public static class OperatorGroesser implements Operator {
		public boolean anwende(long x, long y) {
			return (x > y);
		}
	}
	public static class OperatorGroesserGleich implements Operator {
		public boolean anwende(long x, long y) {
			return (x >= y);
		}
	}
	
	
	private static void erzeugeEntschedungsBaum() {
		EntscheidungsBaumKnoten k1, k2, k3;
		EntscheidungsBaumKnoten sGbNm, sGbNsRm, eM, eSm, gbWm, sGbNSm, sGbNSsRm,  kG;
		try {
			sGbNm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHKAG);
			sGbNsRm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE);
			eM = new EntscheidungsBaumKnoten(EB_EISGLAETTE_MOEGLICH);
			eSm = new EntscheidungsBaumKnoten(EB_EISGLAETTE_MOEGLICH_SOFORT);
			gbWm = new EntscheidungsBaumKnoten(EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG);
			sGbNSm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT);
			sGbNSsRm = new EntscheidungsBaumKnoten(EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT);
			kG = new EntscheidungsBaumKnoten(EB_KEINE_GLAETTEGEHFAHR);
			
			k1 = new EntscheidungsBaumKnoten(sGbNm, sGbNsRm);
			k1.initDifferenzFbofTaupunktPrognoseKnoten(new OperatorGroesser(), 0);
			
			k2 = new EntscheidungsBaumKnoten(k1, eM);
			k2.initFahrBahnZustandKnoten(new long [] {FBZ_TROCKEN}, new long [] {FBZ_FEUCHT, FBZ_NASS});
			
			k1 = new EntscheidungsBaumKnoten(eM, gbWm);
			k1.initFahrBahnZustandKnoten(new long [] {FBZ_FEUCHT, FBZ_NASS}, new long [] {FBZ_TROCKEN});
			
			k3 = new EntscheidungsBaumKnoten(k1, gbWm);
			k3.initFbofTemperaturKnoten(new OperatorKleinerGleich(), 3);
			
			k1 = new EntscheidungsBaumKnoten(k2, k3);
			k1.initFbofPrognoseKnoten(new OperatorKleinerGleich(), 2);
			
			k2 = new EntscheidungsBaumKnoten(sGbNSm, sGbNSsRm);
			k2.initDifferenzFbofTaupunktPrognoseKnoten(new OperatorGroesser(), 0);
			
			k3 = new EntscheidungsBaumKnoten(k2, sGbNSsRm);
			k3.initDifferenzFbofTaupunktKnoten(new OperatorGroesser(), 2);
			
			k2 = new EntscheidungsBaumKnoten(k3, sGbNSsRm);
			k2.initDifferenzFbofTaupunktKnoten(new OperatorGroesser(), 0);
			
			k3 = new EntscheidungsBaumKnoten(k2, eSm);
			k3.initFahrBahnZustandGlaetteKnoten(new long [] {FBZ_TROCKEN}, new long [] {FBZ_FEUCHT, FBZ_NASS }, 
					new long [] {FBZ_EIS, FBZ_GEFR_WASSER, FBZ_RAUREIF, FBZ_SCHNEE});
			
			k2 = new EntscheidungsBaumKnoten(k1, k3);
			k2.initFbofTemperaturKnoten(new OperatorGroesser(), 2);
			
			k1 = new EntscheidungsBaumKnoten(sGbNm, kG);
			k1.initLuftTemperaturKnoten(new OperatorKleinerGleich(), 2);
			
			wurzel = new EntscheidungsBaumKnoten(k1, k2);
			wurzel.initFbofTemperaturKnoten(new OperatorGroesser(), 5);
			
			
		} catch (DUAInitialisierungsException e) {
			LOGGER.error("Fehler bei der Initialisierung des EntscheidunsBaumes: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	static public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont) {
		if(wurzel == null) {
			erzeugeEntschedungsBaum();
		}
		return wurzel.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
	}
}
