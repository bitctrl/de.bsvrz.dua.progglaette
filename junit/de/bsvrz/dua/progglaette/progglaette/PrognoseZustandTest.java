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

/**
 * Testet die Klasse PrognoseZustand
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class PrognoseZustandTest extends PrognoseZustand {
	
	/**
	 * Berechnet dem Mittelwert der Werte im Array
	 * @param werte Array von double Werten
	 * @return Mittelwert
	 */
	public double mittelWert(double [] werte) {
		double sum = 0.0;
		for(int i = 0; i< werte.length; i++)
			sum += werte[i];
		
		return sum/werte.length;
	}
	
	/**
	 * Berechnet dem Mittelwert der Werte im Array
	 * @param werte Array von double Werten
	 * @return Mittelwert
	 */
	public double mittelWert(long [] werte) {
		double sum = 0.0;
		for(int i = 0; i< werte.length; i++)
			sum += werte[i];
		
		return sum/werte.length;
	}
	
	/**
	 * Extrapoliert aus der Tabelle der x,y werte mit Hilfe der kleinsten Kvadrate ( Least square method)
	 * eine lineare funktion und extrapoliert dem Wert im Zeitpunkt t 
	 * @param werte Y-Werte
	 * @param zeitPunkte X-Werte
	 * @param t X-Wert zu dem man Y extrapolieren moechtet
	 * @return Y-Wert zum t
	 */
	public double trendExtrapolationKorrekt(double [] werte, long [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i];
		
		summe -= n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i];
		
		summeKvadrat -= n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}
	/**
	 * Extrapoliert aus der Tabelle der x,y werte mit Hilfe der kleinsten Kvadrate ( Least square method)
	 * eine lineare funktion und extrapoliert dem Wert im Zeitpunkt t
	 *  
	 * Fehlerhafte Formel aus AFo
	 *  
	 * @param werte Y-Werte
	 * @param zeitPunkte X-Werte
	 * @param t X-Wert zu dem man Y extrapolieren moechtet
	 * @return Y-Wert zum t
	 */
	public double trendExtrapolation(double [] werte, long [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i] - n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i] - n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}
	
	/**
	 * Generiert ein Array von zufaelligen Werten 
	 * @param min Untere Grenze des Wertebereichs
	 * @param max Obere Grenze des Wertebereichs
	 * @param anzahl Anzahl der Werten
	 * @return Array von zufaelligen Werten
	 */
	public long [] generiereRandomLongArray(long min, long max, int anzahl) {
		long [] a = new long[anzahl];
		for(int i=0; i<anzahl; i++) {
			a[i] = min + (long)(Math.random()*(max-min));
			if(a[i] == 0) i--;
		}
		
		return a;
	}
	
	/**
	 * Generiert ein Array von zufaelligen Werten 
	 * @param min Untere Grenze des Wertebereichs
	 * @param max Obere Grenze des Wertebereichs
	 * @param anzahl Anzahl der Werten
	 * @return Array von zufaelligen Werten
	 */
	public double [] generiereRandomDoubleArray(double min, double max, int anzahl) {
		double [] a = new double[anzahl];
		for(int i=0; i<anzahl; i++)
			a[i] = min + (Math.random()*(max-min));
		
		return a;
	}
	
	/**
	 * Testet die TrendExtrapolation 
	 */
	@Test
	public void TestTrendExtrapolation() {
		
		final double TOLERANZ = 1e-6;
		
		int n = 5;
		long [] x = new long [] { 1, 2, 3, 4, 5 };
		double [] y = new double [] { 1, 1.5, 2, 2.5, 3 };
		
		long [] trend = new long [n];
		int trend_c[] = new int [] { 5, 15, 30, 60, 90 };
		
		double a [] = new double [n];
		double b [] = new double [n];
		double d [] = new double [] {5.5, 10.5, 18, 33, 48 };
		
		
		for(int i = 0; i<x.length; i++) {
			x[i] = x[i]*MIN_IN_MS;
		}
		
		for(int i = 0; i<trend.length; i++) {
			trend[i] = trend_c[i] * MIN_IN_MS + x[x.length-1];
		}
		
		double [] c = PrognoseZustand.berechnePrognose(y, x, x.length-1);
		
		for(int i = 0; i<n; i++) {
			a[i] =  trendExtrapolation(y, x, trend[i]);
			b[i] =  trendExtrapolationKorrekt(y, x, trend[i]);
		}
			
		for(int i=0; i<c.length; i++) {
			Assert.assertEquals(b[i], c[i]);
			Assert.assertEquals(b[i], d[i]);
			System.out.println(String.format("[ %4d ] Prognose: %10.8f == %10.8f  Differrez: %10.8f", i, b[i], c[i], b[i] - c[i]));
		}
		
		for(int k=0; k<1000; k++) {
			System.out.println("Test " + (k+1));
			
			n = (int) (10  + (Math.round(Math.random()*90)));
			
			x = generiereRandomLongArray(0, 1000000, n);
			y = generiereRandomDoubleArray(-1000.0, 1000.0, n);
				
			for(int i = 0; i<trend.length; i++) {
				trend[i] = trend_c[i] * MIN_IN_MS + x[x.length-1];
			}
			
			c = PrognoseZustand.berechnePrognose(y, x, x.length-1);
			
			for(int i = 0; i<trend.length; i++) {
				a[i] =  trendExtrapolation(y, x, trend[i]);
				b[i] =  trendExtrapolationKorrekt(y, x, trend[i]);
			}
			
			for(int i=0; i<c.length; i++) {
				Assert.assertTrue(Math.abs(b[i]- c[i])<TOLERANZ);
				System.out.println(String.format("[ %4d ] Prognose: %10.8f == %10.8f  Differrez: %10.8f", i, b[i], c[i], b[i] - c[i]));
			}	
		}
	}

}
