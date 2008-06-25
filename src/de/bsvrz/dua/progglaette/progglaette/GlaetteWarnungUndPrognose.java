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
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;

/**
 * Hauptklasse des Moduls Glaettewarnung und -prognose Bearbeitet die
 * aufrufsparameter, meldet sich an die Eingabedaten ein und publiziert die
 * Ausgaben.
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 * @version $Id$
 */
public class GlaetteWarnungUndPrognose implements ClientSenderInterface,
		ClientReceiverInterface, StandardApplication {

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
	private String[] konfBereiche = null;

	/**
	 * Verbindung zum DAV.
	 */
	protected static ClientDavInterface dav;

	/**
	 * Der Nachrichtensender.
	 */
	private MessageSender msgSender;

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
		 */
		public UmfDatenHist(SystemObject messStelle) {
			this.messStelle = messStelle;
		}

		/**
		 * Wird nur bei Publizierung der ausgaben benoetigt.
		 */
		public SystemObject messStelle = null;

		/**
		 * Groesse des Ringpuffers.
		 */
		public static final int PUFFER_GROESSE = 10;

		/**
		 * Rigpuffer fuer zeitStempel.
		 */
		public long[] zsPuffer = new long[PUFFER_GROESSE];

		/**
		 * Ringpuffer fuer Taupunkttemperaturen.
		 */
		public double[] tptPuffer = new double[PUFFER_GROESSE];

		/**
		 * Ringpuffer fuer Fahrbahnoberflaechentemperaturen.
		 */
		public double[] fbtPuffer = new double[PUFFER_GROESSE];

		/**
		 * Letzter empfangene Fahrbahnoberflaechenzustand.
		 */
		public long letzteFbz = 0;

		/**
		 * Letzte empfangene Fahrbahnoberflaechentemperatur.
		 */
		public double letzteFbt = 0;

		/**
		 * Letzte empfangene Taupunkttemperatur.
		 */
		public double letzteTpt = 0;

		/**
		 * Letzte empfangene Lufttemperatur.
		 */
		public double letzteLft = 0;

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
	private HashMap<SystemObject, UmfDatenHist> mapUmfDaten = new HashMap<SystemObject, UmfDatenHist>();

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] results) {
		for (ResultData resDatei : results) {

			Data daten = resDatei.getData();
			SystemObject objekt = resDatei.getObject();
			UmfDatenHist umfDaten = mapUmfDaten.get(objekt);

			long zs = resDatei.getDataTime();

			if (umfDaten == null) {
				Debug.getLogger().warning(
						"Umfelddaten fuer Messstelle nicht gefunden: "
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

			// Nur daten in Minutenintervall werden bearbeitet
			if (daten.getTimeValue("T").getMillis() != minInMs) {
				continue;
			}

			String atgPid = resDatei.getDataDescription().getAttributeGroup()
					.getPid();

			if (ATG_FBT.equals(atgPid)) {
				double d = daten.getItem("FahrBahnOberFlächenTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (d > 0) {
					d = daten.getItem("FahrBahnOberFlächenTemperatur")
							.getScaledValue("Wert").doubleValue();
				}
				umfDaten.letzteFbt = d;
				umfDaten.zsLetzterFbt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (ATG_LFT.equals(atgPid)) {
				double d = daten.getItem("LuftTemperatur").getUnscaledValue(
						"Wert").longValue();
				if (d > 0) {
					d = daten.getItem("LuftTemperatur").getScaledValue("Wert")
							.doubleValue();
				}
				umfDaten.letzteLft = d;
				umfDaten.zsLetzterLft = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (ATG_TPT.equals(atgPid)) {
				double d = daten.getItem("TaupunktTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (d > 0) {
					d = daten.getItem("TaupunktTemperatur").getScaledValue(
							"Wert").doubleValue();
				}
				umfDaten.letzteTpt = d;
				umfDaten.zsLetzterTpt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (ATG_FBZ.equals(atgPid)) {
				long d = daten.getItem("FahrBahnOberFlächenZustand")
						.getUnscaledValue("Wert").longValue();
				umfDaten.letzteFbz = d;
				umfDaten.zsLetztenFbz = zs;
				bearbeiteDaten(umfDaten, zs);
			}
		}
	}

	/**
	 * Wird bei jedem einkomenden plausiblen datensatz gerufen.
	 * 
	 * @param umfDaten
	 *            Datenstruktur mit Daten der Messstelle
	 * @param zeitStempel
	 *            Aktueller Zeitstempel
	 */
	public void bearbeiteDaten(UmfDatenHist umfDaten, long zeitStempel) {

		// Wenn beiden Tpt und Fbt schon gekommen sind, koennen wir sie
		// ins Ringpuffer einschreiben
		if (umfDaten.zsLetzterFbt == umfDaten.zsLetzterTpt
				&& umfDaten.zsLetzterTpt == zeitStempel) {

			umfDaten.fbtPuffer[umfDaten.index] = umfDaten.letzteFbt;
			umfDaten.tptPuffer[umfDaten.index] = umfDaten.letzteTpt;
			umfDaten.zsPuffer[umfDaten.index] = zeitStempel;
			umfDaten.index = (umfDaten.index + 1) % UmfDatenHist.PUFFER_GROESSE;

			if (zeitStempel - umfDaten.zsPuffer[umfDaten.index] > 10 * minInMs + 100) {
				// Wir loeschen die eintraege die Aelter als 10 Minuten sind
				for (int i = 0; i < umfDaten.zsPuffer.length; i++) {
					if ((zeitStempel - umfDaten.zsPuffer[i]) > 10 * minInMs + 100) {
						umfDaten.zsPuffer[i] = 0;
					}
				}
			}
		}
		// Wenn schon 2 Datensaetze gekommen sind und im vorheridgen intervall
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

		if (anzahl == 2 && umfDaten.letzterPubZs < zeitStempel - minInMs) {
			publiziereNichtErmmittelbar(umfDaten, zeitStempel - minInMs);
		}

		versuchePrognosePublizieren(umfDaten, zeitStempel);

	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(ClientDavInterface connection) throws Exception {

		dav = connection;
		Collection<SystemObject> messStellen = new LinkedList<SystemObject>();
		Collection<ConfigurationArea> konfBerieche = new LinkedList<ConfigurationArea>();
		Collection<SystemObjectType> typen = new LinkedList<SystemObjectType>();

		msgSender = MessageSender.getInstance();

		for (String s : this.konfBereiche) {
			ConfigurationArea ca = dav.getDataModel().getConfigurationArea(s);
			if (ca == null) {
				msgSender.sendMessage(MessageType.APPLICATION_DOMAIN,
						MessageGrade.WARNING,
						"Der übergebene Konfigurationsbereich " + s
								+ " existiert nicht.");
				Debug.getLogger().warning(
						"Der übergebene Konfigurationsbereich " + s
								+ " existiert nicht.");
				ca = dav.getDataModel().getConfigurationAuthority()
						.getConfigurationArea();
			}
			konfBerieche.add(ca);
		}

		typen.add(dav.getDataModel().getType(TYP_MESSSTELLE));
		messStellen = dav.getDataModel().getObjects(konfBerieche, typen,
				ObjectTimeSpecification.valid());

		SystemObject fbtSensor, lftSensor, fbzSensor, tptSensor;
		Collection<SystemObject> fbtSensorMenge, lftSensorMenge, fbzSensorMenge, tptSensorMenge;
		fbtSensorMenge = new LinkedList<SystemObject>();
		lftSensorMenge = new LinkedList<SystemObject>();
		fbzSensorMenge = new LinkedList<SystemObject>();
		tptSensorMenge = new LinkedList<SystemObject>();

		for (SystemObject so : messStellen) {
			if (!(so instanceof ConfigurationObject)) {
				continue;
			}
			ConfigurationObject confObjekt = (ConfigurationObject) so;
			ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
			fbtSensor = lftSensor = fbzSensor = tptSensor = null;
			for (SystemObject sensor : sensorMenge.getElements()) {
				if (sensor.isValid()) {
					ConfigurationObject confObjekt2 = (ConfigurationObject) sensor;
					Data konfDaten = confObjekt2.getConfigurationData(dav
							.getDataModel().getAttributeGroup(ATG_UFDSENSOR));

					if (TYP_LFT.equals(sensor.getType().getPid())
							&& konfDaten.getUnscaledValue("Typ").intValue() == 0) {
						lftSensor = sensor;
					} else if (TYP_FBT.equals(sensor.getType().getPid())
							&& konfDaten.getUnscaledValue("Typ").intValue() == 0) {
						fbtSensor = sensor;
					} else if (TYP_FBZ.equals(sensor.getType().getPid())
							&& konfDaten.getUnscaledValue("Typ").intValue() == 0) {
						fbzSensor = sensor;
					} else if (TYP_TPT.equals(sensor.getType().getPid())
							&& konfDaten.getUnscaledValue("Typ").intValue() == 0) {
						tptSensor = sensor;
					}
				}
			}
			if (lftSensor == null) {
				Debug
						.getLogger()
						.warning(
								"Messstelle "
										+ so.getPid()
										+ " enthaelt keinen Lufttemperatur Hauptsensor");
				continue;
			} else if (fbtSensor == null) {
				Debug
						.getLogger()
						.warning(
								"Messstelle "
										+ so.getPid()
										+ " enthaelt keinen Fahrbahnoberflaechentemperatur Hauptsensor");
				continue;
			} else if (fbzSensor == null) {
				Debug
						.getLogger()
						.warning(
								"Messstelle "
										+ so.getPid()
										+ " enthaelt keinen Fahrbahnoberflaechenzustand Hauptsensor");
				continue;
			} else if (tptSensor == null) {
				Debug
						.getLogger()
						.warning(
								"Messstelle "
										+ so.getPid()
										+ " enthaelt keinen Taupunkttemperatur Hauptsensor");
				continue;
			}

			UmfDatenHist ud = new UmfDatenHist(so);

			// Die datenstruktur der Messstelle soll mit Hilfe jedes Sensors
			// gefunden werden
			mapUmfDaten.put(lftSensor, ud);
			mapUmfDaten.put(fbtSensor, ud);
			mapUmfDaten.put(tptSensor, ud);
			mapUmfDaten.put(fbzSensor, ud);

			tptSensorMenge.add(tptSensor);
			fbtSensorMenge.add(fbtSensor);
			fbzSensorMenge.add(fbzSensor);
			lftSensorMenge.add(lftSensor);
		}

		DataDescription ddTptDaten = new DataDescription(dav.getDataModel()
				.getAttributeGroup(ATG_TPT), dav.getDataModel().getAspect(
				ASP_MESSWERT_ERSETZUNG));

		DataDescription ddFbtDaten = new DataDescription(dav.getDataModel()
				.getAttributeGroup(ATG_FBT), dav.getDataModel().getAspect(
				ASP_MESSWERT_ERSETZUNG));

		DataDescription ddFbzDaten = new DataDescription(dav.getDataModel()
				.getAttributeGroup(ATG_FBZ), dav.getDataModel().getAspect(
				ASP_MESSWERT_ERSETZUNG));

		DataDescription ddLftDaten = new DataDescription(dav.getDataModel()
				.getAttributeGroup(ATG_LFT), dav.getDataModel().getAspect(
				ASP_MESSWERT_ERSETZUNG));

		dav.subscribeReceiver(this, tptSensorMenge, ddTptDaten, ReceiveOptions
				.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbtSensorMenge, ddFbtDaten, ReceiveOptions
				.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, fbzSensorMenge, ddFbzDaten, ReceiveOptions
				.normal(), ReceiverRole.receiver());
		dav.subscribeReceiver(this, lftSensorMenge, ddLftDaten, ReceiveOptions
				.normal(), ReceiverRole.receiver());

		ddGlaettePrognose = new DataDescription(dav.getDataModel()
				.getAttributeGroup(ATG_GLAETTE), dav.getDataModel().getAspect(
				ASP_PROGNOSE));

		dav.subscribeSender(this, messStellen, ddGlaettePrognose, SenderRole
				.source());

	}

	/**
	 * {@inheritDoc}
	 */
	public void parseArguments(ArgumentList argumente) throws Exception {
		Thread
				.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(@SuppressWarnings("unused")
					Thread t, Throwable e) {
						Debug.getLogger().error("Applikation wird wegen" + //$NON-NLS-1$
								" unerwartetem Fehler beendet", e); //$NON-NLS-1$
						e.printStackTrace();
						Runtime.getRuntime().exit(-1);
					}
				});

		if (!argumente.hasArgument(P_KONF_BEREICHE)) {
			throw new DUAInitialisierungsException(
					"Keine Konfigurationsbereiche eingegeben.");
		}

		String argParameter;
		argParameter = argumente.fetchArgument(P_KONF_BEREICHE).asString();

		if (argParameter.length() < 1) {
			throw new DUAInitialisierungsException(
					"Keine Konfigurationsbereiche eingegeben.");
		}

		this.konfBereiche = argParameter.split(",");

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
	public void publiziereNichtErmmittelbar(UmfDatenHist ud, long zeitStempel) {
		final String[] attGlaettePrognose = new String[] { "AktuellerZustand",
				"PrognoseZustandIn5Minuten", "PrognoseZustandIn15Minuten",
				"PrognoseZustandIn30Minuten", "PrognoseZustandIn60Minuten",
				"PrognoseZustandIn90Minuten" };

		Data glaetteDs = dav.createData(dav.getDataModel().getAttributeGroup(
				ATG_GLAETTE));

		glaetteDs.getItem(attGlaettePrognose[0]).asUnscaledValue().set(-1);

		for (int i = 0; i < 5; i++) {
			glaetteDs.getItem(attGlaettePrognose[i + 1]).asUnscaledValue().set(
					-1);
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
	public void versuchePrognosePublizieren(UmfDatenHist ud, long zeitStempel) {

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

		glaetteDs = dav.createData(dav.getDataModel().getAttributeGroup(
				ATG_GLAETTE));
		letzterIndex = (ud.index + UmfDatenHist.PUFFER_GROESSE - 1)
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

			for (int i = 0; i < ud.zsPuffer.length; i++) {
				if (ud.zsPuffer[i] != 0) {
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
		if (DEBUG) {
			System.out.println("1. Prognose: " + fbz + ", " + fbt + ", " + tpt
					+ ", " + lft + ", " + fbt + ", " + tpt);
		}

		glaetteDs.getItem(attGlaettePrognose[0]).asUnscaledValue()
				.set(prognose);

		for (int i = 0; i < 5; i++) {
			if (DEBUG) {
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
			glaetteDs.getItem(attGlaettePrognose[i + 1]).asUnscaledValue().set(
					prognose);
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
	public void publiziere(UmfDatenHist ud, Data daten, long zeitStempel,
			boolean keineDaten) {
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
		} catch (DataNotSubscribedException e) {
			Debug.getLogger().warning("Datenabsendung unmoeglich");
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed e) {
			Debug.getLogger().warning("Datenabsendung unmoeglich");
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
	public static void main(String[] args) {
		StandardApplicationRunner.run(new GlaetteWarnungUndPrognose(), args);
	}
}
