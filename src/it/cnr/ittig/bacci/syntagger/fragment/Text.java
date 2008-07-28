package it.cnr.ittig.bacci.syntagger.fragment;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

public class Text {
	
	private String buffer;
	
	private Collection<Fragment> fragments;
	
	public Text(String buffer) {

		this.buffer = buffer;
		
		 fragments = new TreeSet<Fragment>();
	}

	public String getBuffer() {
		return buffer;
	}

	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}

	public boolean addFragment(Fragment fragment) {
		
		return fragments.add(fragment);
	}
	
	public Collection<Fragment> getFragments() {
		
		return Collections.unmodifiableCollection(fragments);
	}

	public Fragment getFragment(int index) {
		
		for(Iterator<Fragment> i = fragments.iterator(); i.hasNext(); ) {
			Fragment f = i.next();
			int startIndex = f.getStart();
			int endIndex = f.getEnd();
			if(index >= startIndex && index <= endIndex) {
				return f;
			}
		}
		
		return null;
	}
	
	public Fragment getNextFragment(int index) {
		
		for(Iterator<Fragment> i = fragments.iterator(); i.hasNext(); ) {
			Fragment f = i.next();
			int startIndex = f.getStart();
			if(startIndex >= index) {
				return f;
			}
		}
		
		return null;
	}
	
	public void printFragments() {
		
		for(Iterator<Fragment> i = fragments.iterator(); i.hasNext(); ) {
			Fragment f = i.next();
			System.out.println("FRAGMENT: " + f);
		}
	}
}
