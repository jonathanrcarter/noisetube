/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Java ME version)
 *  
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *  
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *  
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.ui.javame;


import net.noisetube.audio.Calibration;
import net.noisetube.audio.AudioComponent;
import net.noisetube.audio.javame.JavaMERecordTask;
import net.noisetube.core.NTClient;
import net.noisetube.core.javame.MainMIDlet;
import net.noisetube.model.IMeasurementListener;
import net.noisetube.model.Measurement;
import net.noisetube.util.Logger;

import com.sun.lwuit.CheckBox;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * 
 * @author maisonneuve, mstevens
 * 
 */
public class CalibrationForm implements IMeasurementListener
{

	private Logger log = Logger.getInstance();
	private MainMIDlet midlet = MainMIDlet.getInstance();
	
	AudioComponent audioComp;
	
	private Form f;
	// private TextField ref = new TextField("", 3);
	// private TabbedPane tabs = new TabbedPane();

	private Label lblLoudness = new Label("");
	private ComboBox sources_box = new ComboBox();
	private final CalibrationGraph graph = new CalibrationGraph();
	private Calibration calibration;

	// private Container advanced = new Container();
	// private TextField dev_add = new TextField("", 4);
	// private TextField ref_add = new TextField("", 4);
	// private LogUIComponent logui = new LogUIComponent();

	CheckBox A_weighting_filter = new CheckBox("Apply A-weighting");

	private Command back = new Command("Back");
	private Command calibrate = new Command("Calibrate");
	private Command stop = new Command("Stop");

	private List list_correction = new List();

	public CalibrationForm()
	{
		init();
	}

	private void init()
	{
		try
		{
			audioComp = new AudioComponent(NTClient.getInstance().getDevice().getAudioSpecificationToUse(), this);	
		}
		catch(Exception e)
		{
			log.error(e, "CalibrationForm.init()");
		}
	}

