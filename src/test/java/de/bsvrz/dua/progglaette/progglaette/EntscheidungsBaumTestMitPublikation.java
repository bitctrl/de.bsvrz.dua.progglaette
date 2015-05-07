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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.daf.AbstractDavZustand;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.UmfeldDatenSensorUnbekannteDatenartException;
import de.bsvrz.sys.funclib.bitctrl.dua.ufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Provoziert <b>alle</b> moeglichen Wege im Entscheidungsbaum.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: EntscheidungsBaumTestMitPublikation.java 53827 2015-03-18
 *          10:04:42Z peuker $
 */
public class EntscheidungsBaumTestMitPublikation extends GlaetteWarnungUndPrognose {

	/**
	 * Erwarteter Zustand.
	 */
	private Zustand erwarteterZustand = null;

	/**
	 * Erwarteter Zustand.
	 */
	private String horizont = null;

	/**
	 * Testsensoren.
	 */
	static SystemObject fbzSensor, fbtSensor, ltSensor, tptSensor, messStelle;
	/**
	 * datenbeschreibung fuer testdaten.
	 */
	static DataDescription ddLftDaten, ddTptDaten, ddFbtDaten, ddFbzDaten;

	/**
	 * Durchlauf.
	 */
	int lauf = 0;

	/**
	 * Der Test.
	 *
	 * @throws DUAInitialisierungsException
	 *             Ausnahme, die geworfen wird, wenn ein Modul innerhalb einer
	 *             SWE nicht initialisiert werden konnte. Also, wenn z.B. keine
	 *             Anmeldung zum Empfangen oder Versenden von Daten durchgeführt
	 *             werden konnte.
	 * @throws SendSubscriptionNotConfirmed
	 *             Ausnahme, die beim Senden von Daten als einfacher Sender
	 *             generiert wird, wenn noch keine positive Sendesteuerung vom
	 *             Datenverteiler für die zu versendenden Daten vorliegt
	 */
	@Test
	public void test1() throws DUAInitialisierungsException, SendSubscriptionNotConfirmed {
		final EntscheidungsBaumTestMitPublikation gwp = new EntscheidungsBaumTestMitPublikation();
		StandardApplicationRunner.run(gwp, Verbindung.CON_DATA.clone());
		UmfeldDatenArt.initialisiere(GlaetteWarnungUndPrognose.dav);

		try {
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, EntscheidungsBaumTestMitPublikation.tptSensor,
					EntscheidungsBaumTestMitPublikation.ddTptDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, EntscheidungsBaumTestMitPublikation.ltSensor,
					EntscheidungsBaumTestMitPublikation.ddLftDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, EntscheidungsBaumTestMitPublikation.fbtSensor,
					EntscheidungsBaumTestMitPublikation.ddFbtDaten, SenderRole.source());
			GlaetteWarnungUndPrognose.dav.subscribeSender(this, EntscheidungsBaumTestMitPublikation.fbzSensor,
					EntscheidungsBaumTestMitPublikation.ddFbzDaten, SenderRole.source());
		} catch (final Exception e) {
			System.out.println("Fehler bei Anmeldung fuer Sendung der Testdaten");
			e.printStackTrace();
		}

