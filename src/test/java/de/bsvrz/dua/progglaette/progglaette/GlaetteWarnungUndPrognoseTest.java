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

import java.util.Date;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;

/**
 * Testet die Klasse Glaettewarnung und -prognose testet nur die Zeitstempel, ob
 * die daten im korrekten Zeitpunkt publiziert werden.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 */
@Ignore("Testdatenverteiler prüfen")
public class GlaetteWarnungUndPrognoseTest extends GlaetteWarnungUndPrognose {

	/**
	 * Testsensoren.
	 */
	private static SystemObject fbzSensor, fbtSensor, ltSensor, tptSensor, messStelle;
	/**
	 * datenbeschreibung fuer testdaten.
	 */
	private static DataDescription ddLftDaten, ddTptDaten, ddFbtDaten, ddFbzDaten;

	/**
	 * Zeitstempel als Referenzwerte.
	 */
	private static long[] zeitStempel;

	/**
	 * Zeitablauf der geschickten Daten, 1 - Daten geschickt, 0 - Ausfall.
	 *
	 * Es werden die Default Werte Abgeschickt (0), so die Lufttemperatur ist
	 * nich noetig zur Errechnung der Ergebnisse, weil dieser
	 * Entscheidungsknoten wird nicht auswertet
	 *
	 * Wenn kein Ergebniss im vorherigen Intervall erzeugt Wurde, und 2 DS in
	 * aktuellen gerade gekommen sind, dann publiziern wir einen nicht
	 * ermittelbaren DS fuer den vorherigen Intervall
	 */
	private final int[] lft = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	/**
	 * dito.
	 */
	private final int[] fbt = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0 };
	/**
	 * dito.
	 */
	private final int[] tpt = new int[] { 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1,
			0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	/**
	 * dito.
	 */
	private final int[] fbz = new int[] { 1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1,
			0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	/**
	 * anzahl der publizierten DS im jeden schritt.
	 */
	private final int[] pub = new int[] { 2, 0, 0, 1, 0, 0, 1, 0, 2, 1, 1, 1, 1, 0, 1, 1, 2, 0, 1, 2, 0, 0, 1, 0, 0, 1,
			0, 2, 1, 1, 1, 1, 0, 1, 1, 2, 0, 1 };

	/**
	 * Index im ZS array.
	 */
	private static int index = 0;
	/**
	 * Ob man warten soll.
	 */
	private static boolean warten = true;

	/**
	 * Der Test.
	 */
	@Test
	public void test1() {
		final GlaetteWarnungUndPrognoseTest gwp = new GlaetteWarnungUndPrognoseTest();
		StandardApplicationRunner.run(gwp, Verbindung.CON_DATA.clone());

		try {
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, GlaetteWarnungUndPrognoseTest.tptSensor,
					GlaetteWarnungUndPrognoseTest.ddTptDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, GlaetteWarnungUndPrognoseTest.ltSensor,
					GlaetteWarnungUndPrognoseTest.ddLftDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, GlaetteWarnungUndPrognoseTest.fbtSensor,
					GlaetteWarnungUndPrognoseTest.ddFbtDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, GlaetteWarnungUndPrognoseTest.fbzSensor,
					GlaetteWarnungUndPrognoseTest.ddFbzDaten, SenderRole.source());
		} catch (final Exception e) {
			System.out.println("Fehler bei Anmeldung fuer Sendung der Testdaten");
			e.printStackTrace();
		}

		int sum = 0;
		for (final int element : pub) {
			sum += element;
		}

		GlaetteWarnungUndPrognoseTest.zeitStempel = new long[sum];

		final long basisZS = System.currentTimeMillis() - (100 * minInMs);

		int k = 0;
		boolean vorherigeOk = false;
		for (int i = 0; i < GlaetteWarnungUndPrognoseTest.zeitStempel.length; i++) {
			while (pub[k] == 0) {
				k++;
				vorherigeOk = false;
			}
			if (pub[k] == 1) {
				if (vorherigeOk) {
					GlaetteWarnungUndPrognoseTest.zeitStempel[i] = basisZS + (k * minInMs);
				} else {
					GlaetteWarnungUndPrognoseTest.zeitStempel[i] = basisZS + ((k - 1) * minInMs);
				}
			} else if (pub[k] == 2) {
				GlaetteWarnungUndPrognoseTest.zeitStempel[i] = basisZS + ((k - 1) * minInMs);
				GlaetteWarnungUndPrognoseTest.zeitStempel[++i] = basisZS + (k * minInMs);
				vorherigeOk = true;
			}
			k++;
		}

		for (int i = 0; i < pub.length; i++) {
			sendeDaten(lft[i], fbt[i], tpt[i], fbz[i], basisZS + ((i) * minInMs));
		}

		synchronized (GlaetteWarnungUndPrognose.dav) {
			try {
				while (GlaetteWarnungUndPrognoseTest.warten) {
					GlaetteWarnungUndPrognose.dav.wait();
				}
			} catch (final Exception e) {
				//
			}
		}
	}

	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {
		super.initialize(connection);

		GlaetteWarnungUndPrognoseTest.fbzSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFZS.1");
		GlaetteWarnungUndPrognoseTest.fbtSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFT.1");
		GlaetteWarnungUndPrognoseTest.ltSensor = connection.getDataModel().getObject("ufdSensor.test.LT.1");
		GlaetteWarnungUndPrognoseTest.tptSensor = connection.getDataModel().getObject("ufdSensor.test.TPT.1");

		GlaetteWarnungUndPrognoseTest.messStelle = connection.getDataModel().getObject("ufdMessStelle.test.1");

		GlaetteWarnungUndPrognoseTest.ddTptDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_TPT),
						GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		GlaetteWarnungUndPrognoseTest.ddFbtDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBT),
						GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		GlaetteWarnungUndPrognoseTest.ddFbzDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBZ),
						GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		GlaetteWarnungUndPrognoseTest.ddLftDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_LFT),
						GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

	}

	@Override
	public void publiziere(final UmfDatenHist ud, final Data daten, final long zeitStempel1, final boolean keineDaten) {
		super.publiziere(ud, daten, zeitStempel1, keineDaten);
		if (ud.messStelle != GlaetteWarnungUndPrognoseTest.messStelle) {
			return;
		}
		if (GlaetteWarnungUndPrognoseTest.index >= GlaetteWarnungUndPrognoseTest.zeitStempel.length) {
			return;
		}
		Assert.assertEquals(
				String.format("Soll %s Ist %s",
						new Date(GlaetteWarnungUndPrognoseTest.zeitStempel[GlaetteWarnungUndPrognoseTest.index]),
								new Date(zeitStempel1)),
				GlaetteWarnungUndPrognoseTest.zeitStempel[GlaetteWarnungUndPrognoseTest.index], zeitStempel1);
		System.out.println(
				String.format("[ %4d ] ZS OK %s", GlaetteWarnungUndPrognoseTest.index, new Date(zeitStempel1)));
		GlaetteWarnungUndPrognoseTest.index++;
		synchronized (GlaetteWarnungUndPrognose.dav) {
			if (GlaetteWarnungUndPrognoseTest.index >= GlaetteWarnungUndPrognoseTest.zeitStempel.length) {
				GlaetteWarnungUndPrognoseTest.warten = false;
				GlaetteWarnungUndPrognose.dav.notify();
			}
		}
	}

	/**
	 * Initielaisiert den DS.
	 *
	 * @param att
	 *            Attributname
	 * @param data
	 *            Datensatz
	 */
	public void setztAufDefault(final String att, final Data data) {
		data.getItem("T").asTimeValue().setMillis(minInMs);
		data.getItem(att).getItem("Wert").asUnscaledValue().set(0);
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
	}

	/**
	 * Sendet daten fuer Alle Sensoren.
	 *
	 * @param lft1
	 *            1, wenn Daten fuer Lufttemperatur geschickt werden sollen
	 * @param fbt1
	 *            1, wenn Daten fuer Fahrbahnoberflachentemperatur geschickt
	 *            werden sollen
	 * @param tpt1
	 *            1, wenn Daten fuer Taupunkttemperatur geschickt werden sollen
	 * @param fbz1
	 *            1, wenn Daten fuer Fahrbahnoberflachenzustand geschickt werden
	 *            sollen
	 * @param zs1
	 *            Zeitestempel mid dem die Daten geschickt werden
	 */
	public void sendeDaten(final int lft1, final int fbt1, final int tpt1, final int fbz1, final long zs1) {
		try {
			if (fbt1 > 0) {
				final Data daten = GlaetteWarnungUndPrognose.dav.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBT));
				setztAufDefault("FahrBahnOberFlächenTemperatur", daten);
				final ResultData resData = new ResultData(GlaetteWarnungUndPrognoseTest.fbtSensor,
						GlaetteWarnungUndPrognoseTest.ddFbtDaten, zs1, daten);
				GlaetteWarnungUndPrognose.dav.sendData(resData);
			}
			if (lft1 > 0) {
				final Data daten = GlaetteWarnungUndPrognose.dav.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAttributeGroup(GlaetteWarnungUndPrognose.ATG_LFT));
				setztAufDefault("LuftTemperatur", daten);
				final ResultData resData = new ResultData(GlaetteWarnungUndPrognoseTest.ltSensor,
						GlaetteWarnungUndPrognoseTest.ddLftDaten, zs1, daten);
				GlaetteWarnungUndPrognose.dav.sendData(resData);
			}
			if (fbz1 > 0) {
				final Data daten = GlaetteWarnungUndPrognose.dav.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBZ));
				setztAufDefault("FahrBahnOberFlächenZustand", daten);
				final ResultData resData = new ResultData(GlaetteWarnungUndPrognoseTest.fbzSensor,
						GlaetteWarnungUndPrognoseTest.ddFbzDaten, zs1, daten);
				GlaetteWarnungUndPrognose.dav.sendData(resData);
			}
			if (tpt1 > 0) {
				final Data daten = GlaetteWarnungUndPrognose.dav.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAttributeGroup(GlaetteWarnungUndPrognose.ATG_TPT));
				setztAufDefault("TaupunktTemperatur", daten);
				final ResultData resData = new ResultData(GlaetteWarnungUndPrognoseTest.tptSensor,
						GlaetteWarnungUndPrognoseTest.ddTptDaten, zs1, daten);
				GlaetteWarnungUndPrognose.dav.sendData(resData);
			}
		} catch (final Exception e) {
			System.out.println("Fehler bei Sendung");
			e.printStackTrace();
		}
	}

}
