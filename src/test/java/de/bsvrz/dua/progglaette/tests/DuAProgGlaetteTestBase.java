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

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dua.progglaette.progglaette.GlaetteWarnungUndPrognose;
import de.bsvrz.dua.tests.DuATestBase;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import org.junit.After;
import org.junit.Before;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class DuAProgGlaetteTestBase extends DuATestBase {
	protected  GlaetteWarnungUndPrognose _glaetteWarnungUndPrognose;

	protected String[] getUfdArgs() {
		return new String[]{"-KonfigurationsBereichsPid=kb.duaTestUfd"};
	}


	@Override
	protected String[] getConfigurationAreas() {
		return new String[]{"kb.duaTestUfd"};
	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		_glaetteWarnungUndPrognose = new GlaetteWarnungUndPrognose();
		_glaetteWarnungUndPrognose.parseArguments(new ArgumentList(getUfdArgs()));
		_glaetteWarnungUndPrognose.initialize(_connection);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	public void sendData(final ResultData... resultDatas) throws SendSubscriptionNotConfirmed {
		_glaetteWarnungUndPrognose.update(resultDatas);
	}
}
