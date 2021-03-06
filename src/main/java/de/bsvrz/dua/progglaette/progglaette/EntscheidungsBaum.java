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

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Der Entscheidungsbaum macht eine Glaetteprognose aus den aktuellen
 * Eigenschaften der Fahrbahn und Luft und seinen Prognosewerten.
 *
 * @author BitCtrl Systems GmbH, Bachraty..
 */
public class EntscheidungsBaum {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Standardkonstruktor.
	 */
	protected EntscheidungsBaum() {
		//
	}

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH = Integer.MIN_VALUE;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_NICHT_ERMITTELBAR = -1;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_KEINE_GLAETTEGEHFAHR = 1;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG = 2;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_EISGLAETTE_MOEGLICH = 3;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_TENDENZBERECHNUNG_NICHT_MOEGLICH = 4;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG = 5;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE = 6;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT = 7;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT = 8;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_EISGLAETTE_MOEGLICH_SOFORT = 9;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_GLAETTE_VORHANDEN = 10;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_EIS_SCHNEE_AUF_DER_FAHRBAHN = 11;

	/**
	 * Konstanden nach dem DatK.
	 *
	 * kb.tmUmfeldDatenGlobal.xml
	 *
	 */
	public static final int EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE = 12;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_UNDEFINIERT = Long.MIN_VALUE;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_TROCKEN = 0;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_FEUCHT = 1;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_NASS = 32;
	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_GEFR_WASSER = 64;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_SCHNEE = 65;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_EIS = 66;

	/**
	 * Fahrbahnoberflaechenzustaende.
	 */
	public static final long FBZ_RAUREIF = 67;

	/**
	 * Zustand unbekanner Messwert der Temperatur.
	 */
	public static final double MESSWERT_UNDEFIENIERT = Double.MAX_VALUE;

	/**
	 * Der Wurzel des Entscheidungbaumes.
	 */
	private static EntscheidungsBaumKnoten wurzel = null;

	/**
	 * Logische Operatoren die waehrend der Entscheidung benutzt werden.
	 */
	public interface Operator {
		/**
		 * Anwendung des Operators.
		 *
		 * @param x
		 *            Operand x
		 * @param y
		 *            Operand y
		 * @return Ergebnisswert des Operators
		 */
		boolean anwende(double x, double y);
	}

/**
	 * Operator "&lt;".
	 */
	public static class OperatorKleiner implements Operator {
		@Override
		public boolean anwende(final double x, final double y) {
			return (x < y);
		}
	}

	/**
	 * Operator "&lt;=".
	 */
	public static class OperatorKleinerGleich implements Operator {
		@Override
		public boolean anwende(final double x, final double y) {
			return (x <= y);
		}
	}

	/**
	 * Operator "&gt;".
	 */
	public static class OperatorGroesser implements Operator {
		@Override
		public boolean anwende(final double x, final double y) {
			return (x > y);
		}
	}

	/**
	 * Operator "&gt;=".
	 */
	public static class OperatorGroesserGleich implements Operator {
		@Override
		public boolean anwende(final double x, final double y) {
			return (x >= y);
		}
	}

