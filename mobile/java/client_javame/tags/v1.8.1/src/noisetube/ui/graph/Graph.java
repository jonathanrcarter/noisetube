package noisetube.ui.graph;

import noisetube.model.Measure;
import noisetube.util.CyclicQueue;

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;

public abstract class Graph extends Container
{

	protected int minimumY;
	protected int maximumY;
	
	protected int margeX = 20; //in pixels
	protected double scaleXpx; //pixels per value unit
	protected double scaleYpx; //pixels per value unit
	
	protected CyclicQueue values;

	public class GraphValue
	{

		private double offset;

		public GraphValue(double value)
		{	
			value = Math.min(Math.max(value, minimumY), maximumY);
			offset = value - minimumY;
		}
		
		public double getOffset()
		{
			return offset;
		}
		
	}
	
	public Graph(int capacity, int minimumY, int maximumY)
	{
		super();
		values = new CyclicQueue(capacity);
		setYInterval(minimumY, maximumY); //!!!
		getStyle().setMargin(5, 5, 0, 0);
		getStyle().setPadding(0, 0, 0, 0);
	}
	
	public Graph(int capacity)
	{
		this(capacity, 0, 100); //default full % scale
	}
	
	protected void setYInterval(int minimumY, int maximumY)
	{
		this.minimumY = minimumY;
		this.maximumY = maximumY;
		scaleXpx = ((double) getWidth() - margeX) / values.getCapacity();
		scaleYpx = (getHeight()) / (maximumY - minimumY);
		if(scaleYpx == 0)
			scaleYpx = 1;
	}
	
	public void add(GraphValue gv)
	{
		values.push(gv);
	}
	
	public GraphValue getGraphValue(int index)
	{
		return (GraphValue) values.get(index);
	}
	
	public abstract void addMeasure(Measure value); //HACK, to have shared interface for LoudnessGraph & MemoryGraph (MemoryGraph exploits this method to store memory stats)
	
	protected void drawHorizonalRulerWithLabel(Graphics g, double offsetY, int lineColor, int labelColor, String labelValueUnit)
	{
		int holdColor = g.getColor(); //hold current color to reset it after the line and its label have been drawn
		g.setColor(lineColor);
		g.drawLine(	margeX,
					getHeight() - (int) (offsetY * scaleYpx),
					getWidth(),
					getHeight() - (int) (offsetY * scaleYpx));
		Font font = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		g.setFont(font);
		g.setColor(labelColor);
		g.drawString(Integer.toString((int) offsetY + minimumY) + labelValueUnit, 0, getHeight() - (int) (offsetY * scaleYpx) - (font.getSize() / 2));
		g.setColor(holdColor);
	}
	
	public void additionalPaintTask(Graphics g) //to override
	{
	}
	
	public void additionalValuePaintTask(Graphics g, int index, GraphValue gv) //to override
	{
	}
	
	/**
	 * 
	 * @see com.sun.lwuit.Container#paint(com.sun.lwuit.Graphics)
	 */
	public void paint(Graphics g)
	{
		additionalPaintTask(g);
		
		//Draw graph line:
		g.setColor(0xFFFFFF); //white
		for(int i = 1; i < values.getSize(); i++)
		{
			if(getGraphValue(i) == null)
				return;
			GraphValue current = getGraphValue(i - 1);
			GraphValue next = getGraphValue(i);
			g.drawLine(	margeX + (int) ((i - 1) * scaleXpx),
						getHeight() - (int) (current.offset * scaleYpx),
						margeX + (int) (i * scaleXpx),
						getHeight() - (int) (next.offset * scaleYpx));
			additionalValuePaintTask(g, i, current);
		}
	}

}
