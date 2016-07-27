/*
 *
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.progglaette.
 * 
 * de.bsvrz.dua.progglaette is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.progglaette is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.progglaette.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.progglaette.progglaette;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Berechnet eine Prognose von Messwerten mit der Methode der Linearen
 * Trendextrapolation ( Least Square Method ) Die Prognose ist nach AFo im
 * Horizont 5, 15, 30, 60, 90 Minuten.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 * @version $Id: PrognoseZustand.java 53837 2015-03-18 11:45:45Z peuker $
 */
public class PrognoseZustand {

	/**
	 * Minute in Millisekunden.
	 */
	public static final long MIN_IN_MS = 60L * 1000L;

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
	 * @param zeitArrayOriginal
	 *            Zeitpunkte der Messwerten
	 * @param indexAktuell
	 *            Index des letzten Messwertes ( aktuellen )
	 * @return Prognose fuer den Messwert in 5, 15, 30, 60 und 90 Minuten
	 */
	static double[] berechnePrognose(final double[] werteArray,
			final long[] zeitArrayOriginal, final int indexAktuell) {

		/**
		 * kleinsten Zeitstempel heraussuchen
		 */
		final SortedSet<Long> ordnung = new TreeSet<Long>();
		for (final Long zeitStempel : zeitArrayOriginal) {
			if (zeitStempel > 0) {
				ordnung.add(zeitStempel);
			}
		}
		final long kleinsterZeitStempel = ordnung.first();
		long t0 = 0;
		final long[] zeitArray = new long[zeitArrayOriginal.length];
		for (int i = 0; i < zeitArrayOriginal.length; i++) {
			if (zeitArrayOriginal[i] > 0) {
				zeitArray[i] = ((zeitArrayOriginal[i] - kleinsterZeitStempel) / ((long) (60 * 1000))) + 1;
				if (zeitArray[i] > t0) {
					t0 = zeitArray[i];
				}
			} else {
				zeitArray[i] = 0;
			}
		}

		double a, b;
		final double[] y = new double[5];

		double sumT = 0.0f;
		double sumT2 = 0.0f;
		double sumW = 0.0f;
		double sumTW = 0.0f;

		double tMitte, yMitte;
		final int n = werteArray.length;
		int m = n;

		for (int i = 0; i < n; i++) {
			// Zeitstempel ist 0, wenn ein Datum fehlt
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

		a = (sumTW - (m * tMitte * yMitte)) / (sumT2 - (m * tMitte * tMitte));
		b = yMitte - (a * tMitte);

		y[0] = (a * (t0 + 5L)) + b;
		y[1] = (a * (t0 + 15L)) + b;
		y[2] = (a * (t0 + 30L)) + b;
		y[3] = (a * (t0 + 60L)) + b;
		y[4] = (a * (t0 + 90L)) + b;

		return y;
	}

}