	/**
	 * Erzeugt dem EntscheidungsBaum nach der Abbildung in der AFo.
	 */
	private static void erzeugeEntscheidungsBaum() {
		EntscheidungsBaumKnoten k1, k2, k3;
		EntscheidungsBaumKnoten sGbNm, sGbNsRm, eM, eSm, gbWm, sGbNSm, sGbNsRSm, kG, gV, sGbNSsRm;
		EntscheidungsBaumKnoten.EntscheidungsMethode methode;

		try {
			sGbNm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG);
			sGbNsRm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE);
			eM = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH);
			eSm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH_SOFORT);
			gbWm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG);
			sGbNSm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT);
			sGbNsRSm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT);
			kG = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_KEINE_GLAETTEGEHFAHR);
			gV = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN);
			sGbNSsRm = new EntscheidungsBaumKnoten(
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE);

			methode = new EntscheidungsBaumKnoten.DifferenzPrognoseFbofTaupunktTemperatur(
					0, new OperatorGroesser());
			k1 = new EntscheidungsBaumKnoten(methode, sGbNm, sGbNsRm);

			methode = new EntscheidungsBaumKnoten.FahbrBahnZustand(
					new long[] { EntscheidungsBaum.FBZ_TROCKEN }, new long[] {
							EntscheidungsBaum.FBZ_FEUCHT,
							EntscheidungsBaum.FBZ_NASS });
			k2 = new EntscheidungsBaumKnoten(methode, k1, eM);

			methode = new EntscheidungsBaumKnoten.FahbrBahnZustand(new long[] {
					EntscheidungsBaum.FBZ_FEUCHT, EntscheidungsBaum.FBZ_NASS },
					new long[] { EntscheidungsBaum.FBZ_TROCKEN });
			k1 = new EntscheidungsBaumKnoten(methode, eM, gbWm);

			methode = new EntscheidungsBaumKnoten.FbofTemperatur(3,
					new OperatorKleinerGleich());
			k3 = new EntscheidungsBaumKnoten(methode, k1, gbWm);

			methode = new EntscheidungsBaumKnoten.FbofPrognoseTemperatur(2,
					new OperatorKleinerGleich());
			k1 = new EntscheidungsBaumKnoten(methode, k2, k3);

			methode = new EntscheidungsBaumKnoten.DifferenzPrognoseFbofTaupunktTemperatur(
					0, new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, sGbNSm, sGbNSsRm);

			methode = new EntscheidungsBaumKnoten.DifferenzFbofTaupunktTemperatur(
					2, new OperatorGroesser());
			k3 = new EntscheidungsBaumKnoten(methode, k2, sGbNSsRm);

			methode = new EntscheidungsBaumKnoten.DifferenzFbofTaupunktTemperatur(
					0, new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, k3, sGbNsRSm);

			methode = new EntscheidungsBaumKnoten.FahbrBahnZustandVollDefiniert(
					new long[] { EntscheidungsBaum.FBZ_TROCKEN }, new long[] {
							EntscheidungsBaum.FBZ_FEUCHT,
							EntscheidungsBaum.FBZ_NASS }, new long[] {
							EntscheidungsBaum.FBZ_GEFR_WASSER,
							EntscheidungsBaum.FBZ_RAUREIF,
							EntscheidungsBaum.FBZ_EIS,
							EntscheidungsBaum.FBZ_SCHNEE });
			k3 = new EntscheidungsBaumKnoten(methode, k2, eSm, gV);

			methode = new EntscheidungsBaumKnoten.FbofTemperatur(2,
					new OperatorGroesser());
			k2 = new EntscheidungsBaumKnoten(methode, k1, k3);

			methode = new EntscheidungsBaumKnoten.LuftTemperatur(2,
					new OperatorKleinerGleich());
			k1 = new EntscheidungsBaumKnoten(methode, sGbNm, kG);

			methode = new EntscheidungsBaumKnoten.FbofTemperatur(5,
					new OperatorGroesser());
			EntscheidungsBaum.wurzel = new EntscheidungsBaumKnoten(methode, k1,
					k2);

		} catch (final DUAInitialisierungsException e) {
			LOGGER.error(
					"Fehler bei der Initialisierung des EntscheidungsBaumes: "
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Berechnet die Glaetteprognose.
	 *
	 * @param fbzAktuell
	 *            Fahrbahnzustand aktuell
	 * @param fbtAktuell
	 *            FahrbahnTemperatur aktuell
	 * @param tptAktuell
	 *            Taupunkttemperatur aktuell
	 * @param lftAktuell
	 *            Lufttemperatur aktuell
	 * @param fbtExtrapoliert
	 *            Fahrbahntemperatur extrapoliert im Prognosehorizont
	 * @param tptExtrapoliert
	 *            Taupunkttemperatur extrapoliert im Prognosehorizont
	 * @return die Glaetteprognose
	 */
	public static int getPrognose(final long fbzAktuell,
			final double fbtAktuell, final double tptAktuell,
			final double lftAktuell, final double fbtExtrapoliert,
			final double tptExtrapoliert) {
		if (EntscheidungsBaum.wurzel == null) {
			EntscheidungsBaum.erzeugeEntscheidungsBaum();
		}
		return EntscheidungsBaum.wurzel.getPrognose(fbzAktuell, fbtAktuell,
				tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
	}
}
