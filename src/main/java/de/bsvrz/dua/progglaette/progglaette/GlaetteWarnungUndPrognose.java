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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Hauptklasse des Moduls Glaettewarnung und -prognose Bearbeitet die
 * aufrufsparameter, meldet sich an die Eingabedaten ein und publiziert die
 * Ausgaben.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 * @version $Id: GlaetteWarnungUndPrognose.java 53825 2015-03-18 09:36:42Z
 *          peuker $
 */
public class GlaetteWarnungUndPrognose implements ClientSenderInterface,
		ClientReceiverInterface, StandardApplication {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Sollen die Prognosen mit ausgegeben werden?
	 */
	private static final boolean DEBUG = false;

	/**
	 * String konstanten.
	 */
	public static final String ATG_LFT = "atg.ufdsLuftTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String ATG_FBT = "atg.ufdsFahrBahnOberFlächenTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String ATG_TPT = "atg.ufdsTaupunktTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String ATG_FBZ = "atg.ufdsFahrBahnOberFlächenZustand";

	/**
	 * String konstanten.
	 */
	public static final String ATG_GLAETTE = "atg.ufdmsGlätte";

	/**
	 * String konstanten.
	 */
	public static final String ATG_UFDSENSOR = "atg.umfeldDatenSensor";

	/**
	 * String konstanten.
	 */
	public static final String ASP_MESSWERT_ERSETZUNG = "asp.messWertErsetzung";

	/**
	 * String konstanten.
	 */
	public static final String ASP_PROGNOSE = "asp.prognose";

	/**
	 * String konstanten.
	 */
	public static final String TYP_MESSSTELLE = "typ.umfeldDatenMessStelle";

	/**
	 * String konstanten.
	 */
	public static final String TYP_LFT = "typ.ufdsLuftTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String TYP_FBT = "typ.ufdsFahrBahnOberFlächenTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String TYP_TPT = "typ.ufdsTaupunktTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String TYP_FBZ = "typ.ufdsFahrBahnOberFlächenZustand";

	/**
	 * Aufrufsparameter.
	 */
	private static final String P_KONF_BEREICHE = "-KonfigurationsBereichsPid";

	/**
	 * Atg Analyse Taupunkttemperatur
	 */
	private static final String ATG_TPT_ANALYSE_FB = "atg.ufdmsTaupunktTemperaturFahrBahn";

	/**
	 * Minute in ms.
	 */
	public final long minInMs = 60 * 1000L;

	/**
	 * Die Datenbeschreibung fuer Ausgabedaten.
	 */
	protected DataDescription ddGlaettePrognose;

	/**
	 * Menge der Sensoren die zu eine Messstelle gehoeren.
	 */
	protected static final String MNG_SENSOREN = "UmfeldDatenSensoren";

	/**
	 * Die uebergebene konfigurationsbereiche.
	 */
	private String konfBereiche = null;

	/**
	 * Verbindung zum DAV.
	 */
	protected ClientDavInterface dav;

	/**
	 * Enthaelt die Ringpuffer und andere Daten fuer die Berechnungen pro
	 * MessStelle.
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	protected static class UmfDatenHist {
		
		/**
		 * Standardkonstruktor.
		 *
		 * @param messStelle
		 *            die UFD-Messstelle
		 * @param keinTptSensor true wenn es keinen TPT-Sensor gibt und daher immer die Analysedaten verwendet werden sollen
		 */
		public UmfDatenHist(final SystemObject messStelle, final boolean keinTptSensor) {
			this.messStelle = messStelle;
			this.keinTptSensor = keinTptSensor;
		}

		/**
		 * Wird nur bei Publizierung der ausgaben benoetigt.
		 */
		public SystemObject messStelle = null;

		/**
		 * true wenn es keinen TPT-Sensor gibt und daher immer die Analysedaten verwendet werden sollen
		 */
		public final boolean keinTptSensor;

		/**
		 * Groesse des Ringpuffers.
		 */
		public static final int PUFFER_GROESSE = 10;

		/**
		 * Rigpuffer fuer zeitStempel.
		 */
		public long[] zsPuffer = new long[UmfDatenHist.PUFFER_GROESSE];

		/**
		 * Ringpuffer fuer Taupunkttemperaturen.
		 */
		public double[] tptPuffer = new double[UmfDatenHist.PUFFER_GROESSE];

		/**
		 * Ringpuffer fuer Fahrbahnoberflaechentemperaturen.
		 */
		public double[] fbtPuffer = new double[UmfDatenHist.PUFFER_GROESSE];

		/**
		 * Letzter empfangene Fahrbahnoberflaechenzustand.
		 */
		public long letzteFbz = EntscheidungsBaum.FBZ_UNDEFINIERT;

		/**
		 * Letzte empfangene Fahrbahnoberflaechentemperatur.
		 */
		public double letzteFbt = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;

		/**
		 * Letzte empfangene Taupunkttemperatur.
		 */
		public double letzteTpt = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
		
		/**
		 * Letzte empfangene Taupunkttemperatur aus der Datenaufbereitung.
		 */
		public double letzteTptAnalyseFb = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;

		/**
		 * Letzte empfangene Lufttemperatur.
		 */
		public double letzteLft = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;

		/**
		 * Zeitstempel der letzten Fahrbahnoberflaechentemperatur.
		 */
		public long zsLetzterFbt = 0;

		/**
		 * Zeitstempel der letzten Taupunkttemperatur.
		 */
		public long zsLetzterTpt = 0;

		/**
		 * Zeitstempel des letzten Fahrbahnoberflaechenzustandes.
		 */
		public long zsLetztenFbz = 0;

		/**
		 * Zeitstempel der letzten Lufttemperatur.
		 */
		public long zsLetzterLft = 0;
		
		/**
		 * Zeitstempel des letzten Taupunkttemperatur aus der Datenaufbereitung.
		 */
		public long zsLetzterTptAnalyseFb;

		/**
		 * Index fuer neachsten Eintrag im Ringpuffer.
		 */
		public int index = 0;

		/**
		 * Zeitstempel des letzten publizierten DS.
		 */
		public long letzterPubZs = 0;

		/**
		 * Flag bestimmt, ob ein datensatz mit "keine Daten" geschickt wurde.
		 */
		public boolean keineDaten = true;

	};

	/**
	 * Hashtabelle abbildet die sensoren auf die Messstelledaten.
	 */
	private final HashMap<SystemObject, UmfDatenHist> mapUmfDaten = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] results) {
		for (final ResultData resDatei : results) {

			final Data daten = resDatei.getData();
			final SystemObject objekt = resDatei.getObject();
			final UmfDatenHist umfDaten = mapUmfDaten.get(objekt);

			long zs = resDatei.getDataTime();

			if (umfDaten == null) {
				LOGGER.warning("Umfelddaten fuer Messstelle nicht gefunden: "
						+ objekt.getPid());
				continue;
			}

			// wenn keine eingabedaten zur verfuaegung stehen, dann auch die
			// ausgabe angepasst werden muss
			if (daten == null) {
				if (!umfDaten.keineDaten) {
					publiziere(umfDaten, null, zs, true);
				}
				continue;
			}

			final String atgPid = resDatei.getDataDescription()
					.getAttributeGroup().getPid();
			
			if (!GlaetteWarnungUndPrognose.ATG_TPT_ANALYSE_FB.equals(atgPid)) {
				// Nur daten in Minutenintervall werden bearbeitet
				// Diese Prüfung nicht für Analysedaten durchführen, da diese kein T-Attribut haben
				if(daten.getTimeValue("T").getMillis() != minInMs) {
					continue;
				}
			}

			if (GlaetteWarnungUndPrognose.ATG_FBT.equals(atgPid)) {
				umfDaten.letzteFbt = getDataAsDouble(daten, "FahrBahnOberFlächenTemperatur");
				umfDaten.zsLetzterFbt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_LFT.equals(atgPid)) {
				umfDaten.letzteLft = getDataAsDouble(daten, "LuftTemperatur");
				umfDaten.zsLetzterLft = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_TPT.equals(atgPid)) {
				double d = getDataAsDouble(daten, "TaupunktTemperatur");
				if(d == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR
						|| d == EntscheidungsBaum.MESSWERT_UNDEFIENIERT) {
					// Daten nicht ermittelbar oder fehlerhaft, stattdessen Analysedaten nehmen
					if(zs <= umfDaten.zsLetzterTptAnalyseFb) {
						d = umfDaten.letzteTptAnalyseFb;
						zs = umfDaten.zsLetzterTptAnalyseFb;
					}
				}
				umfDaten.letzteTpt = d;
				umfDaten.zsLetzterTpt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_FBZ.equals(atgPid)) {
				umfDaten.letzteFbz = daten.getItem("FahrBahnOberFlächenZustand")
						.getUnscaledValue("Wert").longValue();
				umfDaten.zsLetztenFbz = zs;
				bearbeiteDaten(umfDaten, zs);
			}   
			else if (GlaetteWarnungUndPrognose.ATG_TPT_ANALYSE_FB.equals(atgPid)) {
				double d = getDataAsDouble2(daten, "TaupunktTemperaturFahrBahn");
				umfDaten.letzteTptAnalyseFb = d;
				umfDaten.zsLetzterTptAnalyseFb = zs;
				if(umfDaten.keinTptSensor 
						|| umfDaten.letzteTpt == EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR
						|| umfDaten.letzteTpt == EntscheidungsBaum.MESSWERT_UNDEFIENIERT){
					// TPT-Daten durch Analysedaten ersetzen
					if(umfDaten.zsLetzterTpt <= zs) {
						umfDaten.letzteTpt = d;
						umfDaten.zsLetzterTpt = zs;
					}
				}
				bearbeiteDaten(umfDaten, zs);
			}
		}
	}

	private double getDataAsDouble(final Data daten, final String name) {
		Data.NumberValue value = daten.getItem(name)
				.getScaledValue("Wert");
		if(value.isState()){
			return EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR;
		}
		return value.doubleValue();
	}
	
	private double getDataAsDouble2(final Data daten, final String name) {
		Data.NumberValue value = daten.getScaledValue(name);
		if(value.isState()){
			return EntscheidungsBaumKnoten.EBK_TEMPERATUR_NICHT_ERMITTELBAR;
		}
		return value.doubleValue();
	}

	/**
	 * Wird bei jedem einkomenden plausiblen datensatz gerufen.
	 *
	 * @param umfDaten
	 *            Datenstruktur mit Daten der Messstelle
	 * @param zeitStempel
	 *            Aktueller Zeitstempel
	 */
	public void bearbeiteDaten(final UmfDatenHist umfDaten,
			final long zeitStempel) {

		// Wenn beiden Tpt und Fbt schon gekommen sind, koennen wir sie
		// ins Ringpuffer einschreiben
		if ((umfDaten.zsLetzterFbt == umfDaten.zsLetzterTpt)
				&& (umfDaten.zsLetzterTpt == zeitStempel)) {

			umfDaten.fbtPuffer[umfDaten.index] = umfDaten.letzteFbt;
			umfDaten.tptPuffer[umfDaten.index] = umfDaten.letzteTptAnalyseFb;
			umfDaten.zsPuffer[umfDaten.index] = zeitStempel;
			umfDaten.index = (umfDaten.index + 1) % UmfDatenHist.PUFFER_GROESSE;

			if ((zeitStempel - umfDaten.zsPuffer[umfDaten.index]) > ((10 * minInMs) + 100)) {
				// Wir loeschen die eintraege die Aelter als 10 Minuten sind
				for (int i = 0; i < umfDaten.zsPuffer.length; i++) {
					if ((zeitStempel - umfDaten.zsPuffer[i]) > ((10 * minInMs) + 100)) {
						umfDaten.zsPuffer[i] = 0;
					}
				}
			}
		}
		// Wenn schon 2 Datensaetze gekommen sind und im vorherigen intervall
		// wurde noch nicht publiziert
		// dann schicken wir einen nicht ermittelbaren Datensatz
		int anzahl = 0;
		if (umfDaten.zsLetzterFbt == zeitStempel) {
			anzahl++;
		}
		if (umfDaten.zsLetzterTpt == zeitStempel) {
			anzahl++;
		}
		if (umfDaten.zsLetzterLft == zeitStempel) {
			anzahl++;
		}
		if (umfDaten.zsLetztenFbz == zeitStempel) {
			anzahl++;
		}

		if ((anzahl == 2) && (umfDaten.letzterPubZs < (zeitStempel - minInMs))) {
			publiziereNichtErmmittelbar(umfDaten, zeitStempel - minInMs);
		}

		versuchePrognosePublizieren(umfDaten, zeitStempel);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final ClientDavInterface connection)
			throws Exception {

		dav = connection;
		Collection<SystemObject> messStellen;
		final Collection<ConfigurationArea> kBereiche;

		kBereiche = DUAUtensilien.getKonfigurationsBereicheAlsObjekte(
				dav, konfBereiche);
		
		messStellen = DUAUtensilien
				.getBasisInstanzen(
						dav.getDataModel().getType(
								DUAKonstanten.TYP_UFD_MESSSTELLE), dav,
						kBereiche);

		SystemObject fbtSensor, lftSensor, fbzSensor, tptSensor;
		Collection<SystemObject> fbtSensorMenge, lftSensorMenge, fbzSensorMenge, tptSensorMenge;
		fbtSensorMenge = new LinkedList<>();
		lftSensorMenge = new LinkedList<>();
		fbzSensorMenge = new LinkedList<>();
		tptSensorMenge = new LinkedList<>();

		for (final SystemObject so : messStellen) {
			if (!(so instanceof ConfigurationObject)) {
				continue;
			}
			final ConfigurationObject confObjekt = (ConfigurationObject) so;
			final ObjectSet sensorMenge = confObjekt
					.getObjectSet(GlaetteWarnungUndPrognose.MNG_SENSOREN);
			fbtSensor = lftSensor = fbzSensor = tptSensor = null;
			for (final SystemObject sensor : sensorMenge.getElements()) {
				if (sensor.isValid()) {
					final ConfigurationObject confObjekt2 = (ConfigurationObject) sensor;
					final Data konfDaten = confObjekt2
							.getConfigurationData(dav
									.getDataModel()
									.getAttributeGroup(
											GlaetteWarnungUndPrognose.ATG_UFDSENSOR));

					if(konfDaten != null) {
						if(GlaetteWarnungUndPrognose.TYP_LFT.equals(sensor
								                                            .getType().getPid())
								&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
							lftSensor = sensor;
						}
						else if(GlaetteWarnungUndPrognose.TYP_FBT.equals(sensor
								                                                 .getType().getPid())
								&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
							fbtSensor = sensor;
						}
						else if(GlaetteWarnungUndPrognose.TYP_FBZ.equals(sensor
								                                                 .getType().getPid())
								&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
							fbzSensor = sensor;
						}
						else if(GlaetteWarnungUndPrognose.TYP_TPT.equals(sensor
								                                                 .getType().getPid())
								&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
							tptSensor = sensor;
						}
					}
				}
			}
			if (lftSensor == null) {
				LOGGER.warning("Messstelle " + so.getPid()
						+ " enthaelt keinen Lufttemperatur Hauptsensor");
				continue;
			} else if (fbtSensor == null) {
				LOGGER.warning("Messstelle "
						+ so.getPid()
						+ " enthaelt keinen Fahrbahnoberflaechentemperatur Hauptsensor");
				continue;
			} else if (fbzSensor == null) {
				LOGGER.warning("Messstelle "
						+ so.getPid()
						+ " enthaelt keinen Fahrbahnoberflaechenzustand Hauptsensor");
				continue;
			}

			final UmfDatenHist ud = new UmfDatenHist(so, tptSensor == null);

			// Die datenstruktur der Messstelle soll mit Hilfe jedes Sensors
			// gefunden werden
			mapUmfDaten.put(lftSensor, ud);
			mapUmfDaten.put(fbtSensor, ud);
			mapUmfDaten.put(fbzSensor, ud);
			
			fbtSensorMenge.add(fbtSensor);
			fbzSensorMenge.add(fbzSensor);
			lftSensorMenge.add(lftSensor);
			
			if(tptSensor != null) {
				mapUmfDaten.put(tptSensor, ud);
				tptSensorMenge.add(tptSensor);
			}
		}

		final DataDescription ddTptDaten = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_TPT),
				dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddFbtDaten = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_FBT),
				dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddFbzDaten = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_FBZ),
				dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddLftDaten = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_LFT),
				dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));
		
		final DataDescription ddTptAnalyseDaten = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						ATG_TPT_ANALYSE_FB),
				dav.getDataModel().getAspect(
						DUAKonstanten.ASP_ANALYSE));

		dav.subscribeReceiver(this, tptSensorMenge,
				ddTptDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbtSensorMenge,
				ddFbtDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbzSensorMenge,
				ddFbzDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, lftSensorMenge,
				ddLftDaten, ReceiveOptions.normal(), ReceiverRole.receiver());	
		
		dav.subscribeReceiver(this, messStellen,
				ddTptAnalyseDaten, ReceiveOptions.normal(), ReceiverRole.receiver());

		ddGlaettePrognose = new DataDescription(dav
				.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_GLAETTE),
				dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_PROGNOSE));

		dav.subscribeSender(this, messStellen,
				ddGlaettePrognose, SenderRole.source());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void parseArguments(final ArgumentList argumente) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(final Thread t, final Throwable e) {
				LOGGER.error("Applikation wird wegen" + //$NON-NLS-1$
						" unerwartetem Fehler beendet", e); //$NON-NLS-1$
				e.printStackTrace();
				Runtime.getRuntime().exit(-1);
			}
		});

		if (!argumente.hasArgument(GlaetteWarnungUndPrognose.P_KONF_BEREICHE)) {
			throw new DUAInitialisierungsException(
					"Keine Konfigurationsbereiche eingegeben.");
		}

		String argParameter;
		argParameter = argumente.fetchArgument(
				GlaetteWarnungUndPrognose.P_KONF_BEREICHE).asString();

		if (argParameter.length() < 1) {
			throw new DUAInitialisierungsException(
					"Keine Konfigurationsbereiche eingegeben.");
		}

		this.konfBereiche = argParameter;

		argumente.fetchUnusedArguments();
	}

	/**
	 * Publiziert einen Datensatz, der als "nicht ermittelbar" gekennzeichnet
	 * ist.
	 *
	 * @param ud
	 *            Messstelledaten
	 * @param zeitStempel
	 *            Aktueller Zeistempel
	 */
	public void publiziereNichtErmmittelbar(final UmfDatenHist ud,
			final long zeitStempel) {
		final String[] attGlaettePrognose = new String[] { "AktuellerZustand",
				"PrognoseZustandIn5Minuten", "PrognoseZustandIn15Minuten",
				"PrognoseZustandIn30Minuten", "PrognoseZustandIn60Minuten",
				"PrognoseZustandIn90Minuten" };

		final Data glaetteDs = dav
				.createData(dav.getDataModel()
						.getAttributeGroup(
								GlaetteWarnungUndPrognose.ATG_GLAETTE));

		glaetteDs.getItem(attGlaettePrognose[0]).asUnscaledValue().set(-1);

		for (int i = 0; i < 5; i++) {
			glaetteDs.getItem(attGlaettePrognose[i + 1]).asUnscaledValue()
					.set(-1);
		}

		publiziere(ud, glaetteDs, zeitStempel, false);
	}

	/**
	 * Berechnet und publiziert die Prognose fuer eine Messstelle.
	 *
	 * @param ud
	 *            Messstelle daten
	 * @param zeitStempel
	 *            Aktueller Zeitstempel
	 */
	public void versuchePrognosePublizieren(final UmfDatenHist ud,
			final long zeitStempel) {

		final String[] attGlaettePrognose = new String[] { "AktuellerZustand",
				"PrognoseZustandIn5Minuten", "PrognoseZustandIn15Minuten",
				"PrognoseZustandIn30Minuten", "PrognoseZustandIn60Minuten",
				"PrognoseZustandIn90Minuten" };
		double[] fbtExtrapoliert, tptExtrapoliert;
		Data glaetteDs;
		int letzterIndex;
		int prognose;
		int cnt = 0;

		if (ud.letzterPubZs == zeitStempel) {
			return;
		}

		glaetteDs = dav
				.createData(dav.getDataModel()
						.getAttributeGroup(
								GlaetteWarnungUndPrognose.ATG_GLAETTE));
		letzterIndex = ((ud.index + UmfDatenHist.PUFFER_GROESSE) - 1)
				% UmfDatenHist.PUFFER_GROESSE;

		// Wenn im letzten intervall beide Fbt und tpt gekommen sind, und der
		// Anzahl der Gueltigen
		// DS im Puffer groesser als 7 ist, dann berechnen wir die Prognose

		if (ud.zsPuffer[letzterIndex] != zeitStempel) {
			// letzte DS noch nicht gekommen
			fbtExtrapoliert = new double[attGlaettePrognose.length - 1];
			tptExtrapoliert = new double[attGlaettePrognose.length - 1];
			for (int i = 0; i < fbtExtrapoliert.length; i++) {
				fbtExtrapoliert[i] = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
				tptExtrapoliert[i] = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
			}
		} else {

			for (final long element : ud.zsPuffer) {
				if (element != 0) {
					cnt++;
				}
			}

			// Fehlende DS aus aelteren Intervallen
			if (cnt < 7) {
				fbtExtrapoliert = new double[attGlaettePrognose.length - 1];
				tptExtrapoliert = new double[attGlaettePrognose.length - 1];
				for (int i = 0; i < fbtExtrapoliert.length; i++) {
					fbtExtrapoliert[i] = -1001;
					tptExtrapoliert[i] = -1001;
				}
			} else {
				// Korrekte Historie
				fbtExtrapoliert = PrognoseZustand.berechnePrognose(
						ud.fbtPuffer, ud.zsPuffer, letzterIndex);
				tptExtrapoliert = PrognoseZustand.berechnePrognose(
						ud.tptPuffer, ud.zsPuffer, letzterIndex);
			}
		}

		double fbt, tpt, lft;
		long fbz;

		if (ud.zsLetztenFbz == zeitStempel) {
			fbz = ud.letzteFbz;
		} else {
			fbz = EntscheidungsBaum.FBZ_UNDEFINIERT;
		}

		if (ud.zsLetzterFbt == zeitStempel) {
			fbt = ud.letzteFbt;
		} else {
			fbt = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
		}

		if (ud.zsLetzterTpt == zeitStempel) {
			tpt = ud.letzteTpt;
		} else {
			tpt = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
		}

		if (ud.zsLetzterLft == zeitStempel) {
			lft = ud.letzteLft;
		} else {
			lft = EntscheidungsBaum.MESSWERT_UNDEFIENIERT;
		}

		prognose = EntscheidungsBaum.getPrognose(fbz, fbt, tpt, lft, fbt, tpt);
		if (prognose == EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH) {
			return;
		}
		if (GlaetteWarnungUndPrognose.DEBUG) {
			System.out.println("1. Prognose: " + fbz + ", " + fbt + ", " + tpt
					+ ", " + lft + ", " + fbt + ", " + tpt);
		}

		glaetteDs.getItem(attGlaettePrognose[0]).asUnscaledValue()
				.set(prognose);

		for (int i = 0; i < 5; i++) {
			if (GlaetteWarnungUndPrognose.DEBUG) {
				System.out.println((i + 2) + ". Prognose: " + fbz + ", " + fbt
						+ ", " + tpt + ", " + lft + ", " + fbtExtrapoliert[i]
						+ ", " + tptExtrapoliert[i] + ", fbt - tpt = "
						+ (fbtExtrapoliert[i] - tptExtrapoliert[i]));
			}
			prognose = EntscheidungsBaum.getPrognose(fbz, fbt, tpt, lft,
					fbtExtrapoliert[i], tptExtrapoliert[i]);
			if (prognose == EntscheidungsBaum.EB_DATEN_NICHT_VOLLSTAENDIG_ENTSCHEIDUNG_NICHT_MOEGLICH) {
				return;
			}
			glaetteDs.getItem(attGlaettePrognose[i + 1]).asUnscaledValue()
					.set(prognose);
		}

		publiziere(ud, glaetteDs, zeitStempel, false);
	}

	/**
	 * Publiziert einen Datensatz.
	 *
	 * @param ud
	 *            Messstelledaten
	 * @param daten
	 *            Datum zum publizieren
	 * @param zeitStempel
	 *            Zeitstempel des Datums
	 * @param keineDaten
	 *            <code>true</code> wenn eid Datensatz mit "keine Daten"
	 *            publiziert werden soll
	 */
	public void publiziere(final UmfDatenHist ud, final Data daten,
			final long zeitStempel, final boolean keineDaten) {
		ResultData resultate;

		if (keineDaten) {
			resultate = new ResultData(ud.messStelle, ddGlaettePrognose,
					zeitStempel, null);
		} else {
			resultate = new ResultData(ud.messStelle, ddGlaettePrognose,
					zeitStempel, daten);
		}

		ud.keineDaten = keineDaten;
		ud.letzterPubZs = zeitStempel;
		try {
			dav.sendData(resultate);
		} catch (final DataNotSubscribedException e) {
			LOGGER.warning("Datenabsendung unmoeglich");
			e.printStackTrace();
		} catch (final SendSubscriptionNotConfirmed e) {
			LOGGER.warning("Datenabsendung unmoeglich");
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Ergibt die Name der SWE.
	 *
	 * @return die Name der SWE
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_DATENAUFBEREITUNG_UFD;
	}

	/**
	 * Haupmethode.
	 *
	 * @param args
	 *            Aufrufsparameter
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new GlaetteWarnungUndPrognose(), args);
	}
}
