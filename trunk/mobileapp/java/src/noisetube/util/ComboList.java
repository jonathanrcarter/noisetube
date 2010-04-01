/**
 * 
 */
package noisetube.util;

import java.util.Vector;

/**
 * @author mstevens
 * 
 */
public class ComboList
{

	private Vector valueList;
	private Vector labelList;
	private int defaultIdx;

	/**
	 * @param valueList
	 * @param labelList
	 */
	public ComboList()
	{
		this.valueList = new Vector();
		this.labelList = new Vector();
		this.defaultIdx = -1;
	}

	public void addItem(String label, int value, boolean asDefault)
	{
		addItem(label, value);
		if(asDefault)
			defaultIdx = labelList.size() - 1;
	}

	public void addItem(String label, int value)
	{
		valueList.addElement(new Integer(value));
		labelList.addElement(label);
	}

	/**
	 * @return the valueList
	 */
	public int[] getValueList()
	{
		int[] values = new int[valueList.size()];
		for(int v = 0; v < valueList.size(); v++)
			values[v] = ((Integer) valueList.elementAt(v)).intValue();
		return values;
	}

	public int getValueAtIdx(int idx)
	{
		return ((Integer) valueList.elementAt(idx)).intValue();
	}

	public int getIdxForValue(int value)
	{
		for(int i = 0; i < valueList.size(); i++)
			if(((Integer) valueList.elementAt(i)).intValue() == value)
				return i;
		return -1;
	}

	/**
	 * @return the labelList
	 */
	public String[] getLabelList()
	{
		String[] labels = new String[labelList.size()];
		labelList.copyInto(labels);
		return labels;
	}

	public String getLabelAtIdx(int idx)
	{
		return (String) labelList.elementAt(idx);
	}

	/**
	 * @return the defaultIdx
	 */
	public int getDefaultIdx()
	{
		return defaultIdx;
	}

	/**
	 * @param defaultIdx
	 *            the defaultIdx to set
	 */
	public void setDefaultIdx(int defaultIdx)
	{
		this.defaultIdx = defaultIdx;
	}

	public int size()
	{
		return labelList.size();
	}

}
