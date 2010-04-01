package noisetube.ui;

import noisetube.model.Measure;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * @author maisonneuve, mstevens
 * 
 */
public class LoudnessUIComponent extends Container
{

	private int graphCapacity = 50;
	private Label leqLabel;

	private LoudnessGraph graph;

	public LoudnessUIComponent()
	{
		super();

		setLayout(new BorderLayout());
		// setLayout(new FlowLayout());

		// decibel value l value
		leqLabel = new Label("00");
		leqLabel.getStyle().setFont(Font.getBitmapFont("decibel"));
		// size for 1xx db
		// label.setPreferredSize(new Dimension(50, 70));
		addComponent(BorderLayout.EAST, leqLabel);
		// addComponent(label);

		// graphical curve
		setGraph();

		getStyle().setMargin(5, 5, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
		// getStyle().setBorder(Border.createLineBorder(1));
		// getStyle().setBgTransparency(100);
	}

	private void setGraph()
	{
		graph = new LoudnessGraph(graphCapacity);
		// g.setPreferredSize(new Dimension(300, 70));
		// g.getStyle().setPadding(0,0,80,10);
		addComponent(BorderLayout.CENTER, graph);

		graph.getStyle().setBgColor(0x00FF00);

	}

	public void reset()
	{
		removeComponent(graph);
		leqLabel.setText("00");
		leqLabel.getStyle().setFgColor(0xFFFFFF);
		leqLabel.getStyle().setBgColor(this.getStyle().getBgColor());
		setGraph();
	}

	public void update_graph(Measure m)
	{

		graph.push(m);
		double leqs = m.getLeq();
		// red
		if(leqs >= 80)
		{
			leqLabel.getStyle().setFgColor(0xFFFFFF);
			leqLabel.getStyle().setBgColor(0xCC0000);
		}
		else
		// yellow/orange
		if(leqs >= 70)
		{
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0xFF6600);
		}
		else
		// yellow
		if(leqs >= 60)
		{
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0xFFFF00);
		}
		else
		{
			// green
			leqLabel.getStyle().setFgColor(0x000000);
			leqLabel.getStyle().setBgColor(0x66FF00);
		}
		leqLabel.setText((int) leqs + "");
		graph.repaint();
	}

}