	public Form getForm()
	{
		f = new Form("Calibration");
		// calibration = midlet.getPreferences().getCalibration();
		A_weighting_filter.setSelected(true);
		// tabs.addTab("Basic", basic);
		// tabs.addTab("Advanced", advanced);
		// ref = new TextField("...");
		// ref.setMaxSize(3);
		Container basic = new Container();
		// recorder.setRecordingTime(1100);
		// recorder.setTimeInterval(1);

		// BASIC TAB
		basic.setLayout(new BorderLayout());
		Container ref_container = new Container();
		ref_container.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		ref_container.addComponent(A_weighting_filter);
		ref_container.addComponent(new Label("Not corrected Leq:"));
		ref_container.addComponent(text);

		// ref_container.addComponent(new Label("Real SL:"));
		// ref_container.addComponent(ref);

		basic.addComponent(BorderLayout.CENTER, ref_container);
		/*
		 * recorder.setLeqListener(new ILeqListener() {
		 * 
		 * public void sendLeq(double leq) { try { current_leq = leq;
		 * text.setText("" + ((float) ((int) leq * 10) / 10.0)); // long now=
		 * System.currentTimeMillis(); logui.displayLog(); } catch (Exception e)
		 * { log.error(e, "calibration"); } } });
		 */

		// ADVANCED TAB
		/*
		 * graph.setPreferredSize(new Dimension(150, 150)); Container advanced_c
		 * = new Container(); advanced_c.setLayout(new
		 * BoxLayout(BoxLayout.Y_AXIS)); advanced_c.addComponent(graph);
		 * sources_box.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent ev) { // nothing actually }
		 * }); // "remove values" component Container remove_corr_container =
		 * new Container(); remove_corr_container.setLayout(new
		 * BoxLayout(BoxLayout.X_AXIS));
		 * remove_corr_container.addComponent(sources_box); Button delBtn = new
		 * Button("Del"); delBtn.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent ev) { Association o =
		 * (Association) list_correction.getSelectedItem(); if
		 * (Dialog.show("Delete correction",
		 * "Do you want to delete the association " + o.toString(), "Yes",
		 * "No")) { calibration.removeCorrectionAt(o.idx); refresh(); } } });
		 * remove_corr_container.addComponent(delBtn);
		 * advanced_c.addComponent(remove_corr_container);
		 * 
		 * // "add value" component Container add_corr_container = new
		 * Container(); add_corr_container.setLayout(new
		 * BoxLayout(BoxLayout.X_AXIS)); add_corr_container.addComponent(new
		 * Label("Phone(x):")); dev_add.setPreferredW(30);
		 * ref_add.setPreferredW(30); dev_add.setNextFocusDown(ref_add);
		 * ref_add.setNextFocusUp(dev_add);
		 * add_corr_container.addComponent(dev_add);
		 * add_corr_container.addComponent(new Label("Ref(y):"));
		 * add_corr_container.addComponent(ref_add); Button addBtn = new
		 * Button("Add"); addBtn.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent ae) { try { double dev_val =
		 * Double.parseDouble(dev_add.getText()); double ref_val =
		 * Double.parseDouble(ref_add.getText());
		 * calibration.addCorrection(ref_val, dev_val); refresh(); } catch
		 * (Exception e) { log.error("Value parsing error");
		 * Dialog.show("Error", "Parsing error", "Ok", "Cancel"); } } });
		 * add_corr_container.addComponent(addBtn);
		 * advanced_c.addComponent(add_corr_container);
		 * 
		 * Container reset_container = new Container();
		 * reset_container.setLayout(new FlowLayout());
		 * advanced_c.addComponent(reset_container); Button reset = new
		 * Button("Reset"); //reset.getStyle().setPadding(5, 5, 7, 7);
		 * //reset.getStyle().setMargin(10, 10, 50, 70);
		 * 
		 * reset.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent arg0) { calibration.reset();
		 * refresh(); } });
		 * 
		 * //advanced_c.addComponent(reset);
		 * reset_container.addComponent(reset); Button clean = new
		 * Button("Clean"); clean.getStyle().setPadding(5, 5, 7, 7);
		 * clean.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent arg0) {
		 * calibration.removeAll(); refresh(); } });
		 * //advanced_c.addComponent(clean);
		 * reset_container.addComponent(clean);
		 * 
		 * 
		 * advanced.setLayout(new BorderLayout());
		 * advanced.addComponent(BorderLayout.CENTER, advanced_c);
		 * 
		 * refresh();
		 */
		f.addComponent(basic);
		f.addCommand(calibrate);
		f.addCommand(back);
		f.addCommandListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent ae)
			{
				if(back.equals(ae.getCommand()))
				{
					audioComp.stop();
					midlet.showMeasureForm();
				}
				else
				{ 	//start or stop
					if(audioComp.isRunning())
					{
						stopCalibration();
					}
					else
					{
						startCalibration();
					}
				}
			}
		});

		// Return the form:
		return f;
	}
	
	public void receiveMeasurement(Measurement m)
	{
		if()
		lblLoudness.setText(m.getLoudnessLeqDBA())
		
	}

	public void refresh()
	{
		calibration.update();
		midlet.getPreferences().save();
		double[][] data = calibration.corrector;
		graph.data = data;
		// find max
		graph.reset_axis();
		// find min

		list_correction = new List();
		for(int i = 0; i < data.length; i++)
		{
			list_correction.addItem(new Association(
					data[i][Calibration.INPUT_IDX] + " --> "
							+ data[i][Calibration.OUTPUT_IDX] + " dB", i));
			if(data[i][Calibration.INPUT_IDX] > graph.maxx)
			{
				graph.setMaxx((int) data[i][Calibration.INPUT_IDX]);
			}
			if(data[i][Calibration.INPUT_IDX] < graph.minx)
			{
				graph.setMinx((int) data[i][Calibration.INPUT_IDX]);
			}
			if(data[i][Calibration.INPUT_IDX] > graph.maxy)
			{
				graph.setMaxy((int) data[i][Calibration.OUTPUT_IDX]);
			}
			if(data[i][Calibration.INPUT_IDX] < graph.miny)
			{
				graph.setMiny((int) data[i][Calibration.OUTPUT_IDX]);
			}
		}
		sources_box.setModel(list_correction.getModel());
		graph.repaint();
	}

	private void startCalibration()
	{
		f.removeAllCommands();
		f.addCommand(stop);
		f.addCommand(back);
		text.setText("Measuring...");
		audioComp.start();
	}

	private void stopCalibration()
	{
		f.removeAllCommands();
		f.addCommand(calibrate);
		f.addCommand(back);

		audioComp.stop();
		/*
		 * if ((Dialog.show("Calibration",
		 * "Do you want calibrate your device from " + current_leq +
		 * " dB(A) to " + ref.getText() + " dB(A)", "Ok", "Cancel"))) { double
		 * ref_leq = Double.parseDouble(ref.getText()); int step = (int)
		 * (ref_leq - current_leq); double[][] data = calibration.corrector; for
		 * (int i = 0; i < data.length; i++) { data[i][Calibration.OUTPUT_IDX]
		 * += step; } midlet.getPreferences().save(); }
		 */
	}

	final class Association
	{
		String text;
		int idx;

		public Association(String text, int idx)
		{
			this.text = text;
			this.idx = idx;
		}

		public String toString()
		{
			return text;
		}
	}

}
