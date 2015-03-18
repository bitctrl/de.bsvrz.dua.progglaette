/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.14 Glättewarnung und -prognose
 * 
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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
 * Testet den Entscheidungsbaum.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 * @version $Id$
 */
public class EntscheidungsBaumTest extends EntscheidungsBaum {

	/**
	 * Zustanaende.
	 */
	public static final long T = FBZ_TROCKEN;

	/**
	 * Zustanaende.
	 */
	public static final long F = FBZ_FEUCHT;

	/**
	 * Zustanaende.
	 */
	public static final long N = FBZ_NASS;

	/**
	 * Zustanaende.
	 */
	public static final long W = FBZ_GEFR_WASSER;

	/**
	 * Zustanaende.
	 */
	public static final long S = FBZ_SCHNEE;

	/**
	 * Zustanaende.
	 */
	public static final long E = FBZ_EIS;

	/**
	 * Zustanaende.
	 */
	public static final long R = FBZ_RAUREIF;

	/**
	 * Zustanaende.
	 */
	public static final long U = FBZ_UNDEFINIERT;

	/**
	 * MESSWERT_UNDEFIENIERT.
	 */
	public static final double NV = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;

	/**
	 * dieser Wert sagt, dass es eine Zufaellige Nummer sein kann.
	 */
	public static final long Z = 999999;

