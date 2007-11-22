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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;

/**
 * Testet die Klasse Glaettewarnung und -prognose
 * testet nur die Zeitstempel, ob die daten im korrekten Zeitpunkt publiziert werden
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class GlaetteWarnungUndPrognoseTest extends GlaetteWarnungUndPrognose {

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", 
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd",
			"-debugLevelStdErrText=WARNING",
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBeriechsPid=kb.glaetteProgTest"};
	
	/**
	 * Testsensoren
	 */
	private static SystemObject fbzSensor, fbtSensor, ltSensor, tptSensor, messStelle;
	/**
	 * datenbeschreibung fuer testdaten
	 */
	private static DataDescription DD_LFTDATEN, DD_TPTDATEN, DD_FBTDATEN, DD_FBZDATEN;
	
	/**
	 * Zeitstempel als Referenzwerte
	 */
	private static long zeitStempel[];
	
	/**
	 * Zeitablauf der geschickten daten, 1 - gschickt, 0 - Ausfall
	 */
	private int lftT [] = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	private int fbtT [] = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0 };
	private int tptT [] = new int[] { 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	private int fbzT [] = new int[] { 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	// anzahl der publizierten DS im jedem schritt
	private int pub  [] = new int[] { 2, 0, 0, 1, 0, 0, 1, 0, 1, 1, 2, 1, 1, 0, 1, 1, 2, 0, 1 };
	
	/**
	 * Index im ZS array
	 */
	private static int index = 0;
	/**
	 * Ob man warten soll
	 */
	private static boolean warten = true;
	
	/**
	 * Der Test
	 */
	@Test 
	public void test1() {
		GlaetteWarnungUndPrognoseTest gwp = new GlaetteWarnungUndPrognoseTest();
		StandardApplicationRunner.run(gwp, CON_DATA);
		
		try {
			dav.subscribeSender(this, tptSensor, DD_TPTDATEN, SenderRole.source());
			dav.subscribeSender(this, ltSensor,  DD_LFTDATEN, SenderRole.source());
			dav.subscribeSender(this, fbtSensor, DD_FBTDATEN, SenderRole.source());
			dav.subscribeSender(this, fbzSensor, DD_FBZDATEN, SenderRole.source());
		}catch (Exception e) {
			System.out.println("Fehler bei Anmeldung fuer Sendung der Testdaten");
			e.printStackTrace();
		}
		
		int sum = 0;
		for(int i=0; i<pub.length; i++) {
			sum += pub[i];
		}
		
		GlaetteWarnungUndPrognoseTest.zeitStempel = new long[sum];
		
		long basisZS = System.currentTimeMillis() - 100 * MIN_IN_MS;
		
		int k=0;
		boolean vorherige_ok = false;
		for(int i=0;i<zeitStempel.length; i++) {
			while(pub[k] == 0) {
				k++;
				vorherige_ok = false;
			}
			if(pub[k]==1) {
				if(vorherige_ok)
					zeitStempel[i] = basisZS  + k * MIN_IN_MS;
				else zeitStempel[i] = basisZS  + (k-1) * MIN_IN_MS;
			}
			else if(pub[k]==2) {
				zeitStempel[i] = basisZS  + (k-1) * MIN_IN_MS;
				zeitStempel[++i] = basisZS  + k * MIN_IN_MS;
				vorherige_ok = true;
			}
			k++;
		}
		
		for(int i=0; i<pub.length; i++) {
			sendeDaten(lftT[i], fbtT[i], tptT[i], fbzT[i], basisZS + (i)*MIN_IN_MS);
		}
		
		synchronized (GlaetteWarnungUndPrognoseTest.dav) {
			try {
				while(warten) dav.wait();
			} catch (Exception e) {	}
		}
	}
	 
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(ClientDavInterface connection) throws Exception {
		super.initialize(connection);
		
		fbzSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFZS.1");
		fbtSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFT.1");
		ltSensor = connection.getDataModel().getObject("ufdSensor.test.LT.1");
		tptSensor = connection.getDataModel().getObject("ufdSensor.test.TPT.1");
		
		messStelle = connection.getDataModel().getObject("ufdMessStelle.test.1");
		
		DD_TPTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_TPT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DD_FBTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_FBT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DD_FBZDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_FBZ), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DD_LFTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_LFT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void publiziere(UmfDatenHist ud, Data daten, long zeitStempel, boolean keineDaten) {
		if(ud.messStelle != messStelle) return;
		if(index>= GlaetteWarnungUndPrognoseTest.zeitStempel.length) return;
		Assert.assertEquals(GlaetteWarnungUndPrognoseTest.zeitStempel[index++], zeitStempel);
		System.out.println(String.format("[ %4d ] ZS OK", index-1));
		synchronized (dav) {
			if(index>= GlaetteWarnungUndPrognoseTest.zeitStempel.length) {
				warten = false;
				dav.notify();
			}
		}
	}
	
	/**
	 * Initielaisiert den DS
	 * @param att Attributname
	 * @param data Datensatz
	 */
	public void setztAufDefault(String att, Data data) {
		data.getItem("T").asTimeValue().setMillis(MIN_IN_MS);
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
	 * Sendet daten fuer Alle Sensoren
	 * @param lft 1, wenn Daten fuer Lufttemperatur geschickt werden sollen
	 * @param fbt 1, wenn Daten fuer Fahrbahnoberflachentemperatur geschickt werden sollen
	 * @param tpt 1, wenn Daten fuer Taupunkttemperatur geschickt werden sollen
	 * @param fbz 1, wenn Daten fuer Fahrbahnoberflachenzustand geschickt werden sollen
	 * @param zs Zeitestempel mid dem die Daten geschickt werden 
	 */
	public void sendeDaten(int lft, int fbt, int tpt, int fbz, long zs) {
		try {
			if(fbt>0) {
				Data daten = dav.createData(dav.getDataModel().getAttributeGroup(ATG_FBT));
				setztAufDefault("FahrBahnOberFlächenTemperatur", daten);
				ResultData resData = new ResultData(fbtSensor, DD_FBTDATEN, zs, daten);
				dav.sendData(resData);
			}
			if(lft>0) {
				Data daten = dav.createData(dav.getDataModel().getAttributeGroup(ATG_LFT));
				setztAufDefault("LuftTemperatur", daten);
				ResultData resData = new ResultData(ltSensor, DD_LFTDATEN, zs, daten);
				dav.sendData(resData);
			}
			if(fbz>0) {
				Data daten = dav.createData(dav.getDataModel().getAttributeGroup(ATG_FBZ));
				setztAufDefault("FahrBahnOberFlächenZustand", daten);
				ResultData resData = new ResultData(fbzSensor, DD_FBZDATEN, zs, daten);
				dav.sendData(resData);
			}
			if(tpt>0) {
				Data daten = dav.createData(dav.getDataModel().getAttributeGroup(ATG_TPT));
				setztAufDefault("TaupunktTemperatur", daten);
				ResultData resData = new ResultData(tptSensor, DD_TPTDATEN, zs, daten);
				dav.sendData(resData);
			}
		} catch (Exception e) {
			System.out.println("Fehler bei Sendung");
			e.printStackTrace();
		}
	}

}
