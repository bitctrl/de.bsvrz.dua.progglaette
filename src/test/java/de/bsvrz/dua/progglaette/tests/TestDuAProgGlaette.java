/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.progglaette.tests.
 * 
 * de.bsvrz.dua.progglaette.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.progglaette.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.progglaette.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.progglaette.tests;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.kappich.pat.testumg.util.DavTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuAProgGlaette extends DuAProgGlaetteTestBase {

	private SystemObject _tpt;
	private SystemObject _fbz;
	private SystemObject _lt;
	private SystemObject _rlf;
	private SystemObject _fbt;
	private SystemObject _messstelle;
	private AttributeGroup _atgtpt;
	private AttributeGroup _atgfbz;
	private AttributeGroup _atglt;
	private AttributeGroup _atgrlf;
	private AttributeGroup _atgfbt;
	private Aspect _aspAnalyse;
	private AttributeGroup _atgGlaette;
	private AttributeGroup _atgtptAna;
	private DataDescription _ddGlaette;
	private Aspect _aspPrognose;
	private static final AtomicLong _timestamp = new AtomicLong(60000);

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		_tpt = _dataModel.getObject("ufd.tpt");
		_fbz = _dataModel.getObject("ufd.fbz");
		_lt = _dataModel.getObject("ufd.lt");
		_rlf = _dataModel.getObject("ufd.rlf");
		_fbt = _dataModel.getObject("ufd.fbt");
		_messstelle = _dataModel.getObject("ufdm.1");
		_atgtpt = _dataModel.getAttributeGroup("atg.ufds" + "TaupunktTemperatur");
		_atgfbz = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenZustand");
		_atglt = _dataModel.getAttributeGroup("atg.ufds" + "LuftTemperatur");
		_atgrlf = _dataModel.getAttributeGroup("atg.ufds" + "RelativeLuftFeuchte");
		_atgfbt = _dataModel.getAttributeGroup("atg.ufds" + "FahrBahnOberFlächenTemperatur");
		_atgtptAna = _dataModel.getAttributeGroup("atg.ufdmsTaupunktTemperaturFahrBahn");
		_atgGlaette = _dataModel.getAttributeGroup("atg.ufdmsGlätte");
		_aspAnalyse = _dataModel.getAspect(DUAKonstanten.ASP_ANALYSE);
		_aspPrognose = _dataModel.getAspect("asp.prognose");
		_ddGlaette = new DataDescription(_atgGlaette, _aspPrognose);
		_timestamp.set(60000);
		DavTestUtil.startRead(_messstelle, _ddGlaette);
	}

	@Test
	public void testProgGlaette() throws Exception {

		expect(null);

		sendData(fbt(5.1), lt(2.0), tpt(0.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag möglich");
		nextInterval();

		sendData(fbt(5.1), lt(2.1), tpt(0.0), fbz("trocken"));
		expect("Keine Glättegefahr");
		nextInterval();	
		
		sendData(fbt(5.0), lt(2.1), tpt(0.0), fbz("trocken"));
		expect("Glättegefahr bei Wetteränderung möglich");
		nextInterval();	
		
		sendData(fbt(3.0), lt(2.1), tpt(0.0), fbz("trocken"));
		expect("Glättegefahr bei Wetteränderung möglich");
		nextInterval();		
		
		sendData(fbt(3.0), lt(2.1), tpt(0.0), fbz("feucht"));
		expect("Eisglätte möglich");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt(0.0), fbz("feucht"));
		expect("Eisglätte sofort möglich");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt(0.0), fbz("nass"));
		expect("Eisglätte sofort möglich");
		nextInterval();	
		
		sendData(fbt(1.0), lt(2.1), tpt(0.0), fbz("Eis"));
		expect("Glätte vorhanden");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt(0.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sofort sowie Reifglätte möglich");
		nextInterval();		
		
		sendData(fbt(2.0), lt(2.1), tpt(-1.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sofort möglich");
		nextInterval();

		sendData(fbt(-3.0), lt(2.1), tpt(-1.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sowie Reifglätte sofort möglich");
		nextInterval();	
	}
	
	@Test
	public void testProgGlaetteTptAnalyse() throws Exception {

		expect(null);

		sendData(fbt(5.1), lt(2.0), tpt("fehlerhaft"), tptAna(0.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag möglich");
		nextInterval();

		sendData(fbt(5.1), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("trocken"));
		expect("Keine Glättegefahr");
		nextInterval();	
		
		sendData(fbt(5.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("trocken"));
		expect("Glättegefahr bei Wetteränderung möglich");
		nextInterval();	
		
		sendData(fbt(3.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("trocken"));
		expect("Glättegefahr bei Wetteränderung möglich");
		nextInterval();		
		
		sendData(fbt(3.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("feucht"));
		expect("Eisglätte möglich");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("feucht"));
		expect("Eisglätte sofort möglich");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("nass"));
		expect("Eisglätte sofort möglich");
		nextInterval();	
		
		sendData(fbt(1.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("Eis"));
		expect("Glätte vorhanden");
		nextInterval();	
		
		sendData(fbt(2.0), lt(2.1), tpt("fehlerhaft"), tptAna(0.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sofort sowie Reifglätte möglich");
		nextInterval();		
		
		sendData(fbt(2.0), lt(2.1), tpt("fehlerhaft"), tptAna(-1.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sofort möglich");
		nextInterval();

		sendData(fbt(-3.0), lt(2.1), tpt("fehlerhaft"), tptAna(-1.0), fbz("trocken"));
		expect("Schneeglätte oder Glatteis bei Niederschlag sowie Reifglätte sofort möglich");
		nextInterval();	
	}

	private void expect(final String expected) throws InterruptedException {
		ResultData read = DavTestUtil.readData(_messstelle, _ddGlaette);
		System.out.println(read.getData());
		if(expected == null){
			if(read.hasData()){
				Assert.fail("Erwartet: Leer, aber war: " + read.getData());
			}
		}
		else {
			if(!read.hasData()){
				Assert.fail("Erwartet: " + expected + ", aber war leer.");
			}
			Assert.assertEquals(expected, read.getData().getTextValue("AktuellerZustand").getText());
		}
	}

	private ResultData fbt(final Object value) {
		Data data = _connection.createData(_atgfbt);
		resetMyData(data);
		if(value == null) {
			data = null;
		}
		else {
			data.getItem("FahrBahnOberFlächenTemperatur").getTextValue("Wert").setText(value.toString());
		}
		return new ResultData(_fbt, new DataDescription(_atgfbt, _aspAnalyse), _timestamp.get(), data);
	}
	
	private ResultData fbz(final Object value) {
		Data data = _connection.createData(_atgfbz);
		resetMyData(data);
		if(value == null) {
			data = null;
		}
		else {
			data.getItem("FahrBahnOberFlächenZustand").getTextValue("Wert").setText(value.toString());
		}
		return new ResultData(_fbz, new DataDescription(_atgfbz, _aspAnalyse), _timestamp.get(), data);
	}
	
	private ResultData lt(final Object value) {
		Data data = _connection.createData(_atglt);
		resetMyData(data);
		if(value == null) {
			data = null;
		}
		else {
			data.getItem("LuftTemperatur").getTextValue("Wert").setText(value.toString());
		}
		return new ResultData(_lt, new DataDescription(_atglt, _aspAnalyse), _timestamp.get(), data);
	}
	
	private ResultData tpt(final Object value) {
		Data data = _connection.createData(_atgtpt);
		resetMyData(data);
		if(value == null) {
			data = null;
		}
		else {
			data.getItem("TaupunktTemperatur").getTextValue("Wert").setText(value.toString());
		}
		return new ResultData(_tpt, new DataDescription(_atgtpt, _aspAnalyse), _timestamp.get(), data);
	}
		
	
	private ResultData tptAna(final Object value) {
		Data data = _connection.createData(_atgtptAna);
		if(value == null) {
			data = null;
		}
		else {
			data.getTextValue("TaupunktTemperaturFahrBahn").setText(value.toString());
		}
		return new ResultData(_tpt, new DataDescription(_atgtptAna, _aspAnalyse), _timestamp.get(), data);
	}
	
	private void nextInterval() {
		_timestamp.getAndAdd(60000);
	}

	public void resetMyData(final Data data) {
		resetData(data);
		data.getTimeValue("T").setMillis(60000);
	}
}
