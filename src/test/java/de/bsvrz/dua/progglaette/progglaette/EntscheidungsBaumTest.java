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

import org.junit.Assert;
import org.junit.Test;

/**
 * Testet den Entscheidungsbaum.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class EntscheidungsBaumTest extends EntscheidungsBaum {

	/**
	 * Zustanaende.
	 */
	public static final long T = EntscheidungsBaum.FBZ_TROCKEN;

	/**
	 * Zustanaende.
	 */
	public static final long F = EntscheidungsBaum.FBZ_FEUCHT;

	/**
	 * Zustanaende.
	 */
	public static final long N = EntscheidungsBaum.FBZ_NASS;

	/**
	 * Zustanaende.
	 */
	public static final long W = EntscheidungsBaum.FBZ_GEFR_WASSER;

	/**
	 * Zustanaende.
	 */
	public static final long S = EntscheidungsBaum.FBZ_SCHNEE;

	/**
	 * Zustanaende.
	 */
	public static final long E = EntscheidungsBaum.FBZ_EIS;

	/**
	 * Zustanaende.
	 */
	public static final long R = EntscheidungsBaum.FBZ_RAUREIF;

	/**
	 * Zustanaende.
	 */
	public static final long U = EntscheidungsBaum.FBZ_UNDEFINIERT;

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
		// FBZ -1 nicht ermittelbar
		// Temperatur -1001 nicht ermittelbar
		// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert,
			// tptExtrapoliert, prognose
		{ EntscheidungsBaumTest.Z, -1001, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
		{ EntscheidungsBaumTest.Z, 5.1, -1001, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
		{
					EntscheidungsBaumTest.Z,
					6.2,
					1.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
		{ EntscheidungsBaumTest.Z, 5.2, 3.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_KEINE_GLAETTEGEHFAHR },
		{
					EntscheidungsBaumTest.Z,
					5.3,
					2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
		{ EntscheidungsBaumTest.S, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
		{ EntscheidungsBaumTest.E, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
		{ EntscheidungsBaumTest.W, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
		{ EntscheidungsBaumTest.R, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
		{ EntscheidungsBaumTest.N, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH_SOFORT },
		{ EntscheidungsBaumTest.F, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH_SOFORT },
		{ -1, 2, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
		{
					EntscheidungsBaumTest.T,
					2,
					EntscheidungsBaumTest.Z,
					2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
			EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
			{
					EntscheidungsBaumTest.T,
					1,
					EntscheidungsBaumTest.Z,
					3,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
				EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
				{
					EntscheidungsBaumTest.T,
					1,
					EntscheidungsBaumTest.Z,
					1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
					{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
						EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
						{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					1,
					1,
							EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
							{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					0.2,
					0.1,
								EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT },

								// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert,
			// tptExtrapoliert, prognose
								{ EntscheidungsBaumTest.T, -1, EntscheidungsBaumTest.Z, -3.1,
					EntscheidungsBaumTest.Z, -1001,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
								{ EntscheidungsBaumTest.T, -1, EntscheidungsBaumTest.Z, -3.1,
					-1001, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
								{ EntscheidungsBaumTest.T, -1, EntscheidungsBaumTest.Z, -1001,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
								{ EntscheidungsBaumTest.F, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
								{ EntscheidungsBaumTest.N, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
								{ -1, 2.2, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
								{ EntscheidungsBaumTest.Z, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, -1001, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
								{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					-1,
					-1,
									EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
									{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					-1,
					0.1,
										EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
										{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					1,
					0.1,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
										{ EntscheidungsBaumTest.T, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, -1001, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
										{ EntscheidungsBaumTest.T, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2, -1001,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
										{ -1, 2.2, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
										{ EntscheidungsBaumTest.T, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 0, -1001,
					EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH },
										{ -1, 2.2, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 0,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
										{ EntscheidungsBaumTest.T, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
										{ EntscheidungsBaumTest.T, 3, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
										{ EntscheidungsBaumTest.F, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
										{ EntscheidungsBaumTest.N, 3, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
										{ -1, 3, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_NICHT_ERMITTELBAR },
										{ -1, 3.1, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
										{ -1, 4, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },

										{
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
											EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
											{
					EntscheidungsBaumTest.Z,
					5.1,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
												EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
												{
					EntscheidungsBaumTest.Z,
					6.2,
					1.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
												{ EntscheidungsBaumTest.Z, 5.2, 3.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_KEINE_GLAETTEGEHFAHR },
												{
					EntscheidungsBaumTest.Z,
					5.3,
					2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
												{ EntscheidungsBaumTest.S, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
												{ EntscheidungsBaumTest.E, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
												{ EntscheidungsBaumTest.W, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
												{ EntscheidungsBaumTest.R, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTE_VORHANDEN },
												{ EntscheidungsBaumTest.N, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH_SOFORT },
												{ EntscheidungsBaumTest.F, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH_SOFORT },
												{
					EntscheidungsBaumTest.U,
					2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
													EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
													{
					EntscheidungsBaumTest.T,
					2,
					EntscheidungsBaumTest.Z,
					2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
														EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
														{
					EntscheidungsBaumTest.T,
					1,
					EntscheidungsBaumTest.Z,
					3,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
															EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
															{
					EntscheidungsBaumTest.T,
					1,
					EntscheidungsBaumTest.Z,
					1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
																EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE_SOFORT },
																{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
																	EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
																	{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					1,
					1,
																		EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT_SOWIE_REIFGLAETTE },
																		{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					0.2,
					0.1,
																			EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOFORT },

																			// fbzAktuell, fbtAktuell, lftAktuell, tptAktuell, fbtExtrapoliert,
			// tptExtrapoliert, prognose
																			{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.NV,
																				EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																				{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					-3.1,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
																					EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																					{
					EntscheidungsBaumTest.T,
					-1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
																						EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																						{ EntscheidungsBaumTest.F, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
																						{ EntscheidungsBaumTest.N, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
																						{
					EntscheidungsBaumTest.U,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					1,
					EntscheidungsBaumTest.Z,
																							EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																							{
					EntscheidungsBaumTest.Z,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
																								EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																								{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					-1,
					-1,
																									EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
																									{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					-1,
					0.1,
																										EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG_SOWIE_REIFGLAETTE },
																										{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					1,
					0.1,
					EntscheidungsBaum.EB_SCHNEEGLAETTE_GLATTEIS_BEI_NIEDERSCHLAG },
																										{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.NV,
					EntscheidungsBaumTest.Z,
																											EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																											{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					2,
					EntscheidungsBaumTest.NV,
																												EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																												{
					EntscheidungsBaumTest.U,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					2,
					EntscheidungsBaumTest.Z,
																													EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																													{
					EntscheidungsBaumTest.T,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					0,
					EntscheidungsBaumTest.NV,
																														EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																														{
					EntscheidungsBaumTest.U,
					2.2,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					0,
					EntscheidungsBaumTest.Z,
																															EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																															{ EntscheidungsBaumTest.T, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
																															{ EntscheidungsBaumTest.T, 3, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
																															{ EntscheidungsBaumTest.F, 2.2, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
																															{ EntscheidungsBaumTest.N, 3, EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z, 2.1, EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_EISGLAETTE_MOEGLICH },
																															{
					EntscheidungsBaumTest.U,
					3,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaumTest.Z,
					2.1,
					EntscheidungsBaumTest.Z,
																																EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH },
																																{ -1, 3.1, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG },
																																{ -1, 4, EntscheidungsBaumTest.Z, EntscheidungsBaumTest.Z, 2.1,
					EntscheidungsBaumTest.Z,
					EntscheidungsBaum.EB_GLAETTEGEFAHR_BEI_WETTERAENDERUNG }, };

	/**
	 * Die modifizierte tabelle.
	 */
	private static double[][] tabelle = new double[EntscheidungsBaumTest.TT.length][EntscheidungsBaumTest.TT[0].length];

	/**
	 * Kopiert Arrays.
	 *
	 * @param src
	 *            Quelle Array
	 * @param dst
	 *            Ziel Array
	 */
	public void copy(final double[][] src, final double[][] dst) {
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src[i].length; j++) {
				dst[i][j] = src[i][j];
			}
		}
	}

	/**
	 * Die Werte die, zufaellig sein koennen ( mit Z gekennzeichnet ), werden
	 * randomisiert.
	 */
	public void randomisiere() {
		boolean rand = false;
		for (int i = 0; i < EntscheidungsBaumTest.tabelle.length; i++) {
			for (int j = 0; j < EntscheidungsBaumTest.tabelle[i].length; j++) {
				if (EntscheidungsBaumTest.tabelle[i][j] == EntscheidungsBaumTest.Z) {
					EntscheidungsBaumTest.tabelle[i][j] = (-1010 + (Math
							.random() * 2020));
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
			copy(EntscheidungsBaumTest.TT, EntscheidungsBaumTest.tabelle);
			if (j != 0) {
				randomisiere();
			}
			for (int i = 0; i < EntscheidungsBaumTest.tabelle.length; i++) {

				fbzAktuell = (long) (EntscheidungsBaumTest.tabelle[i][0]);
				fbtAktuell = EntscheidungsBaumTest.tabelle[i][1];
				lftAktuell = EntscheidungsBaumTest.tabelle[i][2];
				tptAktuell = EntscheidungsBaumTest.tabelle[i][3];

				fbtExtrapoliert = EntscheidungsBaumTest.tabelle[i][4];
				tptExtrapoliert = EntscheidungsBaumTest.tabelle[i][5];

				antwort = (int) (EntscheidungsBaumTest.tabelle[i][6]);

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
