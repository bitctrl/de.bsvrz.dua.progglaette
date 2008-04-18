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

import java.util.Date;

import junit.framework.Assert;

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
 * 
 * @version $Id$
 */
public class GlaetteWarnungUndPrognoseTest extends GlaetteWarnungUndPrognose {

	/**
	 * Verbindungsdaten.
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", "-benutzer=Tester",
			"-authentifizierung=passwd",
			"-debugLevelStdErrText=OFF", "-debugLevelFileText=OFF",
			"-KonfigurationsBereichsPid=kb.glaetteProgTest" };

//	/**
//	 * Verbindungsdaten.
//	 */
//	public static final String[] CON_DATA = new String[] {
//			"-datenverteiler=10.44.44.10:8083", "-benutzer=Tester",
//			"-authentifizierung=c:\\passwd",
//			"-debugLevelStdErrText=OFF", "-debugLevelFileText=OFF",
//			"-KonfigurationsBereichsPid=kb.glaetteProgTest" };

	/**
	 * Testsensoren.
	 */
	private static SystemObject fbzSensor, fbtSensor, ltSensor, tptSensor,
			messStelle;
	/**
	 * datenbeschreibung fuer testdaten.
	 */
	private static DataDescription ddLftDaten, ddTptDaten, ddFbtDaten,
			ddFbzDaten;

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
	private int[] lft = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0 };
	/**
	 * dito.
	 */
	private int[] fbt = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0,
			0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1,
			0, 0 };
	/**
	 * dito.
	 */
	private int[] tpt = new int[] { 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1 };
	/**
	 * dito.
	 */
	private int[] fbz = new int[] { 1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1 };
	/**
	 * anzahl der publizierten DS im jeden schritt.
	 */	
	private int[] pub = new int[] { 2, 0, 0, 1, 0, 0, 1, 0, 2, 1, 1, 1, 1, 0,
			1, 1, 2, 0, 1, 2, 0, 0, 1, 0, 0, 1, 0, 2, 1, 1, 1, 1, 0, 1, 1, 2,
			0, 1 };

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
		GlaetteWarnungUndPrognoseTest gwp = new GlaetteWarnungUndPrognoseTest();
		StandardApplicationRunner.run(gwp, CON_DATA);

		try {
			dav.subscribeSender(this, tptSensor, ddTptDaten, SenderRole
					.source());
			dav.subscribeSender(this, ltSensor, ddLftDaten, SenderRole
					.source());
			dav.subscribeSender(this, fbtSensor, ddFbtDaten, SenderRole
					.source());
			dav.subscribeSender(this, fbzSensor, ddFbzDaten, SenderRole
					.source());
		} catch (Exception e) {
			System.out
					.println("Fehler bei Anmeldung fuer Sendung der Testdaten");
			e.printStackTrace();
		}

		int sum = 0;
		for (int i = 0; i < pub.length; i++) {
			sum += pub[i];
		}

		GlaetteWarnungUndPrognoseTest.zeitStempel = new long[sum];

		long basisZS = System.currentTimeMillis() - 100 * minInMs;

		int k = 0;
		boolean vorherigeOk = false;
		for (int i = 0; i < zeitStempel.length; i++) {
			while (pub[k] == 0) {
				k++;
				vorherigeOk = false;
			}
			if (pub[k] == 1) {
				if (vorherigeOk) {
					zeitStempel[i] = basisZS + k * minInMs;
				} else {
					zeitStempel[i] = basisZS + (k - 1) * minInMs;
				}
			} else if (pub[k] == 2) {
				zeitStempel[i] = basisZS + (k - 1) * minInMs;
				zeitStempel[++i] = basisZS + k * minInMs;
				vorherigeOk = true;
			}
			k++;
		}

		for (int i = 0; i < pub.length; i++) {
			sendeDaten(lft[i], fbt[i], tpt[i], fbz[i], basisZS + (i) * minInMs);
		}

		synchronized (GlaetteWarnungUndPrognoseTest.dav) {
			try {
				while (warten) {
					dav.wait();
				}
			} catch (Exception e) {
				//
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		super.initialize(connection);

		fbzSensor = connection.getDataModel().getObject(
				"ufdSensor.test.FBOFZS.1");
		fbtSensor = connection.getDataModel().getObject(
				"ufdSensor.test.FBOFT.1");
		ltSensor = connection.getDataModel().getObject("ufdSensor.test.LT.1");
		tptSensor = connection.getDataModel().getObject("ufdSensor.test.TPT.1");

		messStelle = connection.getDataModel()
				.getObject("ufdMessStelle.test.1");

		ddTptDaten = new DataDescription(dav.getDataModel().getAttributeGroup(
				ATG_TPT), dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));

		ddFbtDaten = new DataDescription(dav.getDataModel().getAttributeGroup(
				ATG_FBT), dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));

		ddFbzDaten = new DataDescription(dav.getDataModel().getAttributeGroup(
				ATG_FBZ), dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));

		ddLftDaten = new DataDescription(dav.getDataModel().getAttributeGroup(
				ATG_LFT), dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void publiziere(UmfDatenHist ud, Data daten, long zeitStempel1,
			boolean keineDaten) {
		super.publiziere(ud, daten, zeitStempel1, keineDaten);
		if (ud.messStelle != messStelle) {
			return;
		}
		if (index >= GlaetteWarnungUndPrognoseTest.zeitStempel.length) {
			return;
		}
		Assert.assertEquals(String.format("Soll %s Ist %s", new Date(
				GlaetteWarnungUndPrognoseTest.zeitStempel[index]), new Date(
				zeitStempel1)),
				GlaetteWarnungUndPrognoseTest.zeitStempel[index], zeitStempel1);
		System.out.println(String.format("[ %4d ] ZS OK %s", index, new Date(
				zeitStempel1)));
		index++;
		synchronized (dav) {
			if (index >= GlaetteWarnungUndPrognoseTest.zeitStempel.length) {
				warten = false;
				dav.notify();
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
	public void setztAufDefault(String att, Data data) {
		data.getItem("T").asTimeValue().setMillis(minInMs);
		data.getItem(att).getItem("Wert").asUnscaledValue().set(0);
		data.getItem(att).getItem("Status").getItem("Erfassung")
				.getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal")
				.getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal")
				.getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung")
				.getUnscaledValue("Interpoliert").set(0);
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
	public void sendeDaten(int lft1, int fbt1, int tpt1, int fbz1, long zs1) {
		try {
			if (fbt1 > 0) {
				Data daten = dav.createData(dav.getDataModel()
						.getAttributeGroup(ATG_FBT));
				setztAufDefault("FahrBahnOberFlächenTemperatur", daten);
				ResultData resData = new ResultData(fbtSensor, ddFbtDaten, zs1,
						daten);
				dav.sendData(resData);
			}
			if (lft1 > 0) {
				Data daten = dav.createData(dav.getDataModel()
						.getAttributeGroup(ATG_LFT));
				setztAufDefault("LuftTemperatur", daten);
				ResultData resData = new ResultData(ltSensor, ddLftDaten, zs1,
						daten);
				dav.sendData(resData);
			}
			if (fbz1 > 0) {
				Data daten = dav.createData(dav.getDataModel()
						.getAttributeGroup(ATG_FBZ));
				setztAufDefault("FahrBahnOberFlächenZustand", daten);
				ResultData resData = new ResultData(fbzSensor, ddFbzDaten, zs1,
						daten);
				dav.sendData(resData);
			}
			if (tpt1 > 0) {
				Data daten = dav.createData(dav.getDataModel()
						.getAttributeGroup(ATG_TPT));
				setztAufDefault("TaupunktTemperatur", daten);
				ResultData resData = new ResultData(tptSensor, ddTptDaten, zs1,
						daten);
				dav.sendData(resData);
			}
		} catch (Exception e) {
			System.out.println("Fehler bei Sendung");
			e.printStackTrace();
		}
	}

}
