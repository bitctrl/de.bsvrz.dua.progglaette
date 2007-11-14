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
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;

public class EntscheidungsBaumKnoten {
	
	
	public static final int EBK_TEMPERATUR_NICHT_ERMITTELBAR = -1001;
	
	/**
	 * Nachfolgende Knoten
	 */
	protected EntscheidungsBaumKnoten nachfolgerLinks = null;
	protected EntscheidungsBaumKnoten nachfolgerRechts = null;
	
	/**
	 * Die EntscheidungsMethode des Knotens
	 */
	private EntscheidungsMethode methode = null; 
	
	/**
	 * Ergebnisswert, dem ein Endknoten leifert
	 */
	private int ergebnissWert = 0;

	/**
	 * 
	 * Die moegliche Horizonte fuer die Entscheidung
	 *
	 */
	public enum Horizont {
		HOR_AKTUELLER_ZUSTAND,
		HOR_PROGNOSE_ZUSTAND_5MIN,
		HOR_PROGNOSE_ZUSTAND_10MIN,
		HOR_PROGNOSE_ZUSTAND_15MIN,
		HOR_PROGNOSE_ZUSTAND_30MIN,
		HOR_PROGNOSE_ZUSTAND_60MIN
	};
	
	
	/**
	 * Die EntscheidungsMethode des Knotens, entscheidet ob man nach links/rechts/mitte
	 * weitergeht, oder Etnscheidung trifft 
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	public interface EntscheidungsMethode {
		/**
		 * Ergibt, ob wir nach links oder rechts in dem Unterbaum weitergehen 
		 * @param fbzAktuell
		 * @param fbtAktuell
		 * @param tptAktuell
		 * @param lftAktuell
		 * @param fbtExtrapoliert
		 * @param tptExtrapoliert
		 * @param horizont
		 * @return <code>true</code>, wenn nach links
		 */
		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont);
	}
	
	
	public abstract class EntscheidungTemperatur implements EntscheidungsMethode {
		private long grenzWert = 0;
		private EntscheidungsBaum.Operator operator = null;
		protected long wert = 0;
		
		public EntscheidungTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			this.grenzWert = grenzWert;
			this.operator = operator;
		}
		
		protected final int entscheide(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont) {
			if(operator.anwende(wert, grenzWert)) {
				if(EntscheidungsBaumKnoten.this.nachfolgerLinks == null) return 0;
				return EntscheidungsBaumKnoten.this.nachfolgerLinks.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
			} else {
				if(EntscheidungsBaumKnoten.this.nachfolgerRechts == null) return 0;
				return EntscheidungsBaumKnoten.this.nachfolgerRechts.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
			}
		}
	}
	
	public class FbofTemperatur extends EntscheidungTemperatur {

		public FbofTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell,
				long tptAktuell, long lftAktuell, long fbtExtrapoliert,
				long tptExtrapoliert, Horizont horizont) {
			if(fbtAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			wert = fbtAktuell;
			return entscheide(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}
	}
		

	public class LuftTemperatur extends EntscheidungTemperatur {

		public LuftTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell,
				long tptAktuell, long lftAktuell, long fbtExtrapoliert,
				long tptExtrapoliert, Horizont horizont) {
			if(lftAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR)
				return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			wert = lftAktuell;
			return entscheide(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}
	}
	
	public class DifferenzFbofTaupunktTemperatur extends EntscheidungTemperatur {

		public DifferenzFbofTaupunktTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell,
				long tptAktuell, long lftAktuell, long fbtExtrapoliert,
				long tptExtrapoliert, Horizont horizont) {
			if(fbtAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR ||
					tptAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR) 
				return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			wert = fbtAktuell - tptAktuell;
			return entscheide(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}
	}

	public class DifferenzFbofTaupunktPrognoseTemperatur extends EntscheidungTemperatur {

		public DifferenzFbofTaupunktPrognoseTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell,
				long tptAktuell, long lftAktuell, long fbtExtrapoliert,
				long tptExtrapoliert, Horizont horizont) {
			if(tptExtrapoliert == EBK_TEMPERATUR_NICHT_ERMITTELBAR ||
					fbtAktuell == EBK_TEMPERATUR_NICHT_ERMITTELBAR) {
				return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH; 
			}
			wert = fbtAktuell - tptExtrapoliert;
			return entscheide(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}
	}
	
	public class FbofPrognoseTemperatur extends EntscheidungTemperatur {

		public FbofPrognoseTemperatur(long grenzWert, EntscheidungsBaum.Operator operator) {
			super(grenzWert, operator);
		}

		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell,
				long tptAktuell, long lftAktuell, long fbtExtrapoliert,
				long tptExtrapoliert, Horizont horizont) {
			if(fbtExtrapoliert == EBK_TEMPERATUR_NICHT_ERMITTELBAR) {
				return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
			}
			wert = fbtExtrapoliert;
			return entscheide(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}
	}
	
	public class FahbrBahnZustand implements EntscheidungsMethode {
		private long [] werteLinks = null;
		private long [] werteRechts = null;
		
		public FahbrBahnZustand(long [] werteLinks, long [] werteRechts) {
			this.werteLinks = werteLinks;
			this.werteRechts = werteRechts;
		}
		
		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont) {
			long wert = fbzAktuell.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").longValue();
			for(int i=0; i<werteLinks.length; i++) 
				if(werteLinks[i] == wert) {
					if(EntscheidungsBaumKnoten.this.nachfolgerLinks == null) return 0;
					return EntscheidungsBaumKnoten.this.nachfolgerLinks.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
				}
			for(int i=0; i<werteRechts.length; i++) 
				if(werteRechts[i] == wert) {
					if(EntscheidungsBaumKnoten.this.nachfolgerRechts == null) return 0;
					return EntscheidungsBaumKnoten.this.nachfolgerRechts.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
				}
			return EntscheidungsBaum.EB_TENDENZBERECHNUNG_NICHT_MOEGLICH;
		}
	}
	
	public class FahbrBahnZustandMitGlaette extends FahbrBahnZustand {
		
		private long [] werteGlaette = null;
		
		public FahbrBahnZustandMitGlaette(long [] werteLinks, long [] werteRechts, long [] werteGlaette) {
			super(werteLinks, werteRechts);
			this.werteGlaette = werteGlaette;
		}
		
		public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont) {
			long wert = fbzAktuell.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").longValue();
			for(int i=0; i<werteGlaette.length; i++) 
				if(werteGlaette[i] == wert) 
					return EntscheidungsBaum.EB_GLAETTE_VORHANDEN;
			return super.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
		}	
	}
		
	public int getEntscheidungsWert(Data fbzAktuell, long fbtAktuell, long tptAktuell, long lftAktuell, long fbtExtrapoliert, long tptExtrapoliert, Horizont horizont) {
		if(methode == null) return ergebnissWert;
		return methode.getEntscheidungsWert(fbzAktuell, fbtAktuell, tptAktuell, lftAktuell, fbtExtrapoliert, tptExtrapoliert, horizont);
	}
	
	public EntscheidungsBaumKnoten(EntscheidungsBaumKnoten nachfolgerLinks, EntscheidungsBaumKnoten nachfolgerRechts) {		
		this.nachfolgerLinks = nachfolgerLinks;
		this.nachfolgerRechts = nachfolgerRechts;
	}
	
	public EntscheidungsBaumKnoten(int ergebnisswert) {
		this.methode = null;
		this.ergebnissWert = ergebnisswert;
	}
	
	public void initLuftTemperaturKnoten(EntscheidungsBaum.Operator operator, long grenzwert) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new LuftTemperatur(grenzwert, operator);
	}
	
	public void initFbofTemperaturKnoten(EntscheidungsBaum.Operator operator, long grenzwert) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new FbofTemperatur(grenzwert, operator);
	}
	
	public void initFahrBahnZustandKnoten(long [] werteLinks, long [] werteRechts) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new FahbrBahnZustand(werteLinks, werteRechts );
	}
	public void initFahrBahnZustandGlaetteKnoten(long [] werteLinks, long [] werteRechts, long [] werteGlaette) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new FahbrBahnZustandMitGlaette(werteLinks, werteRechts, werteGlaette );
	}
	
	public void initFbofPrognoseKnoten(EntscheidungsBaum.Operator operator, long grenzwert) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new FbofPrognoseTemperatur(grenzwert, operator);
	}
	
	public void initDifferenzFbofTaupunktKnoten(EntscheidungsBaum.Operator operator, long grenzwert) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new DifferenzFbofTaupunktTemperatur(grenzwert, operator);
	}

	public void initDifferenzFbofTaupunktPrognoseKnoten(EntscheidungsBaum.Operator operator, long grenzwert) throws DUAInitialisierungsException {
		if(nachfolgerLinks == null || nachfolgerRechts == null)
			throw new DUAInitialisierungsException("Ein Endkonten soll keine Entscheidungsmethode beinhalten");
		this.methode = new DifferenzFbofTaupunktPrognoseTemperatur(grenzwert, operator);
	}
}
