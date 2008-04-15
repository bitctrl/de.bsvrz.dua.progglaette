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

/**
 * Berechnet eine Prognose von Messwerten mit der Methode der Linearen
 * Trendextrapolation ( Least Square Method ) Die Prognose ist nach AFo im
 * Horizont 5, 15, 30, 60, 90 Minuten.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
 */
public class PrognoseZustand {

	/**
	 * Minute in Millisekunden.
	 */
	public static final long MIN_IN_MS = 60 * 1000L;

	/**
	 * Standardkonstruktor.
	 */
	protected PrognoseZustand() {
		
	}
	
	/**
	 * Berechnet die Prognose.
	 * 
	 * @param werteArray
	 *            Messwerte
	 * @param zeitArray
	 *            Zeitpunkte der Messwerten
	 * @param indexAktuell
	 *            Index des letzten Messwertes ( aktuellen )
	 * @return Prognose fuer den Messwert in 5, 15, 30, 60 und 90 Minuten
	 */
	static double[] berechnePrognose(double[] werteArray, long[] zeitArray,
			int indexAktuell) {

		double a, b;
		double[] y = new double[5];

		double sumT = 0.0f;
		double sumT2 = 0.0f;
		double sumW = 0.0f;
		double sumTW = 0.0f;

		double tMitte, yMitte;
		int n = werteArray.length;
		int m = n;
		long t0 = zeitArray[indexAktuell];

		for (int i = 0; i < n; i++) {
			// Zeitstemepel ist 0, wenn ein Datum faehlt
			if (zeitArray[i] != 0) {
				sumT += zeitArray[i];
				sumW += werteArray[i];
				sumT2 += zeitArray[i] * zeitArray[i];
				sumTW += zeitArray[i] * werteArray[i];
			} else {
				m--;
			}
		}

		tMitte = sumT / m;
		yMitte = sumW / m;

		a = (sumTW - m * tMitte * yMitte) / (sumT2 - m * tMitte * tMitte);
		b = yMitte - a * tMitte;

		y[0] = a * (t0 + 5 * MIN_IN_MS) + b;
		y[1] = a * (t0 + 15 * MIN_IN_MS) + b;
		y[2] = a * (t0 + 30 * MIN_IN_MS) + b;
		y[3] = a * (t0 + 60 * MIN_IN_MS) + b;
		y[4] = a * (t0 + 90 * MIN_IN_MS) + b;

		return y;
	}

}