	/**
	 * Per-Hand berechenete Eingabe und Prognosewerte.
	 */
	private static final double[][] TT = new double[][] {
			// FBZ           -1 nicht ermittelbar
			// Temperatur -1001 nicht ermittelbar
			// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert, tptExtrapoliert, prognose
			{ Z, -1001, Z, Z, Z, Z, EB_NICHT_ERMITTELBAR },
			{ Z, 5.1, -1001, Z, Z, Z, EB_NICHT_ERMITTELBAR },
			{ Z, 6.2, 1.1, Z, Z, Z, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ Z, 5.2, 3.1, Z, Z, Z, EB_KEINE_GLAETTEGEHFAHR },
			{ Z, 5.3, 2, Z, Z, Z, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ S, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ E, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ W, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ R, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ N, 2, Z, Z, Z, Z, EB_EISGLAETTE_MOEGLICH_SOFORT },
			{ F, 2, Z, Z, Z, Z, EB_EISGLAETTE_MOEGLICH_SOFORT },
			{ -1, 2, Z, Z, Z, Z, EB_NICHT_ERMITTELBAR },
			{ T, 2, Z, 2, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, 1, Z, 3, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, 1, Z, 1, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, -1, Z, -3, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
			{ T, -1, Z, -3.1, 1, 1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
			{ T, -1, Z, -3.1, 0.2, 0.1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT },

			// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert, tptExtrapoliert, prognose
			{ T, -1, Z, -3.1, Z, -1001, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ T, -1, Z, -3.1, -1001, Z, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ T, -1, Z, -1001, Z, Z, EB_NICHT_ERMITTELBAR },
			{ F, 2.2, Z, Z, 2, Z, EB_EISGLAETTE_MOEGLICH },
			{ N, 2.2, Z, Z, 2, Z, EB_EISGLAETTE_MOEGLICH },
			{ -1, 2.2, Z, Z, 1, Z, EB_NICHT_ERMITTELBAR },
			{ Z, 2.2, Z, Z, -1001, Z, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, -1, -1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
			{ T, 2.2, Z, Z, -1, 0.1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
			{ T, 2.2, Z, Z, 1, 0.1, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ T, 2.2, Z, Z, -1001, Z, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, 2, -1001, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ -1, 2.2, Z, Z, 2, Z, EB_NICHT_ERMITTELBAR },
			{ T, 2.2, Z, Z, 0, -1001, EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
			{ -1, 2.2, Z, Z, 0, Z, EB_NICHT_ERMITTELBAR },
			{ T, 2.2, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ T, 3, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ F, 2.2, Z, Z, 2.1, Z, EB_EISGLAETTE_MOEGLICH },
			{ N, 3, Z, Z, 2.1, Z, EB_EISGLAETTE_MOEGLICH },
			{ -1, 3, Z, Z, 2.1, Z, EB_NICHT_ERMITTELBAR },
			{ -1, 3.1, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ -1, 4, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },

			{ Z, NV, Z, Z, Z, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ Z, 5.1, NV, Z, Z, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ Z, 6.2, 1.1, Z, Z, Z, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ Z, 5.2, 3.1, Z, Z, Z, EB_KEINE_GLAETTEGEHFAHR },
			{ Z, 5.3, 2, Z, Z, Z, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ S, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ E, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ W, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ R, 2, Z, Z, Z, Z, EB_GLAETTE_VORHANDEN },
			{ N, 2, Z, Z, Z, Z, EB_EISGLAETTE_MOEGLICH_SOFORT },
			{ F, 2, Z, Z, Z, Z, EB_EISGLAETTE_MOEGLICH_SOFORT },
			{ U, 2, Z, Z, Z, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, 2, Z, 2, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, 1, Z, 3, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, 1, Z, 1, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{ T, -1, Z, -3, Z, Z,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
			{ T, -1, Z, -3.1, 1, 1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
			{ T, -1, Z, -3.1, 0.2, 0.1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT },

			// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert, tptExtrapoliert, prognose
			{ T, -1, Z, -3.1, Z, NV,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, -1, Z, -3.1, NV, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, -1, Z, NV, Z, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ F, 2.2, Z, Z, 2, Z, EB_EISGLAETTE_MOEGLICH },
			{ N, 2.2, Z, Z, 2, Z, EB_EISGLAETTE_MOEGLICH },
			{ U, 2.2, Z, Z, 1, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ Z, 2.2, Z, Z, NV, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, -1, -1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
			{ T, 2.2, Z, Z, -1, 0.1,
					EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
			{ T, 2.2, Z, Z, 1, 0.1, EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
			{ T, 2.2, Z, Z, NV, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, 2, NV,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ U, 2.2, Z, Z, 2, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, 0, NV,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ U, 2.2, Z, Z, 0, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ T, 2.2, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ T, 3, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ F, 2.2, Z, Z, 2.1, Z, EB_EISGLAETTE_MOEGLICH },
			{ N, 3, Z, Z, 2.1, Z, EB_EISGLAETTE_MOEGLICH },
			{ U, 3, Z, Z, 2.1, Z,
					EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
			{ -1, 3.1, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
			{ -1, 4, Z, Z, 2.1, Z, EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG }, };

	/**
	 * Die modifizierte tabelle.
	 */
	private static double[][] tabelle = new double[TT.length][TT[0].length];

	/**
	 * Kopiert Arrays.
	 * 
	 * @param src Quelle Array
	 * @param dst Ziel Array
	 */
	public void copy(final double[][] src, double[][] dst) {
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src[i].length; j++) {
				dst[i][j] = src[i][j];
			}
		}
	}

	/**
	 * Die Werte die, zufaellig sein koennen ( mit Z gekennzeichnet ), werden randomisiert.
	 */
	public void randomisiere() {
		boolean rand = false;
		for (int i = 0; i < tabelle.length; i++) {
			for (int j = 0; j < tabelle[i].length; j++) {
				if (tabelle[i][j] == Z) {
					tabelle[i][j] = (-1010 + Math.random() * 2020);
					rand = true;
				}
			}
		}
		System.out.println("Tabelle "
				+ ((rand) ? "randomisiert" : "NICHT randomisiert"));
	}

	/**
	 * Die Test-methode.
	 */
	@Test
	public void baumTest() {

		int prognose, antwort;
		long fbzAktuell;
		double fbtAktuell, lftAktuell, tptAktuell;
		double fbtExtrapoliert, tptExtrapoliert;

		for (int j = 0; j < 10; j++) {
			copy(TT, tabelle);
			if (j != 0) {
				randomisiere();
			}
			for (int i = 0; i < tabelle.length; i++) {

				fbzAktuell = (long) (tabelle[i][0]);
				fbtAktuell = tabelle[i][1];
				lftAktuell = tabelle[i][2];
				tptAktuell = tabelle[i][3];

				fbtExtrapoliert = tabelle[i][4];
				tptExtrapoliert = tabelle[i][5];

				antwort = (int) (tabelle[i][6]);

				prognose = EntscheidungsBaum.getPrognose(fbzAktuell,
						fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert,
						tptExtrapoliert);
				Assert.assertEquals(antwort, prognose);
				
				System.out.println(String.format(
						"[ %4d ] Prognose: %2d == %2d ", i, prognose, antwort));
			}
		}
	}
}
