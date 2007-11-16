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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * 
 * Hauptklasse des Moduls Glaettewarnung und -prognose
 * Bearbeitet die aufrufsparameter, meldet sich an die Eingabedaten ein
 * und publiziert die Ausgaben
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class GlaetteWarnungUndPrognose implements ClientSenderInterface,
		ClientReceiverInterface, StandardApplication {
	
	/**
	 * String konstanten
	 */
	private final  String ATG_LFT = "atg.ufdsLuftTemperatur";
	private final  String ATG_FBT = "atg.ufdsFahrBahnOberFlächenTemperatur";
	private final  String ATG_TPT = "atg.ufdsTaupunktTemperatur";
	private final  String ATG_FBZ = "atg.ufdsFahrBahnOberFlächenZustand";
	private final  String ATG_GLAETTE = "atg.ufdmsGlätte";
	private final  String ATG_UFDSENSOR = "atg.umfeldDatenSensor";

	private final  String ASP_MESSWERT_ERSETZUNG = "asp.messWertErsetzung";
	private final  String ASP_PROGNOSE = "asp.prognose";
	private final  String TYP_MESSSTELLE = "typ.umfeldDatenMessStelle";
	
	private final  String TYP_LFT = "typ.ufdsLuftTemperatur";
	private final  String TYP_FBT = "typ.ufdsFahrBahnOberFlächenTemperatur";
	private final  String TYP_TPT = "typ.ufdsTaupunktTemperatur";
	private final  String TYP_FBZ = "typ.ufdsFahrBahnOberFlächenZustand";
	
	/**
	 * Aufrufsparameter
	 */
	private final String P_KONF_BEREICHE = "-KonfigurationsBeriechsPid";
	
	/**
	 *  Minute in ms
	 */
	private final long MIN_IN_MS = 60 * 1000l;
	
	/**
	 * Die  Datenbeschreibung fuer Ausgabedaten
	 */
	private DataDescription DD_GLAETTEPROGNOSE;
	
	/** 
	 *  Menge der Sensoren die zu eine Messstelle gehoeren
	 */
	protected static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	
	/**
	 * Die uebergebene konfigurationsbereiche
	 */
	private String [] konfBereiche = null;
	
	/**
	 * Verbindung zum DAV
	 */
	private ClientDavInterface dav;
	
	/**
	 * Enthaelt die Ringpuffer und andere Daten fuer die Berechnungen pro MessStelle
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private static class UmfDatenHist {
		/**
		 * Standardkonstruktor
		 * @param messStelle
		 */
		public UmfDatenHist(SystemObject messStelle) {
			this.messStelle = messStelle;
		}
		/**
		 * Wird nur bei Publizierung der ausgaben benoetigt
		 */
		public SystemObject messStelle = null;
		/**
		 * Groesse des Ringpuffers
		 */
		public static final int PUFFER_GROESSE = 10;
		
		/**
		 * Rigpuffer fuer zeitStempel
		 */
		public long zsPuffer [] = new long[PUFFER_GROESSE];
		
		/**
		 * Ringpuffer fuer Taupunkttemperaturen
		 */
		public double tptPuffer [] = new double[PUFFER_GROESSE];
		
		/**
		 * Ringpuffer fuer Fahrbahnoberflaechentemperaturen 
		 */
		public double fbtPuffer [] = new double[PUFFER_GROESSE];
		
		/**
		 * Letzter empfangene Fahrbahnoberflaechenzustand
		 */
		public long   letzteFbz = 0;
		/**
		 * Letzte empfangene Fahrbahnoberflaechentemperatur
		 */
		public double letzteFbt = 0;
		/**
		 * Letzte empfangene Taupunkttemperatur
		 */
		public double letzteTpt = 0;
		/**
		 * Letzte empfangene Lufttemperatur
		 */
		public double letzteLft = 0;
		/**
		 * Zeitstempel der letzten Fahrbahnoberflaechentemperatur
		 */
		public long zsLetzterFbt = 0;
		/**
		 * Zeitstempel der letzten Taupunkttemperatur
		 */
		public long zsLetzterTpt = 0;
		/**
		 * Zeitstempel des letzten Fahrbahnoberflaechenzustandes
		 */
		public long zsLetztenFbz = 0;
		/**
		 * Zeitstempel der letzten Lufttemperatur
		 */
		public long zsLetzterLft = 0;
		/**
		 * Index fuer neachsten Eintrag im Ringpuffer
		 */
		public int index = 0;
		/**
		 * Zeitstempel des letzten publizierten DS
		 */
		public long letzterPubZs = 0;
		/**
		 * Flag bestimmt, ob ein datensatz mit "keine Daten" geschickt wurde
		 */
		public boolean keineDaten = true;

	};
	/**
	 * Der Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Hashtabelle abbildet die sensoren auf die Messstelledaten
	 */
	private HashMap<SystemObject, UmfDatenHist> mapUmfDaten = new HashMap<SystemObject, UmfDatenHist> ();
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] results) {
		for(ResultData resDatei : results) {
			Data daten = resDatei.getData();
			SystemObject objekt = resDatei.getObject();
			UmfDatenHist umfDaten = mapUmfDaten.get(objekt);
			long zs = resDatei.getDataTime();
			if(umfDaten == null) {
				LOGGER.error("Umfelddaten fuer Messstelle nicht gefunden: " + objekt.getPid());
				continue;
			}			
			
			// wenn keine eingabedaten zur verfuaegung stahen, dann auch die ausgabe muss angepasst werden
			if(daten == null && umfDaten.keineDaten == false) {
				umfDaten.keineDaten = true;
				publiziere(umfDaten, null, zs, true);
			}
			
			// Nur daten in Minutenintervall werden bearbeitet			
			if(daten.getTimeValue("T").getMillis() != MIN_IN_MS) continue;
			
			String atgPid =  resDatei.getDataDescription().getAttributeGroup().getPid();
			
			if( ATG_FBT.equals(atgPid)) {
				double d = daten.getItem("FahrBahnOberFlächenTemperatur").getUnscaledValue("Wert").longValue();
				if(d > 0 )
					d = daten.getItem("FahrBahnOberFlächenTemperatur").getScaledValue("Wert").doubleValue();
				umfDaten.letzteFbt = d;
				umfDaten.zsLetzterFbt = zs;
				bearbeiteDaten(umfDaten, zs);
			}
			else if( ATG_LFT.equals(atgPid)) {
				double d = daten.getItem("LuftTemperatur").getUnscaledValue("Wert").longValue();
				if(d > 0 )
					d = daten.getItem("LuftTemperatur").getScaledValue("Wert").doubleValue();
				umfDaten.letzteLft = d;
				umfDaten.zsLetzterLft = zs;
				bearbeiteDaten(umfDaten, zs);
			}
			else if( ATG_TPT.equals(atgPid)) {
				double d = daten.getItem("TaupunktTemperatur").getUnscaledValue("Wert").longValue();
				if(d > 0 )
					d = daten.getItem("TaupunktTemperatur").getScaledValue("Wert").doubleValue();
				umfDaten.letzteTpt = d;
				umfDaten.zsLetzterTpt = zs;
				bearbeiteDaten(umfDaten, zs);
			}
			else if( ATG_FBZ.equals(atgPid)) {
				long d = daten.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").longValue();
				if(d > 0 )
					d = daten.getItem("FahrBahnOberFlächenZustand").getScaledValue("Wert").longValue();
				umfDaten.letzteFbz = d;
				umfDaten.zsLetztenFbz = zs;
				bearbeiteDaten(umfDaten, zs);
			}
		}
	}
	
	/**
	 * Wird bei jedem einkomenden plausiblen datensatz gerufen
	 * @param umfDaten Datenstruktur mit Daten der Messstelle
	 * @param zeitStempel Aktueller Zeitstempel
	 */
	public void bearbeiteDaten(UmfDatenHist umfDaten, long zeitStempel) {
		// alle 4 gleich mit zeitStempel
		if(umfDaten.zsLetzterFbt == umfDaten.zsLetztenFbz &&
				umfDaten.zsLetzterLft == umfDaten.zsLetzterTpt &&
					umfDaten.zsLetzterLft == umfDaten.zsLetztenFbz &&
						umfDaten.zsLetzterLft == zeitStempel ) {
			
			umfDaten.fbtPuffer[umfDaten.index] = umfDaten.letzteFbt;
			umfDaten.tptPuffer[umfDaten.index] = umfDaten.letzteTpt;
			umfDaten.index = (umfDaten.index +1) % umfDaten.PUFFER_GROESSE;
			
			if(zeitStempel - umfDaten.zsPuffer[umfDaten.index] > 10 * MIN_IN_MS + 100) {
				// Wir loeschen die eintraege die Aelter als 10 Minuten sind
				for(int i=0; i< umfDaten.zsPuffer.length; i++)
					if((zeitStempel - umfDaten.zsPuffer[i]) > 10 * MIN_IN_MS + 100)
						umfDaten.zsPuffer[i] = 0;
				}		
			publizierePrognose(umfDaten, zeitStempel);
			umfDaten.letzterPubZs = zeitStempel;
		} 
		
		// Wenn schon 2 datensaetze gekommen sind und im vorheridgen intervall wurde noch nicht publiziert
		int anzahl = 0;
		if(umfDaten.zsLetzterFbt == zeitStempel) anzahl++;
		if(umfDaten.zsLetzterTpt == zeitStempel) anzahl++;
		if(umfDaten.zsLetzterLft == zeitStempel) anzahl++;
		if(umfDaten.zsLetztenFbz == zeitStempel) anzahl++;
		
		if(anzahl == 2 && umfDaten.letzterPubZs < zeitStempel - 10*MIN_IN_MS) {
			umfDaten.letzterPubZs = zeitStempel - 10 * MIN_IN_MS;
			publiziereNichtErmmittelbar(umfDaten, zeitStempel - 10 * MIN_IN_MS);
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initialize(ClientDavInterface connection) throws Exception {
		
		this.dav = connection;
		Collection<SystemObject> messStellen = new LinkedList<SystemObject>();
		Collection<ConfigurationArea> konfBerieche = new LinkedList<ConfigurationArea>();
		Collection<SystemObjectType> typen = new LinkedList<SystemObjectType>();
		
		for(String s: this.konfBereiche) 
			konfBerieche.add(dav.getDataModel().getConfigurationArea(s));
		
		typen.add(dav.getDataModel().getType(TYP_MESSSTELLE));
		messStellen = dav.getDataModel().getObjects(konfBerieche, typen, ObjectTimeSpecification.valid());
		
		SystemObject fbtSensor, lftSensor, fbzSensor, tptSensor;
		Collection<SystemObject> fbtSensorMenge, lftSensorMenge, fbzSensorMenge, tptSensorMenge;
		fbtSensorMenge = new LinkedList<SystemObject>();
		lftSensorMenge = new LinkedList<SystemObject>();
		fbzSensorMenge = new LinkedList<SystemObject>();
		tptSensorMenge = new LinkedList<SystemObject>();
		
		for(SystemObject so: messStellen) {
			if(!(so  instanceof ConfigurationObject)) continue;
			ConfigurationObject confObjekt = (ConfigurationObject)so;
			ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
			fbtSensor =  lftSensor =  fbzSensor =  tptSensor = null;
			for( SystemObject sensor : sensorMenge.getElements()) {
				ConfigurationObject confObjekt2 = (ConfigurationObject)sensor;
				Data konfDaten = confObjekt2.getConfigurationData(
							dav.getDataModel().getAttributeGroup(ATG_UFDSENSOR));
				
				if(TYP_LFT.equals(sensor.getType().getPid()) &&
					konfDaten.getUnscaledValue("Typ").intValue() == 0) 
						lftSensor = sensor;
				else if(TYP_FBT.equals(sensor.getType().getPid()) &&
						konfDaten.getUnscaledValue("Typ").intValue() == 0) 
							fbtSensor = sensor;
				else if(TYP_FBZ.equals(sensor.getType().getPid()) &&
						konfDaten.getUnscaledValue("Typ").intValue() == 0) 
							fbzSensor = sensor;
				else if(TYP_TPT.equals(sensor.getType().getPid()) &&
						konfDaten.getUnscaledValue("Typ").intValue() == 0) 
							tptSensor = sensor;
			}
			if( lftSensor == null) { 
				LOGGER.warning("Messstelle " + so.getPid() + " enthaelt keinen Lufttemperatur Hauptsensor");
				continue;
			}
			else if( fbtSensor == null) { 
				LOGGER.warning("Messstelle " + so.getPid() + " enthaelt keinen Fahrbahnoberflaechentemperatur Hauptsensor");
				continue;
			}
			else if( fbzSensor == null) { 
				LOGGER.warning("Messstelle " + so.getPid() + " enthaelt keinen Fahrbahnoberflaechenzustand Hauptsensor");
				continue;
			}
			else if( tptSensor == null) { 
				LOGGER.warning("Messstelle " + so.getPid() + " enthaelt keinen Taupunkttemperatur Hauptsensor");
				continue;
			}

			UmfDatenHist ud = new UmfDatenHist(so);

			// Die datenstruktur der Messstelle soll mit Hilfe jedes Sensors gefunden werden
			mapUmfDaten.put(lftSensor, ud);
			mapUmfDaten.put(fbtSensor, ud);
			mapUmfDaten.put(tptSensor, ud);
			mapUmfDaten.put(fbzSensor, ud);
			
			tptSensorMenge.add(tptSensor);
			fbtSensorMenge.add(fbtSensor);
			fbzSensorMenge.add(fbzSensor);
			lftSensorMenge.add(lftSensor);
		}
		
		DataDescription DD_TPTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_TPT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DataDescription DD_FBTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_FBT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DataDescription DD_FBZDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_FBZ), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		DataDescription DD_LFTDATEN = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_LFT), 
				dav.getDataModel().getAspect(ASP_MESSWERT_ERSETZUNG));
		
		dav.subscribeReceiver(this, tptSensorMenge, DD_TPTDATEN, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbtSensorMenge, DD_FBTDATEN, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbzSensorMenge, DD_FBZDATEN, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, lftSensorMenge, DD_LFTDATEN, ReceiveOptions.normal(), ReceiverRole.receiver());
		
		DD_GLAETTEPROGNOSE = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_GLAETTE), 
				dav.getDataModel().getAspect(ASP_PROGNOSE));
		
		dav.subscribeSender(this, messStellen, DD_GLAETTEPROGNOSE, SenderRole.source());
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void parseArguments(ArgumentList argumente) throws Exception {
		Debug.init(this.getSWETyp().toString(), argumente);

		if( ! argumente.hasArgument(P_KONF_BEREICHE)) {
			throw new Exception("Keine Konfigurationsobjekte eingegeben.");		
		}
		
		String argParameter;
		argParameter = argumente.fetchArgument(P_KONF_BEREICHE).asString();
		this.konfBereiche = argParameter.split(",");

		argumente.fetchUnusedArguments();
	}
	/**
	 * Publiziert einen Datensatz, der als "nicht ermittelbar" gekennzeichnet ist
	 * @param ud Messstelledaten
	 * @param zeitStempel Aktueller Zeistempel
	 */
	public void publiziereNichtErmmittelbar(UmfDatenHist ud, long zeitStempel) {
		final String ATT_GLAETTE_PROGNOSE [] = new String [] { "AktuellerZustand", "PrognoseZustandIn5Minuten", 
				"PrognoseZustandIn15Minuten", "PrognoseZustandIn30Minuten", 
				"PrognoseZustandIn60Minuten", "PrognoseZustandIn90Minuten"
		};
	
		Data glaetteDs = dav.createData(dav.getDataModel().getAttributeGroup(ATG_GLAETTE));
		
		glaetteDs.getItem(ATT_GLAETTE_PROGNOSE[0]).asUnscaledValue().set(-1);
		
		for(int i=0; i<5; i++) 
			glaetteDs.getItem(ATT_GLAETTE_PROGNOSE[i+1]).asUnscaledValue().set(-1);
		
		publiziere(ud, glaetteDs, zeitStempel, false);
	}

	/**
	 * Berechnet und publiziert die Prognose fuer eine Messstelle
	 * @param ud Messstelle daten
	 * @param zeitStempel Aktueller Zeitstempel
	 */
	public void publizierePrognose(UmfDatenHist ud, long zeitStempel) {

		final String ATT_GLAETTE_PROGNOSE [] = new String [] { "AktuellerZustand", "PrognoseZustandIn5Minuten", 
				"PrognoseZustandIn15Minuten", "PrognoseZustandIn30Minuten", 
				"PrognoseZustandIn60Minuten", "PrognoseZustandIn90Minuten"
		};
	
		int cnt = 0;
		for(int i=0; i< ud.zsPuffer.length; i++)
			if(ud.zsPuffer[i] != 0) cnt++;
		
		double fbtExtrapoliert[], tptExtrapoliert[];
		int prognose;
		
		Data glaetteDs = dav.createData(dav.getDataModel().getAttributeGroup(ATG_GLAETTE));
		
		// Wenn weniger als 7 
		if(cnt<7) {
			fbtExtrapoliert = new double[5];
			tptExtrapoliert = new double[5];
			for(int i=0; i<fbtExtrapoliert.length; i++) {
				fbtExtrapoliert[i] = -1001;
				tptExtrapoliert[i] = -1001;
			}
		} else {
			fbtExtrapoliert = PrognoseZustand.berechnePrognose(ud.fbtPuffer, ud.zsPuffer, (ud.index - 1)%UmfDatenHist.PUFFER_GROESSE);
			tptExtrapoliert = PrognoseZustand.berechnePrognose(ud.tptPuffer, ud.zsPuffer, (ud.index - 1)%UmfDatenHist.PUFFER_GROESSE);
		}
		
		prognose = EntscheidungsBaum.getPrognose(ud.letzteFbz, ud.letzteFbt, ud.letzteTpt, ud.letzteLft, ud.letzteFbt, ud.letzteTpt);
		glaetteDs.getItem(ATT_GLAETTE_PROGNOSE[0]).asUnscaledValue().set(prognose);
		
		for(int i=0; i<5; i++) {
			prognose = EntscheidungsBaum.getPrognose(ud.letzteFbz, ud.letzteFbt, ud.letzteTpt, ud.letzteLft, fbtExtrapoliert[i], tptExtrapoliert[i]);
			glaetteDs.getItem(ATT_GLAETTE_PROGNOSE[i+1]).asUnscaledValue().set(prognose);
		}
		
		publiziere(ud, glaetteDs, zeitStempel, false);
	}
	/**
	 * Publiziert einen Datensatz
	 * @param ud Messstelledaten 
	 * @param daten Datum zum publizieren
	 * @param zeitStempel Zeitstempel des Datums
	 * @param keineDaten <code>true</code> wenn eid Datensatz mit "keine Daten" publiziert werden soll
	 */
	public void publiziere(UmfDatenHist ud, Data daten, long zeitStempel, boolean keineDaten) {
		ResultData resultate;
		
		if(keineDaten == true) 
			resultate = new ResultData(ud.messStelle, DD_GLAETTEPROGNOSE, zeitStempel, null);
		else 
			resultate = new ResultData(ud.messStelle, DD_GLAETTEPROGNOSE, zeitStempel, daten);
	
		ud.keineDaten = keineDaten;
		try {
			dav.sendData(resultate);
		} catch (Exception e) {
			LOGGER.warning("Datenabsendung unmoeglich");
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Ergibt die Name der SWE
	 * @return die Name der SWE
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_DATENAUFBEREITUNG_UFD;
	}
	
	/**
	 * Haupmethode
	 * @param args Aufrufsparameter
	 */
	public static void main(String args[]) {
		GlaetteWarnungUndPrognose gwp = new GlaetteWarnungUndPrognose();
		StandardApplicationRunner.run(gwp, args);
	}
}
