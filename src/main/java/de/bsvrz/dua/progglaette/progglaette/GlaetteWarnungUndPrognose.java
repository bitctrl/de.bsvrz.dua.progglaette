/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.14 Gl�ttewarnung und -prognose
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
 * Wei�enfelser Stra�e 67<br>
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
	public static final String ATG_FBT = "atg.ufdsFahrBahnOberFl�chenTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String ATG_TPT = "atg.ufdsTaupunktTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String ATG_FBZ = "atg.ufdsFahrBahnOberFl�chenZustand";

	/**
	 * String konstanten.
	 */
	public static final String ATG_GLAETTE = "atg.ufdmsGl�tte";

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
	public static final String TYP_FBT = "typ.ufdsFahrBahnOberFl�chenTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String TYP_TPT = "typ.ufdsTaupunktTemperatur";

	/**
	 * String konstanten.
	 */
	public static final String TYP_FBZ = "typ.ufdsFahrBahnOberFl�chenZustand";

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
		public UmfDatenHist(final SystemObject messStelle) {
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
	private final HashMap<SystemObject, UmfDatenHist> mapUmfDaten = new HashMap<SystemObject, UmfDatenHist>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] results) {
		for (final ResultData resDatei : results) {

			final Data daten = resDatei.getData();
			final SystemObject objekt = resDatei.getObject();
			final UmfDatenHist umfDaten = mapUmfDaten.get(objekt);

			final long zs = resDatei.getDataTime();

			if (umfDaten == null) {
				LOGGER.warning(
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

			final String atgPid = resDatei.getDataDescription()
					.getAttributeGroup().getPid();

			if (GlaetteWarnungUndPrognose.ATG_FBT.equals(atgPid)) {
				double d = daten.getItem("FahrBahnOberFl�chenTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (d > 0) {
					d = daten.getItem("FahrBahnOberFl�chenTemperatur")
							.getScaledValue("Wert").doubleValue();
				}
				umfDaten.letzteFbt = d;
				umfDaten.zsLetzterFbt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_LFT.equals(atgPid)) {
				double d = daten.getItem("LuftTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (d > 0) {
					d = daten.getItem("LuftTemperatur").getScaledValue("Wert")
							.doubleValue();
				}
				umfDaten.letzteLft = d;
				umfDaten.zsLetzterLft = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_TPT.equals(atgPid)) {
				double d = daten.getItem("TaupunktTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (d > 0) {
					d = daten.getItem("TaupunktTemperatur")
							.getScaledValue("Wert").doubleValue();
				}
				umfDaten.letzteTpt = d;
				umfDaten.zsLetzterTpt = zs;
				bearbeiteDaten(umfDaten, zs);
			} else if (GlaetteWarnungUndPrognose.ATG_FBZ.equals(atgPid)) {
				final long d = daten.getItem("FahrBahnOberFl�chenZustand")
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
	public void bearbeiteDaten(final UmfDatenHist umfDaten,
			final long zeitStempel) {

		// Wenn beiden Tpt und Fbt schon gekommen sind, koennen wir sie
		// ins Ringpuffer einschreiben
		if ((umfDaten.zsLetzterFbt == umfDaten.zsLetzterTpt)
				&& (umfDaten.zsLetzterTpt == zeitStempel)) {

			umfDaten.fbtPuffer[umfDaten.index] = umfDaten.letzteFbt;
			umfDaten.tptPuffer[umfDaten.index] = umfDaten.letzteTpt;
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

		GlaetteWarnungUndPrognose.dav = connection;
		Collection<SystemObject> messStellen = new LinkedList<SystemObject>();
		final Collection<ConfigurationArea> konfBerieche = new LinkedList<ConfigurationArea>();
		final Collection<SystemObjectType> typen = new LinkedList<SystemObjectType>();

		msgSender = MessageSender.getInstance();

		for (final String s : this.konfBereiche) {
			ConfigurationArea ca = GlaetteWarnungUndPrognose.dav.getDataModel()
					.getConfigurationArea(s);
			if (ca == null) {
				msgSender.sendMessage(MessageType.APPLICATION_DOMAIN,
						MessageGrade.WARNING,
						"Der �bergebene Konfigurationsbereich " + s
						+ " existiert nicht.");
				LOGGER.warning(
						"Der �bergebene Konfigurationsbereich " + s
						+ " existiert nicht.");
				ca = GlaetteWarnungUndPrognose.dav.getDataModel()
						.getConfigurationAuthority().getConfigurationArea();
			}
			konfBerieche.add(ca);
		}

		typen.add(GlaetteWarnungUndPrognose.dav.getDataModel().getType(
				GlaetteWarnungUndPrognose.TYP_MESSSTELLE));
		messStellen = GlaetteWarnungUndPrognose.dav.getDataModel().getObjects(
				konfBerieche, typen, ObjectTimeSpecification.valid());

		SystemObject fbtSensor, lftSensor, fbzSensor, tptSensor;
		Collection<SystemObject> fbtSensorMenge, lftSensorMenge, fbzSensorMenge, tptSensorMenge;
		fbtSensorMenge = new LinkedList<SystemObject>();
		lftSensorMenge = new LinkedList<SystemObject>();
		fbzSensorMenge = new LinkedList<SystemObject>();
		tptSensorMenge = new LinkedList<SystemObject>();

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
							.getConfigurationData(GlaetteWarnungUndPrognose.dav
									.getDataModel()
									.getAttributeGroup(
											GlaetteWarnungUndPrognose.ATG_UFDSENSOR));

					if (GlaetteWarnungUndPrognose.TYP_LFT.equals(sensor
							.getType().getPid())
							&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
						lftSensor = sensor;
					} else if (GlaetteWarnungUndPrognose.TYP_FBT.equals(sensor
							.getType().getPid())
							&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
						fbtSensor = sensor;
					} else if (GlaetteWarnungUndPrognose.TYP_FBZ.equals(sensor
							.getType().getPid())
							&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
						fbzSensor = sensor;
					} else if (GlaetteWarnungUndPrognose.TYP_TPT.equals(sensor
							.getType().getPid())
							&& (konfDaten.getUnscaledValue("Typ").intValue() == 0)) {
						tptSensor = sensor;
					}
				}
			}
			if (lftSensor == null) {
				LOGGER
				.warning(
						"Messstelle "
								+ so.getPid()
								+ " enthaelt keinen Lufttemperatur Hauptsensor");
				continue;
			} else if (fbtSensor == null) {
				LOGGER
				.warning(
						"Messstelle "
								+ so.getPid()
								+ " enthaelt keinen Fahrbahnoberflaechentemperatur Hauptsensor");
				continue;
			} else if (fbzSensor == null) {
				LOGGER
				.warning(
						"Messstelle "
								+ so.getPid()
								+ " enthaelt keinen Fahrbahnoberflaechenzustand Hauptsensor");
				continue;
			} else if (tptSensor == null) {
				LOGGER
				.warning(
						"Messstelle "
								+ so.getPid()
								+ " enthaelt keinen Taupunkttemperatur Hauptsensor");
				continue;
			}

			final UmfDatenHist ud = new UmfDatenHist(so);

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

		final DataDescription ddTptDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_TPT),
				GlaetteWarnungUndPrognose.dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddFbtDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_FBT),
				GlaetteWarnungUndPrognose.dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddFbzDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_FBZ),
				GlaetteWarnungUndPrognose.dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		final DataDescription ddLftDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_LFT),
				GlaetteWarnungUndPrognose.dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		GlaetteWarnungUndPrognose.dav.subscribeReceiver(this, tptSensorMenge,
				ddTptDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		GlaetteWarnungUndPrognose.dav.subscribeReceiver(this, fbtSensorMenge,
				ddFbtDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		GlaetteWarnungUndPrognose.dav.subscribeReceiver(this, fbzSensorMenge,
				ddFbzDaten, ReceiveOptions.normal(), ReceiverRole.receiver());
		GlaetteWarnungUndPrognose.dav.subscribeReceiver(this, lftSensorMenge,
				ddLftDaten, ReceiveOptions.normal(), ReceiverRole.receiver());

		ddGlaettePrognose = new DataDescription(GlaetteWarnungUndPrognose.dav
				.getDataModel().getAttributeGroup(
						GlaetteWarnungUndPrognose.ATG_GLAETTE),
				GlaetteWarnungUndPrognose.dav.getDataModel().getAspect(
						GlaetteWarnungUndPrognose.ASP_PROGNOSE));

		GlaetteWarnungUndPrognose.dav.subscribeSender(this, messStellen,
				ddGlaettePrognose, SenderRole.source());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void parseArguments(final ArgumentList argumente) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(
					@SuppressWarnings("unused") final Thread t,
					final Throwable e) {
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
	public void publiziereNichtErmmittelbar(final UmfDatenHist ud,
			final long zeitStempel) {
		final String[] attGlaettePrognose = new String[] { "AktuellerZustand",
				"PrognoseZustandIn5Minuten", "PrognoseZustandIn15Minuten",
				"PrognoseZustandIn30Minuten", "PrognoseZustandIn60Minuten",
		"PrognoseZustandIn90Minuten" };

		final Data glaetteDs = GlaetteWarnungUndPrognose.dav
				.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
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

		glaetteDs = GlaetteWarnungUndPrognose.dav
				.createData(GlaetteWarnungUndPrognose.dav.getDataModel()
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
			GlaetteWarnungUndPrognose.dav.sendData(resultate);
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