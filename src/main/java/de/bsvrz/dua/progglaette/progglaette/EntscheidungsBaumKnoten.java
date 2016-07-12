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

/**
 * Ein Knoten des Eintscheidungsbaumes Enthaelt Referenzen fuer seine Nachfolger
 * und eine Entscheidungsmethode, die die Richtung bestimmt Fals es ein
 * EndKnoten ist, dann enthaelt den Ergebnisswert.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 * @version $Id: EntscheidungsBaumKnoten.java 53825 2015-03-18 09:36:42Z peuker
 *          $
 */
public class EntscheidungsBaumKnoten {

	/**
	 * Die Konstante fuer nicht ermittelbare Werte.
	 */
	public static final double EBK_TEMPERATUR_NICHT_ERMITTELBAR = -1001.0;

	/**
	 * Nachfolgende Knoten Links.
	 */
	protected EntscheidungsBaumKnoten nachfolgerLinks = null;

	/**
	 * Nachfolgende Knoten Rechts.
	 */
	protected EntscheidungsBaumKnoten nachfolgerRechts = null;

	/**
	 * Nachfolgende Knoten in der Mitte.
	 */
	protected EntscheidungsBaumKnoten nachfolgerMitte = null;

	/**
	 * Die EntscheidungsMethode des Knotens.
	 */
	private EntscheidungsMethode methode = null;

	/**
	 * Ergebniswert, dem ein Endknoten leifert.
	 */
	private int ergebnisWert = -1;

	/**
	 * Die Richtungen in dennen man im Baum weitergehen kann.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private enum Richtung {
		/**
		 * Richtung nach Links.
		 */
		R_LINKS,
		/**
		 * Richtung in der Mitte.
		 */
		R_MITTE,
		/**
		 * Richtung nach Rechts.
		 */
		R_RECHTS,
		/**
		 * Richtung keine ( Wert nicht ermittelbar ).
		 */
		R_NICHT_ERMITTELBAR,
		/**
		 * Richtung keine ( Tendenzwert nicht verfuegbar ).
		 */
		R_TENDENZBERECHNUNG_NICHT_MOEGLICH,

