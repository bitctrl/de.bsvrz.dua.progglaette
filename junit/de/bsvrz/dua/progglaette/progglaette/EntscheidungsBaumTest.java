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

import junit.framework.Assert;
import org.junit.Test;

/**
 * Testet den Entscheidungsbaum
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class EntscheidungsBaumTest extends EntscheidungsBaum {
	
	public static final long T = FBZ_TROCKEN;
	public static final long F = FBZ_FEUCHT;
	public static final long N = FBZ_NASS;
	public static final long W = FBZ_GEFR_WASSER;
	public static final long S = FBZ_SCHNEE;
	public static final long E = FBZ_EIS;
	public static final long R = FBZ_RAUREIF;
	
	/**
	 * Per-Hand berechenete Eingabe und Prognosewerte
	 */
	private static final double [][] tabelle = new double [] [] {
		// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert, tptExtrapoliert, prognose
		{  0,  -1001,      0,    0,    0,    0,   EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
		{  0,    5.1,  -1001,    0,    0,    0,   EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
		{  0,    6.2,    1.1,    0,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHKAG},
		{  0,    5.2,    3.1,    0,    0,    0,   EB_KEINE_GLAETTEGEHFAHR},
		{  0,    5.3,      2,    0,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHKAG},
		{  S,      2,      0,    0,    0,    0,   EB_GLAETTE_VORHANDEN},
		{  E,      2,      0,    0,    0,    0,   EB_GLAETTE_VORHANDEN},
		{  W,      2,      0,    0,    0,    0,   EB_GLAETTE_VORHANDEN},
		{  R,      2,      0,    0,    0,    0,   EB_GLAETTE_VORHANDEN},
		{  N,      2,      0,    0,    0,    0,   EB_EISGLAETTE_MOEGLICH_SOFORT},
		{  F,      2,      0,    0,    0,    0,   EB_EISGLAETTE_MOEGLICH_SOFORT},
		{ -1,      2,      0,    0,    0,    0,   EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
		{  T,      2,      0,    2,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
		{  T,      1,      0,    3,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
		{  T,      1,      0,    1,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
		{  T,     -1,      0,   -3,    0,    0,   EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
	};
	
	/**
	 * Die Test-methode
	 */
	@Test
	public void baumTest() {
		
		int prognose, antwort;
		long fbzAktuell;
		double fbtAktuell, lftAktuell, tptAktuell;
		double fbtExtrapoliert, tptExtrapoliert;
		
		for(int i=0; i<tabelle.length; i++) {
			
			fbzAktuell = (long)(tabelle[i][0]);
			fbtAktuell = tabelle[i][1];
			lftAktuell = tabelle[i][2];
			tptAktuell = tabelle[i][3];
			
			fbtExtrapoliert = tabelle[i][4];
			tptExtrapoliert = tabelle[i][5];
			
			antwort = (int)(tabelle[i][6]);
			
			prognose = EntscheidungsBaum.getPrognose(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
			Assert.assertEquals(antwort, prognose);
			System.out.println(String.format("[ %4d ] Prognose: %2d == %2d ", i, prognose, antwort));
		}
	}
}
