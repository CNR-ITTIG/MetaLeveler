package it.cnr.ittig.bacci.syntagger.fragment;

public class Fragment implements Comparable<Fragment> {

	protected String text;
	
	protected int start = 0;
	protected int end = 0;
	
	public Fragment(String text) {
		
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int compareTo(Fragment f) {

		if(this.getStart() < f.getStart()) {
			return -1;
		}
		if(this.getStart() > f.getStart()) {
			return 1;
		}		

		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Fragment other = (Fragment) obj;
		if (start != other.start)
			return false;
		return true;
	}
	
	public String toString() {
		
		return "%" + this.getText() + "%";
	}
}