		/**
		 * Richtung keine ( Wert nicht verfuegbar ).
		 */
		R_ENTSCHEIDUNG_NICHT_MOEGLICH
	};

	/**
	 * Die EntscheidungsMethode des Knotens, entscheidet in welcher Richtung man
	 * weitergeht.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public interface EntscheidungsMethode {
		/**
		 * Entscheidet in welcher Richtung man weitergehen soll.
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
		 * @return Richtung
		 */
		Richtung getRichtung(long fbzAktuell, double fbtAktuell,
				double tptAktuell, double lftAktuell, double fbtExtrapoliert,
				double tptExtrapoliert);
	}

	/**
	 * Abstrakte Klasse fuer die Entscheidung nach Temperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public abstract static class EntscheidungTemperatur implements
	EntscheidungsMethode {
		/**
		 * Grenzwert fuer die Entscheidung.
		 */
		private double grenzWert = 0;

		/**
		 * Operator, dem man anwenden soll, um den Grenzwert mit aktuellen Wert
		 * zu vergleichen.
		 */
		private EntscheidungsBaum.Operator operator = null;

		/**
		 * Der aktuelle Wert.
		 */
		protected double wert = 0;

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public EntscheidungTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			this.grenzWert = grenzWert;
			this.operator = operator;
		}

		/**
		 * Mit Anwendung des Operators entscheidet, in welcher Richtung man
		 * weitergehen soll.
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
		 * @return Richtung in welcher man weiter
		 */
		protected final Richtung auswerte(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if (wert == EntscheidungsBaum.MESSWERT_UNDEFIENIERT) {
				return Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH;
			} else if (operator.anwende(wert, grenzWert)) {
				return Richtung.R_LINKS;
			} else {
				return Richtung.R_RECHTS;
			}
		}
	}

	/**
	 * Trifft Entscheidung nach der Fahrbahnoberflaechentemperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FbofTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public FbofTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if (fbtAktuell == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR) {
				return Richtung.R_NICHT_ERMITTELBAR;
			}
			wert = fbtAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell,
					fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach der Lufttemperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class LuftTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public LuftTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if (lftAktuell == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR) {
				return Richtung.R_NICHT_ERMITTELBAR;
			}
			wert = lftAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell,
					fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach der Differenz der Fahrbahnoberflaechen- und
	 * Taupunkttemperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class DifferenzFbofTaupunktTemperatur extends
	EntscheidungTemperatur {

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public DifferenzFbofTaupunktTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if ((fbtAktuell == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR)
					|| (tptAktuell == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR)) {
				return Richtung.R_NICHT_ERMITTELBAR;
			} else if ((fbtAktuell == EntscheidungsBaum.MESSWERT_UNDEFIENIERT)
					|| (tptAktuell == EntscheidungsBaum.MESSWERT_UNDEFIENIERT)) {
				return Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH;
			}
			wert = fbtAktuell - tptAktuell;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell,
					fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach der Differenz der Prognose der
	 * Fahrbahnoberflaechen- und Taupunkttemperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class DifferenzPrognoseFbofTaupunktTemperatur extends
	EntscheidungTemperatur {

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public DifferenzPrognoseFbofTaupunktTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if ((fbtExtrapoliert == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR)
					|| (tptExtrapoliert == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR)) {
				return Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			} else if ((fbtExtrapoliert == EntscheidungsBaum.MESSWERT_UNDEFIENIERT)
					|| (tptExtrapoliert == EntscheidungsBaum.MESSWERT_UNDEFIENIERT)) {
				return Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH;
			}
			wert = fbtExtrapoliert - tptExtrapoliert;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell,
					fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach der Prognose der Fahrbahnoberflaechentemperatur.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FbofPrognoseTemperatur extends EntscheidungTemperatur {

		/**
		 * Standardkonstruktor.
		 *
		 * @param grenzWert
		 *            Grenzwert
		 * @param operator
		 *            Operator - wenn als <code>true</code> ausgewertet, geht
		 *            nach links, sonst rechts
		 */
		public FbofPrognoseTemperatur(final double grenzWert,
				final EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if (fbtExtrapoliert == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR) {
				return Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			}
			wert = fbtExtrapoliert;
			return auswerte(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell,
					fbtExtrapoliert, tptExtrapoliert);
		}
	}

	/**
	 * Trifft Entscheidung nach dem Fahrbahnoberflaechenzustand.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FahbrBahnZustand implements EntscheidungsMethode {

		/**
		 * Wertenmenge, bei denen man nach links geht.
		 */
		protected long[] werteLinks = null;
		/**
		 * Wertenmenge, bei denen man nach rechts geht.
		 */
		protected long[] werteRechts = null;

		/**
		 * Standardkonstruktor.
		 *
		 * @param werteLinks
		 *            Wertenmenge, bei denen man nach links geht
		 * @param werteRechts
		 *            Wertenmenge, bei denen man nach rechts geht
		 */
		public FahbrBahnZustand(final long[] werteLinks,
				final long[] werteRechts) {
			this.werteLinks = werteLinks;
			this.werteRechts = werteRechts;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {
			if (fbzAktuell == EntscheidungsBaum.FBZ_UNDEFINIERT) {
				return Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH;
			}

			for (final long werteLink : werteLinks) {
				if (werteLink == fbzAktuell) {
					return Richtung.R_LINKS;
				}
			}

			for (final long werteRecht : werteRechts) {
				if (werteRecht == fbzAktuell) {
					return Richtung.R_RECHTS;
				}
			}

			return Richtung.R_NICHT_ERMITTELBAR;
		}
	}

	/**
	 * Trifft Entscheidung nach dem Fahrbahnoberflaechenzustand, erweitert es
	 * mit dem Zustand "Glaette vorhanden".
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public static class FahbrBahnZustandVollDefiniert extends FahbrBahnZustand {

		/**
		 * Wertenmenge, bei denen man in die Mitte weitergeht.
		 */
		protected long[] werteMitte;

		/**
		 * Standardkonstruktor.
		 *
		 * @param werteLinks
		 *            Wertenmenge, bei denen man nach links geht
		 * @param werteMitte
		 *            Wertenmenge, bei denen man in die Mitte geht
		 * @param werteRechts
		 *            Wertenmenge, bei denen man nach rechts geht
		 */
		public FahbrBahnZustandVollDefiniert(final long[] werteLinks,
				final long[] werteMitte, final long[] werteRechts) {
			super(werteLinks, werteRechts);
			this.werteMitte = werteMitte;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Richtung getRichtung(final long fbzAktuell,
				final double fbtAktuell, final double tptAktuell,
				final double lftAktuell, final double fbtExtrapoliert,
				final double tptExtrapoliert) {

			if (fbzAktuell == EntscheidungsBaum.FBZ_UNDEFINIERT) {
				return Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH;
			}

			for (final long werteLink : werteLinks) {
				if (werteLink == fbzAktuell) {
					return Richtung.R_LINKS;

				}
			}

			for (final long element : werteMitte) {
				if (element == fbzAktuell) {
					return Richtung.R_MITTE;
				}
			}

			for (final long werteRecht : werteRechts) {
				if (werteRecht == fbzAktuell) {
					return Richtung.R_RECHTS;
				}
			}

			return Richtung.R_NICHT_ERMITTELBAR;
		}
	}

	/**
	 * Auswertet den Ergebniss der Glaetteprognose, mit Hilfe der
	 * Entscheidungsmethode recursiv geht weiter in den Nachfolgerknoten bis der
	 * Prognosewert nicht gefunden wird.
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
	public int getPrognose(final long fbzAktuell, final double fbtAktuell,
			final double tptAktuell, final double lftAktuell,
			final double fbtExtrapoliert, final double tptExtrapoliert) {
		// Endknoten
		if (methode == null) {
			return ergebnisWert;
		}

		final Richtung r = methode.getRichtung(fbzAktuell, fbtAktuell,
				tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);

		if (r == Richtung.R_LINKS) {
			return nachfolgerLinks.getPrognose(fbzAktuell, fbtAktuell,
					tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		} else if (r == Richtung.R_RECHTS) {
			return nachfolgerRechts.getPrognose(fbzAktuell, fbtAktuell,
					tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		} else if (r == Richtung.R_MITTE) {
			return nachfolgerMitte.getPrognose(fbzAktuell, fbtAktuell,
					tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert);
		} else if (r == Richtung.R_TENDENZBERECHNUNG_NICHT_MOEGLICH) {
			return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
		} else if (r == Richtung.R_NICHT_ERMITTELBAR) {
			return EntscheidungsBaum.EB_NICHT_ERMITTELBAR;
		} else if (r == Richtung.R_ENTSCHEIDUNG_NICHT_MOEGLICH) {
			return EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH;
		}

		// nur wegen Compiler
		return EntscheidungsBaum.EB_NICHT_ERMITTELBAR;
	}

	/**
	 * Konstruktor.
	 *
	 * @param methode
	 *            Die EntscheidungsMethode
	 * @param nachfolgerLinks
	 *            Der Nachfolger an der linken Seite
	 * @param nachfolgerRechts
	 *            Der Nachfolger an der rechten Seite
	 * @throws DUAInitialisierungsException
	 *             wird weitergerecht
	 */
	public EntscheidungsBaumKnoten(final EntscheidungsMethode methode,
			final EntscheidungsBaumKnoten nachfolgerLinks,
			final EntscheidungsBaumKnoten nachfolgerRechts)
					throws DUAInitialisierungsException {
		if ((nachfolgerLinks == null) || (nachfolgerRechts == null)
				|| (methode == null)) {
			throw new DUAInitialisierungsException(
					"Kein parameter darf null sein");
		}
		if (methode instanceof FahbrBahnZustandVollDefiniert) {
			throw new DUAInitialisierungsException(
					"Knoten mit Entscheidungsmethode Fahrbahnzustandmitglaette  soll 3 Nachfolger haben");
		}
		this.nachfolgerLinks = nachfolgerLinks;
		this.nachfolgerRechts = nachfolgerRechts;
		this.methode = methode;

	}

	/**
	 * Konstruktor.
	 *
	 * @param methode
	 *            Die EntscheidungsMethode
	 * @param nachfolgerLinks
	 *            Der Nachfolger an der linken Seite
	 * @param nachfolgerMitte
	 *            Der Nachfolger in der Mitte
	 * @param nachfolgerRechts
	 *            Der Nachfolger an der rechten Seite
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 */
	public EntscheidungsBaumKnoten(final EntscheidungsMethode methode,
			final EntscheidungsBaumKnoten nachfolgerLinks,
			final EntscheidungsBaumKnoten nachfolgerMitte,
			final EntscheidungsBaumKnoten nachfolgerRechts)
					throws DUAInitialisierungsException {
		if ((nachfolgerLinks == null) || (nachfolgerMitte == null)
				|| (nachfolgerRechts == null) || (methode == null)) {
			throw new DUAInitialisierungsException(
					"Kein parameter darf null sein");
		}
		this.nachfolgerLinks = nachfolgerLinks;
		this.nachfolgerMitte = nachfolgerMitte;
		this.nachfolgerRechts = nachfolgerRechts;
		this.methode = methode;
	}

	/**
	 * Konstruktor.
	 *
	 * @param ergebnisswert
	 *            Glaetteprognosewert
	 */
	public EntscheidungsBaumKnoten(final int ergebnisswert) {
		this.methode = null;
		this.ergebnisWert = ergebnisswert;
	}
}