		GlaetteWarnungUndPrognose.dav.subscribeReceiver(new ClientReceiverInterface() {

			@Override
			public void update(final ResultData[] results) {
				final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
				for (final ResultData resultat : results) {
					if (resultat.getData() != null) {
						if (((lauf++) == 0) || (horizont == null)) {
							synchronized (GlaetteWarnungUndPrognose.dav) {
								try {
									GlaetteWarnungUndPrognose.dav.notify();
								} catch (final Exception e) {
									//
								}
							}
							return;
						}

						final Zustand ist = Zustand
								.getInstanz(resultat.getData().getUnscaledValue(horizont).intValue());
						System.out.println(
								"Ist (" + dateFormat.format(new Date(resultat.getDataTime())) + "): " + ist + "\n");
						if (EntscheidungsBaumTestMitPublikation.this.erwarteterZustand != null) {
							Assert.assertEquals(ist, EntscheidungsBaumTestMitPublikation.this.erwarteterZustand);
						}
					}
				}

				synchronized (GlaetteWarnungUndPrognose.dav) {
					try {
						GlaetteWarnungUndPrognose.dav.notify();
					} catch (final Exception e) {
						//
					}
				}
			}

		}, GlaetteWarnungUndPrognose.dav.getDataModel().getObject("ufdMessStelle.test.1"),
				new DataDescription(GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup("atg.ufdmsGlätte"),
						GlaetteWarnungUndPrognose.dav.getDataModel().getAspect("asp.prognose")),
				ReceiveOptions.normal(), ReceiverRole.receiver());

		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		/**
		 * 0
		 */
		final ArrayList<MessStellenEreignis> ereignisse = new ArrayList<MessStellenEreignis>();
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z1, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 3.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 1
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z2, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 5.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 2
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z2, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 3.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 3
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z3, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 4
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z3, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 5
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z3, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 32.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 6.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 6
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z6, "PrognoseZustandIn60Minuten",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 3.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * 7
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z5, "PrognoseZustandIn60Minuten",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * Andere Hälfte des Baumes
		 */
		/**
		 * 8
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z10, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 64.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 9
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z10, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 65.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 10
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z10, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.9),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 66.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 11
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z10, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 67.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * feucht oder nass
		 */
		/**
		 * 12
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z9, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 13
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z9, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 32.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * trocken
		 */
		/**
		 * 14
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z8, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 15
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z12, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * 16
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z7, null,
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, -2.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 17
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z7, null,
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 2.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, -1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 18
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z7, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, -1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * 19
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z7, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, -1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);
		/**
		 * 20
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z12, "PrognoseZustandIn90Minuten",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, 1.1),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, -1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		/**
		 * Extra: Provoziere "nicht ermittelbar"
		 */
		/**
		 * 21
		 */
		ereignisse.add(new MessStellenEreignis(cal.getTimeInMillis(), Zustand.Z0, "AktuellerZustand",
				new SensorEreignis[] { new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbtSensor, Double.NaN),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.fbzSensor, 0.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.tptSensor, 1.0),
						new SensorEreignis(EntscheidungsBaumTestMitPublikation.ltSensor, 2.0) }));
		cal.add(Calendar.MINUTE, 1);

		for (final MessStellenEreignis ereignis : ereignisse) {
			try {
				Thread.sleep(100L);
			} catch (final InterruptedException e) { //
			}
			synchronized (GlaetteWarnungUndPrognose.dav) {
				System.out.println(ereignis);
				this.erwarteterZustand = ereignis.zustand;
				this.horizont = ereignis.horizont;
				this.publiziere(ereignis);
				try {
					GlaetteWarnungUndPrognose.dav.wait();
				} catch (final Exception e) {
					//
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {
		super.initialize(connection);

		EntscheidungsBaumTestMitPublikation.fbzSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFZS.1");
		EntscheidungsBaumTestMitPublikation.fbtSensor = connection.getDataModel().getObject("ufdSensor.test.FBOFT.1");
		EntscheidungsBaumTestMitPublikation.ltSensor = connection.getDataModel().getObject("ufdSensor.test.LT.1");
		EntscheidungsBaumTestMitPublikation.tptSensor = connection.getDataModel().getObject("ufdSensor.test.TPT.1");

		EntscheidungsBaumTestMitPublikation.messStelle = connection.getDataModel().getObject("ufdMessStelle.test.1");

		EntscheidungsBaumTestMitPublikation.ddTptDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_TPT),
				GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		EntscheidungsBaumTestMitPublikation.ddFbtDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBT),
				GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		EntscheidungsBaumTestMitPublikation.ddFbzDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_FBZ),
				GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

		EntscheidungsBaumTestMitPublikation.ddLftDaten = new DataDescription(
				GlaetteWarnungUndPrognose.dav.getDataModel().getAttributeGroup(GlaetteWarnungUndPrognose.ATG_LFT),
				GlaetteWarnungUndPrognose.dav.getDataModel()
						.getAspect(GlaetteWarnungUndPrognose.ASP_MESSWERT_ERSETZUNG));

	}

	/**
	 * Publiziert eine Messstellenereignis.
	 *
	 * @param ereignis
	 *            ein Ereignis an einer Messstelle.
	 * @throws SendSubscriptionNotConfirmed
	 *             Ausnahme, die beim Senden von Daten als einfacher Sender
	 *             generiert wird, wenn noch keine positive Sendesteuerung vom
	 *             Datenverteiler für die zu versendenden Daten vorliegt.
	 *
	 */
	public void publiziere(final MessStellenEreignis ereignis) throws SendSubscriptionNotConfirmed {
		for (final SensorEreignis sensorEreignis : ereignis.getEreignisse()) {
			try {
				GlaetteWarnungUndPrognose.dav.sendData(sensorEreignis.getResultData(ereignis.getZeitStempel()));
			} catch (final UmfeldDatenSensorUnbekannteDatenartException e) {
				Debug.getLogger().warning(e.getMessage());
			}
		}
	}

	/**
	 * Eine Menge von gleichzeitigen Sensorereignissen an einer Messstelle.
	 *
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 * @version $Id: EntscheidungsBaumTestMitPublikation.java 53827 2015-03-18
	 *          10:04:42Z peuker $
	 */
	class MessStellenEreignis {

		/**
		 * dito.
		 */
		private List<SensorEreignis> ereignisListe = null;

		/**
		 * der provozierte Zustand.
		 */
		private Zustand zustand = null;

		/**
		 * der Prognosehorizont.
		 */
		private String horizont = null;

		/**
		 * Ereigniszeitstempel.
		 */
		private long zeitStempel = -1;

		/**
		 * Standardkonstruktor.
		 *
		 * @param zeitStempel
		 *            der Ereigniszeitstempel.
		 * @param zustand
		 *            der provozierte Zustand.
		 * @param horizont
		 *            der Prognosehorizont.
		 * @param ereignisse
		 *            eine Menge von Sensorereignissen.
		 */
		MessStellenEreignis(final long zeitStempel, final Zustand zustand, final String horizont,
				final SensorEreignis... ereignisse) {
			this.zeitStempel = zeitStempel;
			this.zustand = zustand;
			this.horizont = horizont;
			this.ereignisListe = new ArrayList<SensorEreignis>();
			for (final SensorEreignis ereignis : ereignisse) {
				this.ereignisListe.add(ereignis);
			}
		}

		/**
		 * Erfragt die Ereignisliste dieses Messstellenereignisses.
		 *
		 * @return die Ereignisliste dieses Messstellenereignisses.
		 */
		List<SensorEreignis> getEreignisse() {
			return this.ereignisListe;
		}

		/**
		 * Erfragt den Ereigniszeitstempel.
		 *
		 * @return der Ereigniszeitstempel.
		 */
		long getZeitStempel() {
			return zeitStempel;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
			String s = dateFormat.format(this.zeitStempel);

			if (this.horizont == null) {
				s += " --> Sende DUMMY\n";
			} else {
				s += " --> Provoziere in " + this.horizont + " \"" + this.zustand + "\"\n";
			}

			if (ereignisListe.size() > 0) {
				for (final SensorEreignis ereignis : ereignisListe) {
					s += ereignis + ", ";
				}
				s = s.substring(0, s.length() - 2);
			}

			return s;
		}

	}

	/**
	 * Ein Ereignis an einem Sensor.
	 *
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 * @version $Id: EntscheidungsBaumTestMitPublikation.java 53827 2015-03-18
	 *          10:04:42Z peuker $
	 */
	class SensorEreignis {

		/**
		 * der Wert.
		 */
		private long wert = 0;

		/**
		 * der Sensor.
		 */
		private SystemObject ufds = null;

		/**
		 * Standardkonstruktor.
		 *
		 * @param ufds
		 *            der Umfelddatensensor.
		 * @param wert1
		 *            der Wert (skaliert).
		 */
		SensorEreignis(final SystemObject ufds, final double wert1) {
			this.ufds = ufds;
			if (ufds.isOfType(UmfeldDatenArt.fbt.getTyp())) {
				if (Double.isNaN(wert1)) {
					this.wert = -1001;
				} else {
					this.wert = Math.round(wert1 * 10.0);
				}
			}
			if (ufds.isOfType(UmfeldDatenArt.lt.getTyp())) {
				if (Double.isNaN(wert1)) {
					this.wert = -1001;
				} else {
					this.wert = Math.round(wert1 * 10.0);
				}
			}
			if (ufds.isOfType(UmfeldDatenArt.tpt.getTyp())) {
				if (Double.isNaN(wert1)) {
					this.wert = -1001;
				} else {
					this.wert = Math.round(wert1 * 10.0);
				}
			}
			if (ufds.isOfType(UmfeldDatenArt.fbz.getTyp())) {
				if (Double.isNaN(wert1)) {
					this.wert = -1;
				} else {
					this.wert = (long) wert1;
				}
			}
		}

		/**
		 * Generiert ein Datum mit Zeitstempel.
		 *
		 * @param zeitStempel1
		 *            ein Zeitstempel
		 * @return ein Datum mit Zeitstempel.
		 * @throws UmfeldDatenSensorUnbekannteDatenartException
		 *             der übergebene Sensor hat eine unbekannte Datenart
		 */
		ResultData getResultData(final long zeitStempel1) throws UmfeldDatenSensorUnbekannteDatenartException {
			DataDescription datenBeschreibung = null;

			if (ufds.isOfType(UmfeldDatenArt.fbt.getTyp())) {
				datenBeschreibung = EntscheidungsBaumTestMitPublikation.ddFbtDaten;
			}
			if (ufds.isOfType(UmfeldDatenArt.lt.getTyp())) {
				datenBeschreibung = EntscheidungsBaumTestMitPublikation.ddLftDaten;
			}
			if (ufds.isOfType(UmfeldDatenArt.tpt.getTyp())) {
				datenBeschreibung = EntscheidungsBaumTestMitPublikation.ddTptDaten;
			}
			if (ufds.isOfType(UmfeldDatenArt.fbz.getTyp())) {
				datenBeschreibung = EntscheidungsBaumTestMitPublikation.ddFbzDaten;
			}

			final Data data = GlaetteWarnungUndPrognose.dav.createData(datenBeschreibung.getAttributeGroup());
			final String att = UmfeldDatenArt.getUmfeldDatenArtVon(this.ufds).getName();
			data.getItem("T").asTimeValue().setMillis(Constants.MILLIS_PER_MINUTE);
			data.getItem(att).getUnscaledValue("Wert").set(wert);
			data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
			data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
			data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
			data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
			data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
			data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

			return new ResultData(this.ufds, datenBeschreibung, zeitStempel1, data);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			String derWert = wert == -1001.0 ? "nErm" : new Double(wert / 10).toString();
			if (this.ufds.isOfType(UmfeldDatenArt.fbz.getTyp())) {
				final int code = (int) this.wert;
				switch (code) {
				case -1:
					derWert = "nErm";
					break;
				case 0:
					derWert = "trocken";
					break;
				case 1:
					derWert = "feucht";
					break;
				case 32:
					derWert = "nass";
					break;
				case 64:
					derWert = "gefroren";
					break;
				case 65:
					derWert = "Schnee";
					break;
				case 66:
					derWert = "Eis";
					break;
				case 67:
					derWert = "Raureif";
					break;
				default:
				}
			}
			return this.ufds.getPid() + " = " + derWert;
		}

	}

	/**
	 * Zustandsspeicher für Glaetteprognosen.
	 *
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 * @version $Id: EntscheidungsBaumTestMitPublikation.java 53827 2015-03-18
	 *          10:04:42Z peuker $
	 */
	static class Zustand extends AbstractDavZustand {

		/**
		 * Der Wertebereich dieses DAV-Enumerationstypen.
		 */
		private static Map<Integer, Zustand> werteBereich = new HashMap<Integer, Zustand>();

		/**
		 * dito.
		 */
		private static final Zustand Z0 = new Zustand("nicht ermittelbar", -1);

		/**
		 * dito.
		 */
		private static final Zustand Z1 = new Zustand("Keine Glättegefahr", 1);

		/**
		 * dito.
		 */
		private static final Zustand Z2 = new Zustand("Gättegefahr bei Wetteränderung möglich", 2);
		/**
		 * dito.
		 */
		private static final Zustand Z3 = new Zustand("Eisglätte möglich", 3);
		// /**
		// * dito.
		// */
		// private static final Zustand Z4 = new Zustand(
		// "Tendenzberechnung nicht möglich", 4);
		/**
		 * dito.
		 */
		private static final Zustand Z5 = new Zustand("Schneeglätte oder Glatteis bei Niederschlag möglich", 5);
		/**
		 * dito.
		 */
		private static final Zustand Z6 = new Zustand(
				"Schneeglätte oder Glatteis bei Niederschlag sowie Reifglätte möglich", 6);
		/**
		 * dito.
		 */
		private static final Zustand Z7 = new Zustand("Schneeglätte oder Glatteis bei Niederschlag sofort möglich", 7);
		/**
		 * dito.
		 */
		private static final Zustand Z8 = new Zustand(
				"Schneeglätte oder Glatteis bei Niederschlag sowie Reifglätte sofort möglich", 8);
		/**
		 * dito.
		 */
		private static final Zustand Z9 = new Zustand("Eisglätte sofort möglich", 9);
		/**
		 * dito.
		 */
		private static final Zustand Z10 = new Zustand("Glätte vorhanden", 10);
		// /**
		// * dito.
		// */
		// private static final Zustand Z11 = new Zustand(
		// "Eis oder Schnee auf der Fahrbahn", 11);
		/**
		 * dito.
		 */
		private static final Zustand Z12 = new Zustand(
				"Schneeglätte oder Glatteis bei Niederschlag sofort sowie Reifglätte möglich", 12);

		/**
		 * Standardkonstruktor.
		 *
		 * @param text1
		 *            Text des Zustandes
		 * @param i1
		 *            Nummer des Zustandes
		 */
		public Zustand(final String text1, final int i1) {
			super(i1, text1);
			Zustand.werteBereich.put(i1, this);
		}

		/**
		 * Erfragt eine statische Instanz dieser Klasse.
		 *
		 * @param code
		 *            deren Kode.
		 * @return eine statische Instanz dieser Klasse.
		 */
		public static Zustand getInstanz(final int code) {
			return Zustand.werteBereich.get(code);
		}

	}
}
